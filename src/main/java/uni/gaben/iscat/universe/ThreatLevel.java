package uni.gaben.iscat.universe;

public enum ThreatLevel {
    NONE("Nessuna", "#000000"),
    LOW("Bassa", "#00ff00"),
    NORMAL("Normale", "#00aaff"),
    HIGH("Alta", "#ffaa00"),
    EXTREME("Estrema", "#ff5500"),
    APOCALYPSE("Apocalisse", "#ff0000");

    private final String displayName;
    private final String colorHex;

    ThreatLevel(String displayName, String colorHex) {
        this.displayName = displayName;
        this.colorHex = colorHex;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColorHex() {
        return colorHex;
    }
}