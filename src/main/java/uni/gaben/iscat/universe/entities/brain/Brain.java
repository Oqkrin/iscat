package uni.gaben.iscat.universe.entities.brain;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entities.AbstractPhysicalEntityModel;
import uni.gaben.iscat.universe.entities.brain.abilities.Ability;
import uni.gaben.iscat.universe.entities.brain.abilities.AbilityCategory;
import uni.gaben.iscat.universe.entities.brain.rotation.RotationGoal;
import uni.gaben.iscat.universe.entities.brain.steering.SteeringGoal;
import uni.gaben.iscat.universe.entities.brain.steering.SteeringModifier;
import uni.gaben.iscat.universe.entities.player.PlayerModel;
import uni.gaben.iscat.universe.entities.projectiles.Shooter;
import java.util.*;

/**
 * Controller logico centralizzato ed architettura decisionale per le entità fisiche (Agent Brain).
 * <p>
 * Agisce come coordinatore di tre pipeline distinte ed eseguite sequenzialmente ad ogni tick di simulazione:
 * </p>
 * <ul>
 * <li><b>Action Lifecycle FSM:</b> Gestisce una macchina a stati finiti per le abilità attive, risolvendo le mutue
 * esclusioni tramite maschere di categorie bloccanti ({@link AbilityCategory}).</li>
 * <li><b>Weighted Steering Blender:</b> Combina vettorialmente gli obiettivi primari di movimento con una lista
 * ordinata di modificatori comportamentali (flocking, schivate), applicando la forza finale direttamente sul corpo rigido.</li>
 * <li><b>PD Rotation Controller:</b> Corregge l'orientamento angolare dell'agente simulando un controller Proporzionale-Derivativo,
 * corredato da uno snap rigido anti-drift per prevenire oscillazioni microscopiche in virgola mobile.</li>
 * </ul>
 *
 * @param <T> Il tipo specifico di entità fisica (sottoclasse di {@link AbstractPhysicalEntityModel}) controllata da questa mente.
 */
public class Brain<T extends AbstractPhysicalEntityModel> implements IEntityController {

    /** Cache globale dell'array enum per evitare allocazioni heap superflue indotte da invocazioni cicliche di {@code values()}. */
    protected static final AbilityCategory[] CATEGORIES = AbilityCategory.values();

    protected final T entity;
    protected final Shooter<T> shooter;
    private boolean enabled = true;

    private final Vector2 steerForce = UU.vector2zero();
    private final Vector2 modifierSteer = new Vector2();

    protected final SteeringGoal defaultSteeringGoal;
    protected final RotationGoal defaultRotationGoal;
    protected SteeringGoal currentSteeringGoal;
    protected RotationGoal currentRotationGoal;

    /** Proprietà reattiva JavaFX che definisce il coefficiente di peso applicato alla forza dello SteeringGoal primario. */
    public final DoubleProperty goalWeight = new SimpleDoubleProperty(1.0);

    private final Map<String, Ability> actionsMap = new HashMap<>();
    private final Map<AbilityCategory, List<Ability>> actionsByCategory = new EnumMap<>(AbilityCategory.class);
    private final Map<AbilityCategory, Ability> activeActions = new EnumMap<>(AbilityCategory.class);
    private final Set<AbilityCategory> blockedCategories = new HashSet<>();
    private final List<AbilityCategory> finishedCategoriesList = new ArrayList<>(CATEGORIES.length);

    private final Map<String, SteeringModifier> modifiersMap = new HashMap<>();
    private final List<SteeringModifier> modifiersOrder = new ArrayList<>();

    /**
     * Costruisce il cervello dell'agente agganciandolo alla sua controparte fisica e configurando gli obiettivi di riposo (idle).
     */
    public Brain(T entity) {
        this.entity = entity;
        this.shooter = new Shooter<>(entity);

        this.defaultSteeringGoal = SteeringGoal.idle();
        this.currentSteeringGoal = defaultSteeringGoal;

        this.defaultRotationGoal = entity.getMaxAngularVelocity() > 0 ? RotationGoal.movement() : RotationGoal.idle();
        this.currentRotationGoal = defaultRotationGoal;
    }

    /**
     * Loop logico principale del controller dell'entità.
     * Coordina l'esecuzione sequenziale di: aggiornamento cooldown, rimozione/avvio abilità, blend delle forze e calcolo della coppia.
     */
    @Override
    public void update(UniverseModel universe, double dt) {
        if (!enabled) return;
        if (entity == null || entity.shouldRemove()) return;

        processActionLifecycles(universe, dt);
        computeAndApplySteering(universe, dt);
        processRotation(universe, dt);
    }

