package uni.gaben.iscat.model;

import org.json.JSONObject;

/**
 * Modello di stato per l'editor delle entità di gioco.
 * Mantiene in memoria la struttura dati JSON dell'entità attualmente in fase di modifica
 * e il percorso del file locale di origine per consentire le operazioni di salvataggio e sovrascrittura.
 */
public class EntityEditorModel {

    /** La struttura dati JSON contenente le proprietà e i parametri dell'entità corrente. */
    private JSONObject currentJson = new JSONObject();
    /** Il percorso assoluto o relativo del file sorgente da cui è stata caricata l'entità, o {@code null} se è nuova. */
    private String originPath;
    /** Stato Operazioni non salvate **/
    private boolean dirty;

    /**
     * Ritorna l'oggetto JSON dell'entità correntemente gestita dall'editor.
     *
     * @return L'istanza di {@link JSONObject} contenente i dati dell'entità.
     */
    public JSONObject getCurrentJson() { return currentJson; }
    /**
     * Aggiorna l'istanza del JSON dell'entità corrente.
     *
     * @param json Il nuovo {@link JSONObject} da associare al modello.
     */
    public void setCurrentJson(JSONObject json) { this.currentJson = json; }

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
     * @param path Il percorso stringa del file locale da associare al modello.
     */
    public void setOriginPath(String path) { this.originPath = path; }

    public boolean isDirty() { return dirty; }
    public void setDirty(boolean dirty) { this.dirty = dirty; }

    /** Call this whenever a field changes. */
    public void markDirty() { this.dirty = true; }

    /** Reset dirty flag (e.g. after save or load). */
    public void clearDirty() { this.dirty = false; }
}