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
 * Model for the skin selection grid.
 * Anchors the selected item to its structural index coordinates and shifts its bounds
 * upstream if it encounters an edge boundary.
 */
public class SkinGridModel {

    private static final int DEFAULT_COLUMNS = 4;
    private static final int SELECTED_SPAN = 2;   // 2x2 multi-cell span

    private final ObservableList<EntityRecord> skins = FXCollections.observableArrayList();
    private final IntegerProperty columns = new SimpleIntegerProperty(DEFAULT_COLUMNS);
    private final StringProperty selectedKey = new SimpleStringProperty(null);
    private final ReadOnlyListWrapper<SkinPlacement> placements = new ReadOnlyListWrapper<>(FXCollections.observableArrayList());

    public SkinGridModel() {
        skins.addListener((ListChangeListener<EntityRecord>) c -> recomputeLayout());
        columns.addListener((obs, old, val) -> recomputeLayout());
        selectedKey.addListener((obs, old, val) -> recomputeLayout());
        recomputeLayout();
    }

    public ObservableList<EntityRecord> getSkins() { return skins; }
    public IntegerProperty columnsProperty() { return columns; }
    public int getColumns() { return columns.get(); }
    public void setColumns(int columns) { this.columns.set(columns); }
    public StringProperty selectedKeyProperty() { return selectedKey; }
    public String getSelectedKey() { return selectedKey.get(); }
    public void setSelectedKey(String key) { this.selectedKey.set(key); }

    public ReadOnlyListProperty<SkinPlacement> placementsProperty() { return placements.getReadOnlyProperty(); }
    public ObservableList<SkinPlacement> getPlacements() { return placements.get(); }

    private void recomputeLayout() {
        List<EntityRecord> skinList = new ArrayList<>(skins);
        int n = skinList.size();
        int cols = getColumns();
        if (n == 0 || cols <= 0) {
            placements.set(FXCollections.observableArrayList());
            return;
        }

        if (cols < SELECTED_SPAN) {
            cols = SELECTED_SPAN;
            this.columns.set(cols);
        }

        String selected = getSelectedKey();
        int selectedIndex = IntStream.range(0, n)
                .filter(i -> skinList.get(i).entityKey().equals(selected))
                .findFirst()
                .orElse(-1);

        // Precompute total spatial footprint requirement
        int extraCells = (selectedIndex >= 0) ? (SELECTED_SPAN * SELECTED_SPAN - 1) : 0;
        int totalCellsNeeded = n + extraCells;
        int rows = (int) Math.ceil((double) totalCellsNeeded / cols);
        if (selectedIndex >= 0 && rows < SELECTED_SPAN) {
            rows = SELECTED_SPAN;
        }

        int[][] occupancy = new int[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                occupancy[r][c] = -1;
            }
        }

        List<SkinPlacement> placementList = new ArrayList<>();

        java.util.function.BiConsumer<Integer, int[]> placeSkin = (index, span) -> {
            int row = span[0];
            int col = span[1];
            int rowSpan = span[2];
            int colSpan = span[3];
            for (int r = row; r < row + rowSpan; r++) {
                for (int c = col; c < col + colSpan; c++) {
                    occupancy[r][c] = index;
                }
            }
            placementList.add(new SkinPlacement(
                    index, skinList.get(index), row, col, rowSpan, colSpan, index == selectedIndex
            ));
        };

        // --- Step 1: Claim space for the expanded item based on its index position ---
        if (selectedIndex >= 0) {
            int naturalRow = selectedIndex / cols;
            int naturalCol = selectedIndex % cols;

            // Shift left if the 2x2 box spills over the right border
            if (naturalCol + SELECTED_SPAN > cols) {
                naturalCol = cols - SELECTED_SPAN;
            }
            // Shift up if the 2x2 box spills over the bottom border
            if (naturalRow + SELECTED_SPAN > rows) {
                naturalRow = rows - SELECTED_SPAN;
            }

            naturalRow = Math.max(0, naturalRow);
            naturalCol = Math.max(0, naturalCol);

            placeSkin.accept(selectedIndex, new int[]{naturalRow, naturalCol, SELECTED_SPAN, SELECTED_SPAN});
        }

        // --- Step 2: Route all remaining skins into empty slots ---
        for (int i = 0; i < n; i++) {
            if (i == selectedIndex) continue;
            boolean placed = false;
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    if (occupancy[r][c] == -1) {
                        placeSkin.accept(i, new int[]{r, c, 1, 1});
                        placed = true;
                        break;
                    }
                }
                if (placed) break;
            }
        }

        placements.set(FXCollections.observableArrayList(placementList));
    }

    public static class SkinPlacement {
        public final int index;
        public final EntityRecord record;
        public final int row;
        public final int col;
        public final int rowSpan;
        public final int colSpan;
        public final boolean selected;

        public SkinPlacement(int index, EntityRecord record, int row, int col, int rowSpan, int colSpan, boolean selected) {
            this.index = index;
            this.record = record;
            this.row = row;
            this.col = col;
            this.rowSpan = rowSpan;
            this.colSpan = colSpan;
            this.selected = selected;
        }
    }
}