    // ========================================================================
    // UPDATE LOOP HELPERS (Private Lifecycle Pipeline)
    // ========================================================================

    /**
     * Risolve ed esegue la timeline e i vincoli delle abilità.
     * Incrementa i timer interni di tutte le abilità, analizza le esclusioni correnti, rimuove le azioni completate
     * e valuta la can attuazione di nuove abilità inattive organizzate per priorità strutturale.
     */
    private void processActionLifecycles(UniverseModel universe, double dt) {
        // 0. Tick globale dei cooldown indipendentemente dallo stato operativo
        for (Ability ability : actionsMap.values()) {
            ability.update(this, universe, dt);
        }

        blockedCategories.clear();
        finishedCategoriesList.clear();

        // 1. Raccolta delle maschere di blocco attive
        for (AbilityCategory category : CATEGORIES) {
            Ability ability = activeActions.get(category);
            if (ability != null) {
                blockedCategories.add(ability.getCategory());
                blockedCategories.addAll(ability.getBlockedCategories());
            }
        }

        // 2. Avanzamento frame-by-frame delle abilità attive e isolamento dei processi terminati
        for (AbilityCategory cat : CATEGORIES) {
            Ability ability = activeActions.get(cat);
            if (ability != null) {
                if (!ability.progressActivation(this, universe, dt)) {
                    finishedCategoriesList.add(cat);
                }
            }
        }

        // Rimozione effettiva delle abilità scadute
        for (AbilityCategory abilityCategory : finishedCategoriesList) {
            activeActions.remove(abilityCategory);
        }

        // 3. Tentativo di attivazione stocastico/prioritario delle abilità dormienti non bloccate
        for (AbilityCategory cat : CATEGORIES) {
            if (blockedCategories.contains(cat) || activeActions.containsKey(cat)) {
                continue;
            }
            List<Ability> catAbilities = actionsByCategory.get(cat);
            if (catAbilities == null || catAbilities.isEmpty()) {
                continue;
            }
            for (Ability ability : catAbilities) {
                if (ability.canActivate(entity, universe, dt)) {
                    activeActions.put(cat, ability);
                    ability.onActivate(this, universe);
                    break; // Consente un'unica attivazione per categoria per frame
                }
            }
        }
    }

    /**
     * Esegue la sommatoria lineare pesata (Weighted Linear Summation) delle forze di guida.
     * Raccoglie i contributi vettoriali da tutti i modificatori comportamentali registrati, vi addiziona la spinta
     * dell'obiettivo primario scalata per il suo peso e applica l'impulso risultante direttamente sul motore fisico.
     */
    private void computeAndApplySteering(UniverseModel universe, double dt) {
        steerForce.set(0, 0);
        double maxForce = entity.getAcceleration();

        // 1. Accumulazione cumulativa delle forze dei modificatori (Flocking, Ostacoli)
        for (SteeringModifier steeringModifier : modifiersOrder) {
            modifierSteer.set(0, 0);
            steeringModifier.computeSteer(entity, universe, maxForce, dt, modifierSteer);

            if (!modifierSteer.isZero()) {
                steerForce.add(modifierSteer);
            }
        }

        // 2. Integrazione vettoriale dell'obiettivo primario ponderato
        Vector2 primaryForce = currentSteeringGoal.computeDesiredVelocity(entity, universe, dt);
        if (primaryForce != null && !primaryForce.isZero()) {
            primaryForce.multiply(goalWeight.get());
            steerForce.add(primaryForce);
        }

        // 3. Applicazione diretta delle forze sul baricentro del corpo rigido
        entity.applyForce(steerForce);
    }

    /**
     * Monitora e corregge l'orientamento spaziale del corpo rigido tramite controllo ad anello chiuso.
     * Calcola la distanza angolare minima normalizzata entro l'intervallo $[-\pi, \pi]$. Se l'errore è inferiore alla
     * tolleranza minima, effettua uno snap rigido per eliminare l'errore statico; altrimenti calcola la coppia
     * correttiva (Torque) applicando le costanti di guadagno proporzionale ($k_p$) e derivativo ($k_d$).
     */
    private void processRotation(UniverseModel universe, double dt) {
        double maxAngularVelocity = entity.getMaxAngularVelocity();
        if (maxAngularVelocity <= 0) return;

        double desiredAngle = currentRotationGoal.compute(entity, universe, dt);
        if (Double.isNaN(desiredAngle)) return;

        double currentAngle = entity.getTransform().getRotationAngle();

        // Normalizzazione dell'angolo di sfasamento (Delta Angle Warp)
        double diff = desiredAngle - currentAngle;
        while (diff < -Math.PI) diff += 2 * Math.PI;
        while (diff > Math.PI) diff -= 2 * Math.PI;

        double angVel = entity.getAngularVelocity();

        // Soglia di stabilità: Snap d'arresto immediato per bloccare oscillazioni e drift macroscopici
        if (Math.abs(diff) < 0.01 && Math.abs(angVel) < 0.1) {
            entity.setAngularVelocity(0);
            entity.getTransform().setRotation(desiredAngle);
            return;
        }

        // Formulazione del controller PD: Coppia = (kp * errore) - (kd * velocitàAngolare)
        double kp = maxAngularVelocity * 2.0;
        double kd = maxAngularVelocity * 0.5;

        entity.applyTorque(kp * diff - kd * angVel);
    }

