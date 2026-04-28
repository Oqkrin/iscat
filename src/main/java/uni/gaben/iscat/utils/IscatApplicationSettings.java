package uni.gaben.iscat.utils;

public class IscatApplicationSettings {
    private static final IscatApplicationSettings instance = new IscatApplicationSettings();
    public static IscatApplicationSettings getInstance() { return instance; }
    private IscatApplicationSettings() {}
}