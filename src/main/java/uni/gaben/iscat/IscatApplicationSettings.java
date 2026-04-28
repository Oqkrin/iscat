package uni.gaben.iscat;

public class IscatApplicationSettings {
    /**
     * Approssimazione Rapporto Aureo
     * da utilizzare per proporzionare layout
     */
    public static final float PHI = 1.618F;
    /**
     * Approssimazione 1 / Rapporto Aureo
     * da utilizzare per proporzionare layout
     */
    public static final float IPHI = 0.618F;

    private static final IscatApplicationSettings instance = new IscatApplicationSettings();

    public static IscatApplicationSettings getInstance() { return instance; }

    private IscatApplicationSettings() {}
}