    // ========================================================================
    // ACTION API
    // ========================================================================

    /** Registers a new capability mapping into the internal indexing database. */
    public void addAction(String id, Ability ability) {
        if (actionsMap.containsKey(id)) {
            throw new IllegalArgumentException("Ability with id '" + id + "' already exists");
        }
        actionsMap.put(id, ability);
        actionsByCategory.computeIfAbsent(ability.getCategory(), k -> new ArrayList<>()).add(ability);
    }

    /**
     * Registers a capabilty generating a random pseudo-UUID descriptor string.
     */
    public void addAction(Ability ability) {
        String id = UUID.randomUUID().toString();
        addAction(id, ability);
    }

    /**
     * Hot-swaps an existing action pointer with a new execution definition, preserving tracking states.
     */
    public void replaceAction(String id, Ability newAbility) {
        Ability oldAbility = actionsMap.get(id);
        if (oldAbility == null) return;

        List<Ability> catList = actionsByCategory.get(oldAbility.getCategory());
        if (catList != null) {
            int idx = catList.indexOf(oldAbility);
            if (idx != -1) catList.set(idx, newAbility);
        }
        if (activeActions.get(oldAbility.getCategory()) == oldAbility) {
            activeActions.put(oldAbility.getCategory(), newAbility);
        }
        actionsMap.put(id, newAbility);
    }

    public Ability getAction(String id) { return actionsMap.get(id); }

    // ========================================================================
    // MODIFIER API
    // ========================================================================

    /** Aggrega un modificatore di sterzata associandolo a una chiave identificativa univoca. */
    public void addModifier(String id, SteeringModifier modifier) {
        if (modifiersMap.containsKey(id)) {
            throw new IllegalArgumentException("Modifier with id '" + id + "' already exists");
        }
        modifiersMap.put(id, modifier);
        modifiersOrder.add(modifier);
    }

    /**
     * Aggrega un modificatore di sterzata assegnando un identificativo generato automaticamente.
     */
    public void addModifier(SteeringModifier modifier) {
        String id = UUID.randomUUID().toString();
        addModifier(id, modifier);
    }


    // ========================================================================
    // UTILITY & MATHEMATICAL HELPERS
    // ========================================================================

    /**
     * Ottimizzato per allocare esclusivamente primitive sullo stack, eludendo la generazione di oggetti garbage di dyn4j.
     * @return Angolo assoluto espresso in radianti necessario per orientarsi verso le coordinate specificate.
     */
    public double angleToTarget(Vector2 pos) {
        Vector2 selfPos = entity.getTransform().getTranslation();
        return Math.atan2(pos.y - selfPos.y, pos.x - selfPos.x);
    }

    /**
     * Calcola l'angolo assoluto diretto verso la posizione corrente del giocatore umano.
     */
    public double angleToPlayer(UniverseModel world) {
        PlayerModel player = world.getPlayer();
        if (player == null) return 0;
        return angleToTarget(player.getTransform().getTranslation());
    }

    // ========================================================================
    // GETTERS & SETTERS
    // ========================================================================

    public void setSteeringGoal(SteeringGoal goal) { this.currentSteeringGoal = goal; }
    public SteeringGoal getSteeringGoal() { return this.currentSteeringGoal; }
    public SteeringGoal getMovementGoal() { return currentSteeringGoal; }
    public SteeringGoal getDefaultGoal() { return defaultSteeringGoal; }

    public void setRotationGoal(RotationGoal goal) { this.currentRotationGoal = goal; }
    public RotationGoal getRotationGoal() { return currentRotationGoal; }
    public RotationGoal getDefaultRotationGoal() { return defaultRotationGoal; }

    public T getEntity() { return entity; }
    public Shooter<T> getShooter() { return shooter; }

    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isEnabled() { return enabled; }
}