package uni.gaben.iscat.model;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import uni.gaben.iscat.universe.entities.EntityRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Modello matematico e reattivo per la disposizione a griglia delle skin dei personaggi.
 * Gestisce l'algoritmo di riposizionamento dinamico degli elementi (Layout Computing)
 * all'interno di una matrice bidimensionale, supportando l'espansione di un elemento selezionato
 * in formato 2x2 e l'inserimento di una cella speciale per la selezione casuale (Random).
 */
public class SkinGridModel {

    /** Dimensione (sia in righe che in colonne) dell'elemento selezionato ed espanso. */
    public static final int SELECTED_SPAN = 2;

    /** Indice sentinella utilizzato per identificare la cella speciale di selezione casuale. */
    public static final int RANDOM_INDEX = -2;

    /** Numero predefinito di colonne della griglia (valore di default). */
    public static final int NCOL = 5;

    /** Lista osservabile contenente i record delle skin caricate nel modello. */
    private final ObservableList<EntityRecord> skins = FXCollections.observableArrayList();

    /** Proprietà intera che traccia il numero corrente di colonne della griglia. */
    private final IntegerProperty columns = new SimpleIntegerProperty(NCOL);

    /** Proprietà stringa contenente la chiave identificativa dell'entità attualmente selezionata ed espansa. */
    private final StringProperty selectedKey = new SimpleStringProperty(null);

    /** Wrapper in sola lettura per la lista osservabile dei posizionamenti calcolati. */
    private final ReadOnlyListWrapper<SkinPlacement> placements =
            new ReadOnlyListWrapper<>(FXCollections.observableArrayList());

    /**
     * Costruttore del modello. Configura i listener reattivi sulle modifiche della lista di skin,
     * sul mutamento del numero di colonne e sul cambio di selezione, in modo da ricalcolare
     * automaticamente la disposizione geometrica della griglia a ogni variazione.
     */
    public SkinGridModel() {
        skins.addListener((ListChangeListener<EntityRecord>) c -> recomputeLayout());
        columns.addListener((obs, old, val) -> recomputeLayout());
        selectedKey.addListener((obs, old, val) -> recomputeLayout());
        recomputeLayout();
    }

    /** @return La lista osservabile delle skin (entità) registrate nel modello. */
    public ObservableList<EntityRecord> getSkins() { return skins; }

    /** @return La proprietà JavaFX legata al numero di colonne della griglia. */
    public IntegerProperty columnsProperty() { return columns; }

    /** @return Il numero corrente di colonne impostato per il layout. */
    public int getColumns() { return columns.get(); }

    /** * Imposta il numero di colonne della griglia. Il valore viene normalizzato affinché
     * non sia mai inferiore allo spazio minimo richiesto dall'elemento espanso.
     * * @param cols Il numero desiderato di colonne.
     */
    public void setColumns(int cols) { columns.set(Math.max(cols, SELECTED_SPAN)); }

    /** @return La proprietà JavaFX associata alla chiave dell'elemento selezionato. */
    public StringProperty selectedKeyProperty() { return selectedKey; }

    /** @return La chiave stringa dell'entità correntemente selezionata, o {@code null}. */
    public String getSelectedKey() { return selectedKey.get(); }

    /** @param key La chiave stringa dell'entità da selezionare ed espandere. */
    public void setSelectedKey(String key) { selectedKey.set(key); }

    /** @return La proprietà in sola lettura della lista dei posizionamenti calcolati per il binding della View. */
    public ReadOnlyListProperty<SkinPlacement> placementsProperty() {
        return placements.getReadOnlyProperty();
    }

    /** @return La lista osservabile contenente le direttive strutturali di posizionamento delle celle. */
    public ObservableList<SkinPlacement> getPlacements() { return placements.get(); }

