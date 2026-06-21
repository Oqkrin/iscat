package uni.gaben.iscat.universe.entities.brain.abilities;

import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entities.AbstractLivingEntityModel;
import uni.gaben.iscat.universe.entities.parsed.EntityModel;
import uni.gaben.iscat.universe.entities.brain.Brain;
import uni.gaben.iscat.universe.entities.AbstractPhysicalEntityModel;
import uni.gaben.iscat.universe.entities.player.PlayerModel;
import uni.gaben.iscat.utils.Cooldown;
import java.util.Collections;

/**
 * Abilità di cura ad area.
 * Scansiona il mondo di gioco alla ricerca di entità viventi alleate (escludendo se stessa e il giocatore umano)
 * che hanno subito danni. Se si trovano nel raggio d'azione, rigenera i loro punti endurance e innesca un'onda
 * d'urto grafica (shockwave) locale per feedback visivo coordinato.
 */
public class HealAbility extends Ability {

    private final Cooldown healCooldown;
    private final Cooldown visualHealCooldown;
    private final double range;
    private final double amount;

    /**
     * Inizializza l'abilità di cura ad area configurando i timer e l'efficacia del ripristino.
     *
     * @param cooldownSec Il tempo di ricarica logico tra un utilizzo e l'altro.
     * @param range       Il raggio massimo dell'area di effetto in metri.
     * @param amount      La quantità di punti endurance rigenerati a ogni alleato coinvolto.
     */
    public HealAbility(double cooldownSec, double range, double amount) {
        super("HealAllies", AbilityCategory.ATTACK, Collections.emptySet());
        this.healCooldown = new Cooldown(cooldownSec);
        this.visualHealCooldown = new Cooldown(cooldownSec != 0 ? cooldownSec : 3);
        this.range = range;
        this.amount = amount;
    }

    /**
     * Verifica la presenza di almeno un alleato danneggiato all'interno del raggio d'azione.
     * Esclude esplicitamente il giocatore umano (PlayerModel) dai beneficiari.
     *
     * @return {@code true} se l'abilità è pronta e vi è un target valido da curare, altrimenti {@code false}.
     */
    @Override
    public boolean canActivate(AbstractPhysicalEntityModel self, UniverseModel world, double dt) {
        if (healCooldown.isCoolingDown()) return false;

        for (AbstractLivingEntityModel l : world.getEntitiesOfType(AbstractLivingEntityModel.class)) {
            if (l == self || l instanceof PlayerModel) continue;

            // Controlla se l'alleato è danneggiato ed è a portata radiale
            if (l.getEndurance() < l.getMaxEndurance() &&
                    self.getTransform().getTranslation().distance(l.getTransform().getTranslation()) <= range) {
                return true;
            }
        }
        return false;
    }

    /**
     * Esegue la cura ad area istantanea su tutte le entità alleate idonee nel raggio.
     * Limita superiormente l'endurance incrementata al valore massimo consentito per l'entità.
     * Se l'effetto visivo è pronto, attiva un'onda d'urto radiale sul modello grafico.
     */
    @Override
    public void onActivate(Brain<?> brain, UniverseModel world) {
        AbstractPhysicalEntityModel entity = brain.getEntity();

        // Applicazione del Ripristino dei Punti Vita (Endurance)
        for (AbstractLivingEntityModel l : world.getEntitiesOfType(AbstractLivingEntityModel.class)) {
            if (l == entity || l instanceof PlayerModel) continue;
            if (entity.getTransform().getTranslation().distance(l.getTransform().getTranslation()) <= range) {
                l.setEndurance(Math.min(l.getEndurance() + amount, l.getMaxEndurance()));
            }
        }

        healCooldown.start();

        // Innesco dell'Effetto Grafico di Cura (Shockwave VFX)
        if (visualHealCooldown.isReady()) {
            visualHealCooldown.start();
            if (entity instanceof EntityModel ge) {
                // Genera l'anello visivo convertendo il raggio metrico in pixel di rendering
                ge.shockwave().trigger(visualHealCooldown.getDefaultDuration(), UU.mToPx(range / 2), UU.mToPx(range) / 10);
            }
        }
    }

    /**
     * Dichiara la fine immediata dell'attivazione dell'azione.
     *
     * @return {@code false} in quanto il ripristino delle risorse è istantaneo e non richiede cicli multi-frame.
     */
    @Override
    public boolean progressActivation(Brain<?> brain, UniverseModel world, double dt) {
        return false;
    }

    /**
     * Routine di aggiornamento per l'avanzamento dei timer del cooldown logico e del cooldown visivo della shockwave.
     */
    @Override
    public void update(Brain<?> brain, UniverseModel world, double dt) {
        healCooldown.update(dt);
        visualHealCooldown.update(dt);
    }
}