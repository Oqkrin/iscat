package uni.gaben.iscat.model;

import org.json.JSONObject;

/**
 * Modello di stato per l'editor delle entità di gioco.
 * Mantiene in memoria la struttura dati JSON dell'entità attualmente in fase di modifica
 * e il percorso del file locale di origine per consentire le operazioni di salvataggio e sovrascrittura.
 */
public class EntityEditorModel {

    /** La struttura dati JSON contenente le proprietà e i parametri dell'entità corrente. */
    private JSONObject currentJson;

    /** Il percorso assoluto o relativo del file sorgente da cui è stata caricata l'entità, o {@code null} se è nuova. */
    private String originPath;

    /**
     * Costruttore della classe. Inizializza un modello vuoto configurando un nuovo
     * {@link JSONObject} privo di proprietà e impostando il percorso di origine a {@code null}.
     */
    public EntityEditorModel() {
        this.currentJson = new JSONObject();
        this.originPath = null;
    }

    /**
     * Ritorna l'oggetto JSON dell'entità correntemente gestita dall'editor.
     *
     * @return L'istanza di {@link JSONObject} contenente i dati dell'entità.
     */
    public JSONObject getCurrentJson() { return currentJson; }

    /**
     * Aggiorna l'istanza del JSON dell'entità corrente.
     *
     * @param currentJson Il nuovo {@link JSONObject} da associare al modello.
     */
    public void setCurrentJson(JSONObject currentJson) { this.currentJson = currentJson; }

    /**
     * Ritorna il percorso del file di origine su disco dell'entità corrente.
     *
     * @return Una stringa contenente il percorso del file sorgente, oppure {@code null}
     * se l'entità non è ancora stata salvata o associata a un file esistente.
     */
    public String getOriginPath() { return originPath; }

    /**
     * Imposta il percorso del file di origine dell'entità su disco.
     *
     * @param originPath Il percorso stringa del file locale da associare al modello.
     */
    public void setOriginPath(String originPath) { this.originPath = originPath; }
}