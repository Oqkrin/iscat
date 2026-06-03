package uni.gaben.iscat.universe.entity.enemies.generic;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseCollisionLayers;
import uni.gaben.iscat.universe.entity.LivingEntityModel;
import uni.gaben.iscat.universe.interfaces.HasSprite;
import uni.gaben.iscat.universe.interfaces.HasShockwave;
import uni.gaben.iscat.universe.rendering.vfx.ShockwaveModel;
import uni.gaben.iscat.universe.entity.player.PlayerModel;

/**
 * Modello polimorfico universale per la gestione logica e fisica dei nemici.
 * Sostituisce i vecchi modelli specifici (es. IscatMobModel, IscatCoreModel) delegando l'intera
 * configurazione geometrica, i coefficienti d'attrito, i parametri di movimento e le routine di danno
 * a un'istanza di {@link GenericEntitySettings} interamente popolata da database.
 */
public class GenericEntityModel extends LivingEntityModel implements HasSprite, HasShockwave {

    /** Archivio dei parametri strutturali e biologici caricati dal database per questa entità. */
    private final GenericEntitySettings settings;
    private final ShockwaveModel shockwaveModel = new ShockwaveModel();

    /**
     * Costruisce il corpo fisico e le proprietà logiche del nemico instanziandolo nel mondo di gioco.
     * Genera la fixture geometrica corretta convertendo le dimensioni da pixel a metri (Dyn4J),
     * configura i filtri di collisione e aggancia i listener per il danno da impatto (RAM).
     *
     * @param x        Coordinata X di spawn in metri.
     * @param y        Coordinata Y di spawn in metri.
     * @param settings DTO contenente i parametri estratti dalla tabella delle entità.
     */
    public GenericEntityModel(double x, double y, GenericEntitySettings settings) {
        super(x, y, settings.initLife, settings.initLife);
        this.settings = settings;

        // Sincronizzazione della chiave identificativa con la superclasse per il tracciamento del bestiario
        if (settings != null && settings.entityKey != null) {
            setEntityKey(settings.entityKey);
        }

        setXpReward(settings.xpReward);

        // Generazione guidata della shape fisica e conversione metrica (con tolleranza del 10% sui bordi dello sprite)
        // Calculate collision size from frame width and scale (90% of visual size for tighter hitbox)
        double collisionSize = UU.pxToM(settings.frameW * settings.scale * 0.9);
        
        BodyFixture fixture = switch (settings.shapeType) {
            case CIRCLE -> addFixture(Geometry.createCircle(collisionSize / 2.0));
            case SQUARE -> addFixture(Geometry.createSquare(collisionSize));
        };

        // Assegnazione del layer per la gestione selettiva dei contatti
        fixture.setFilter(UniverseCollisionLayers.ENEMY_FILTER);
        setMass(MassType.NORMAL);
        setLinearDamping(settings.linearDamping);

        // Iniezione della logica di collisione per i nemici con attacco da sfondamento (RAM)
        // TODO: Move RAM damage to specialized attack behavior in Brain system
        if (settings.mass > 2.0) { // Heavy enemies deal collision damage
            final double heavyDamage = settings.mass * 5.0; // Scale with mass
            final double lightDamage = settings.mass * 2.0;

            setOnCollision(other -> {
                if (other instanceof PlayerModel player) {
                    double speed = this.getLinearVelocity().getMagnitude();

                    // Applica un danno maggiore (heavy) se l'impatto avviene ad alta velocità (carica)
                    if (speed > settings.maxVelocity * 0.85) {
                        player.deltaToLife(-heavyDamage);
                    } else {
                        player.deltaToLife(-lightDamage);
                    }
                }
            });
        }
    }

    /**
     * Aggiorna lo stato temporale dell'entità per il corretto avanzamento dei frame d'animazione.
     *
     * @param dt Delta time (tempo trascorso dall'ultimo tick del ciclo di gioco).
     */
    public void update(double dt) {
        updateStateTime(dt);
        shockwaveModel.update(dt);
    }

    @Override
    public ShockwaveModel shockwave() {
        return shockwaveModel;
    }

    /**
     * Ritorna la velocità massima raggiungibile dal corpo rigido in base ai vincoli del database.
     */
    @Override
    public double getTerminalVelocity() {
        return settings.maxVelocity;
    }

    /**
     * Ritorna l'istanza delle impostazioni associate a questo specifico modello.
     */
    public GenericEntitySettings getSettings() {
        return settings;
    }

    @Override
    public String getSpritePath() {
        return settings.spritePath;
    }

    @Override
    public int getSpriteFrameWidth() {
        return settings.frameW;
    }

    @Override
    public double getFrameDuration() {
        return UU.UNIVERSE_TICK * 6;
        // TODO: ADD ANIMATION DURATION TO SETTINGS
    }

    @Override
    public double getFrameDuration(int state, int frame) {
        return getFrameDuration();
    }

    @Override
    public int getSpriteFrameHeight() {
        return settings.frameW;
    }

    @Override
    public double getVisualScale() {
        return settings.scale;
    }

    @Override
    public double getVisualAngularOffsetDeg() {
        return 0;
    }

}