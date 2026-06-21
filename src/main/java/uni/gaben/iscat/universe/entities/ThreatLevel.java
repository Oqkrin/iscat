package uni.gaben.iscat.universe.entities;

/**
 * Classificazione dei livelli di minaccia ed allerta delle entità di gioco (Threat Levels).
 * Associa a ogni livello una stringa descrittiva localizzata e un codice colore esadecimale
 * per la categorizzazione grafica immediata nella UI/HUD.
 */
public enum ThreatLevel {

    NONE("None", "#000000"),
    LOW("Low", "#00ff00"),
    NORMAL("Normal", "#00aaff"),
    HIGH("High", "#ffaa00"),
    EXTREME("Extreme", "#ff5500"),
    APOCALYPSE("APOCALYPSE", "#ff0000");

    private final String displayName;
    private final String colorHex;

    ThreatLevel(String displayName, String colorHex) {
        this.displayName = displayName;
        this.colorHex = colorHex;
    }

    /**
     * @return Il nome leggibile del livello di minaccia localizzato in italiano.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @return Il codice colore esadecimale (Hex CSS/JavaFX) associato alla minaccia.
     */
    public String getColorHex() {
        return colorHex;
    }
}