package uni.gaben.iscat.universe.enemies.generic;

import uni.gaben.iscat.universe.lib.abstracts.EntitySettings;

/**
 * Estende {@link EntitySettings} per mappare i campi aggiuntivi della tabella 'Entita'.
 * Funge da Data Transfer Object (DTO) per la configurazione dinamica dei nemici caricati dal database,
 * isolando i dati di identità, i metadati grafici della skin, i parametri fisici di collisione
 * e il profilo di comportamento dell'Intelligenza Artificiale.
 */
public class GenericEntitySettings extends EntitySettings {

    /** Chiave identificativa univoca del tipo di nemico (es. "iscat_mob"). */
    public String entityKey = "";

    /** Nome visualizzato nell'interfaccia di gioco e nel bestiario. */
    public String name = "";

    /** Testo di lore ed analisi biologica mostrato nel bestiario. */
    public String description = "";

    /** Percorso della risorsa grafica interna (file PNG dello spritesheet). */
    public String spritePath = "";

    /** Larghezza in pixel del singolo frame dell'animazione. */
    public int frameW = 32;

    /** Altezza in pixel del singolo frame dell'animazione. */
    public int frameH = 32;

    /** Tipologia geometrica della fixture fisica da applicare al corpo dell'entità. */
    public ShapeType shapeType = ShapeType.CIRCLE;

    /** Profilo comportamentale ("Brain") da assegnare all'algoritmo di movimento e attacco dell'IA. */
    public BehaviorType behaviorType = BehaviorType.WANDER_SHOOT;

    /**
     * Definisce le geometrie primitive supportate per il corpo rigido (Collider) del motore fisico.
     */
    public enum ShapeType {
        CIRCLE,
        SQUARE;

        /**
         * Converte in modo sicuro una stringa nella costante enumerativa corrispondente.
         * In caso di valore nullo o non riconosciuto, applica il fallback di sicurezza su CIRCLE.
         */
        public static ShapeType fromString(String s) {
            if (s == null) return CIRCLE;
            return switch (s.trim().toUpperCase()) {
                case "SQUARE" -> SQUARE;
                default       -> CIRCLE;
            };
        }
    }

    /**
     * Specifica le tipologie di comportamento e pattern d'attacco disponibili per la logica dell'IA.
     */
    public enum BehaviorType {
        WANDER_SHOOT,
        RAM,
        IDLE;

        /**
         * Converte in modo sicuro una stringa nella costante enumerativa corrispondente.
         * In caso di valore nullo o non riconosciuto, applica il fallback di sicurezza su WANDER_SHOOT.
         */
        public static BehaviorType fromString(String s) {
            if (s == null) return WANDER_SHOOT;
            return switch (s.trim().toUpperCase()) {
                case "RAM"  -> RAM;
                case "IDLE" -> IDLE;
                default     -> WANDER_SHOOT;
            };
        }
    }
}