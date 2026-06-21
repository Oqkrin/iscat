package uni.gaben.iscat.model;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import uni.gaben.iscat.universe.entities.EntityRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class SkinGridModel {

    public static final int SELECTED_SPAN = 2;   // 2×2 expanded item
    public static final int RANDOM_INDEX = -2;        // sentinel for the random cell
    public static final int NCOL = 5;

    private final ObservableList<EntityRecord> skins = FXCollections.observableArrayList();
    private final IntegerProperty columns = new SimpleIntegerProperty(NCOL);
    private final StringProperty selectedKey = new SimpleStringProperty(null);
    private final ReadOnlyListWrapper<SkinPlacement> placements =
            new ReadOnlyListWrapper<>(FXCollections.observableArrayList());

    public SkinGridModel() {
        skins.addListener((ListChangeListener<EntityRecord>) c -> recomputeLayout());
        columns.addListener((obs, old, val) -> recomputeLayout());
        selectedKey.addListener((obs, old, val) -> recomputeLayout());
        recomputeLayout();
    }

    public ObservableList<EntityRecord> getSkins() { return skins; }
    public IntegerProperty columnsProperty() { return columns; }
    public int getColumns() { return columns.get(); }
    public void setColumns(int cols) { columns.set(Math.max(cols, SELECTED_SPAN)); }
    public StringProperty selectedKeyProperty() { return selectedKey; }
    public String getSelectedKey() { return selectedKey.get(); }
    public void setSelectedKey(String key) { selectedKey.set(key); }

    public ReadOnlyListProperty<SkinPlacement> placementsProperty() {
        return placements.getReadOnlyProperty();
    }
    public ObservableList<SkinPlacement> getPlacements() { return placements.get(); }

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

    public record SkinPlacement(int index, EntityRecord record, int row, int col,
                                int rowSpan, int colSpan, boolean selected) {}
}