    /**
     * Esegue l'algoritmo di calcolo e disposizione geometrica della griglia (Backtracking/Greedy Fill).
     * <p>
     * L'algoritmo mappa i posizionamenti in tre fasi sequenziali:
     * <ol>
     * <li>Posiziona l'elemento selezionato espandendolo in un blocco 2x2 basato sul suo indice naturale.</li>
     * <li>Distribuisce le restanti skin in formato standard 1x1 riempiendo i vuoti da sinistra a destra, dall'alto in basso.</li>
     * <li>Alloca la cella speciale "Random" nel primo slot rimasto disponibile all'interno della matrice di occupazione.</li>
     * </ol>
     */
    private void recomputeLayout() {
        List<EntityRecord> skinList = new ArrayList<>(skins);
        int n = skinList.size();
        int cols = getColumns();
        if (n == 0 || cols < SELECTED_SPAN) {
            placements.set(FXCollections.observableArrayList());
            return;
        }

        String selected = getSelectedKey();
        int selectedIndex = IntStream.range(0, n)
                .filter(i -> skinList.get(i).entityKey().equals(selected))
                .findFirst()
                .orElse(-1);

        // total cells = all 1×1 skins + 3 (expanded item loses 1 cell) + 1 (random)
        int totalCells = n + (SELECTED_SPAN * SELECTED_SPAN - 1) + 1;
        int rows = (int) Math.ceil((double) totalCells / cols);
        rows = Math.max(rows, SELECTED_SPAN);   // at least 2 rows for the expanded item

        int[][] occupancy = new int[rows][cols];
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                occupancy[r][c] = -1;   // -1 = empty

        List<SkinPlacement> placementList = new ArrayList<>();

        // --- 1. Place expanded (selected) item ---
        if (selectedIndex >= 0) {
            int naturalRow = selectedIndex / cols;
            int naturalCol = selectedIndex % cols;
            if (naturalCol + SELECTED_SPAN > cols) naturalCol = cols - SELECTED_SPAN;
            if (naturalRow + SELECTED_SPAN > rows) naturalRow = rows - SELECTED_SPAN;

            for (int r = naturalRow; r < naturalRow + SELECTED_SPAN; r++)
                for (int c = naturalCol; c < naturalCol + SELECTED_SPAN; c++)
                    occupancy[r][c] = selectedIndex;

            placementList.add(new SkinPlacement(
                    selectedIndex, skinList.get(selectedIndex),
                    naturalRow, naturalCol, SELECTED_SPAN, SELECTED_SPAN, true));
        }

        // --- 2. Fill remaining skins ---
        for (int i = 0; i < n; i++) {
            if (i == selectedIndex) continue;
            boolean placed = false;
            for (int r = 0; r < rows && !placed; r++) {
                for (int c = 0; c < cols; c++) {
                    if (occupancy[r][c] == -1) {
                        occupancy[r][c] = i;
                        placementList.add(new SkinPlacement(i, skinList.get(i), r, c, 1, 1, false));
                        placed = true;
                        break;
                    }
                }
            }
        }

        // --- 3. Place the random cell in the first empty spot ---
        boolean randomPlaced = false;
        for (int r = 0; r < rows && !randomPlaced; r++) {
            for (int c = 0; c < cols; c++) {
                if (occupancy[r][c] == -1) {
                    placementList.add(new SkinPlacement(RANDOM_INDEX, null, r, c, 1, 1, false));
                    randomPlaced = true;
                    break;
                }
            }
        }

        placements.set(FXCollections.observableArrayList(placementList));
    }

    /**
     * Record immutabile che definisce le coordinate e i parametri geometrici di piazzamento
     * di una singola cella all'interno del contenitore grafico a griglia.
     *
     * @param index    L'indice numerico d'ordine dell'elemento nella lista globale originale.
     * @param record   Il riferimento al rispettivo {@link EntityRecord} (nullo per la cella casuale).
     * @param row      La coordinata della riga d'origine (Y) nella matrice.
     * @param col      La coordinata della colonna d'origine (X) nella matrice.
     * @param rowSpan  Il numero di righe occupate verticalmente dalla cella.
     * @param colSpan  Il numero di colonne occupate orizzontalmente dalla cella.
     * @param selected {@code true} se la cella rappresenta l'elemento attualmente espanso e selezionato.
     */
    public record SkinPlacement(int index, EntityRecord record, int row, int col,
                                int rowSpan, int colSpan, boolean selected) {}
}