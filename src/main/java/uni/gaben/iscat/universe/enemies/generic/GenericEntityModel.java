package uni.gaben.iscat.universe.enemies.generic;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseCollisionLayers;
import uni.gaben.iscat.universe.lib.implementations.LivingEntityModel;
import uni.gaben.iscat.universe.player.PlayerModel;

/**
 * Modello polimorfico universale per la gestione logica e fisica dei nemici.
 * Sostituisce i vecchi modelli specifici (es. IscatMobModel, IscatCoreModel) delegando l'intera
 * configurazione geometrica, i coefficienti d'attrito, i parametri di movimento e le routine di danno
 * a un'istanza di {@link GenericEntitySettings} interamente popolata da database.
 */
public class GenericEntityModel extends LivingEntityModel {

    /** Archivio dei parametri strutturali e biologici caricati dal database per questa entità. */
    private final GenericEntitySettings settings;

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
        BodyFixture fixture = switch (settings.shapeType) {
            case CIRCLE -> addFixture(
                    Geometry.createCircle(
                            UU.pxToM(settings.dimSprite * settings.scale / 2.0 * 0.9)));
            case SQUARE -> addFixture(
                    Geometry.createSquare(
                            UU.pxToM(settings.dimSprite * settings.scale * 0.9)));
        };

        // Assegnazione del layer per la gestione selettiva dei contatti (es. ignora altri nemici, collide con player/proiettili)
        fixture.setFilter(UniverseCollisionLayers.ENEMY_FILTER);
        setMass(MassType.NORMAL);
        setLinearDamping(settings.dampingLineare);

        // Iniezione della logica di collisione per i nemici con attacco da sfondamento (RAM)
        if (settings.customParam1 > 0.0 || settings.customParam2 > 0.0) {
            final double heavyDamage = settings.customParam1;
            final double lightDamage = settings.customParam2;

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
}