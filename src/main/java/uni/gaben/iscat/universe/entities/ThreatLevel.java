package uni.gaben.iscat.universe.entities;

/**
 * Classificazione dei livelli di minaccia ed allerta delle entità di gioco.
 * Associa a ogni livello un nome visibile e un colore esadecimale per l'interfaccia.
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
     * Ritorna il nome da visualizzare per il livello di minaccia.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Ritorna il codice colore esadecimale associato alla minaccia.
     */
    public String getColorHex() {
        return colorHex;
    }
}