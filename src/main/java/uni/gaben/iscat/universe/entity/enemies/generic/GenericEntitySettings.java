package uni.gaben.iscat.universe.entity.enemies.generic;

import uni.gaben.iscat.universe.entity.PhysicalEntitySettings;

/**
 * Estende {@link PhysicalEntitySettings} per mappare i campi aggiuntivi della tabella 'Entity'.
 * Funge da Data Transfer Object (DTO) per la configurazione dinamica dei nemici caricati dal database,
 * isolando i dati di identità, i metadati grafici della skin, i parametri fisici di collisione
 * e il profilo di comportamento dell'Intelligenza Artificiale.
 * 
 * Mappa 1:1 con la tabella Entity del database SQLite.
 */
public class GenericEntitySettings extends PhysicalEntitySettings {

    // Identity fields
    /** Chiave identificativa univoca del tipo di nemico (es. "iscat_mob"). */
    public String entityKey = "";

    /** Nome visualizzato nell'interfaccia di gioco e nel bestiario. */
    public String name = "";

    /** Testo di lore ed analisi biologica mostrato nel bestiario. */
    public String description = "";

    // Visual properties
    /** Percorso della risorsa grafica interna (file PNG dello spritesheet). */
    public String spritePath = "";

    /** Larghezza in pixel del singolo frame dell'animazione. */
    public int frameW = 32;

    /** Altezza in pixel del singolo frame dell'animazione. */
    public int frameH = 32;


}