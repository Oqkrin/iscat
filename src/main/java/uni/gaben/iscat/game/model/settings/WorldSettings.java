package uni.gaben.iscat.game.model.settings;

public final class WorldSettings {
    private WorldSettings() {}

    /** Costante gravitazionale */
    public static final double COSTANTE_GRAVITAZIONALE = 100.0;

    /** Larghezza mondo (px) - 0 = infinito */
    public static final double LARGHEZZA = 0;

    /** Altezza mondo (px) - 0 = infinito */
    public static final double ALTEZZA = 0;

    /**
     * Coefficiente di restituzione per collisioni (0=anelastica, 1=elastica perfetta).
     * Usato da CollisionPhysics.resolveElasticCollision().
     */
    public static final double COEFFICIENTE_RESTITUZIONE = 0.7;
}