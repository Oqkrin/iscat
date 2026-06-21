package uni.gaben.iscat.view.skin;

import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import uni.gaben.iscat.IscatSettings;
import uni.gaben.iscat.model.SkinGridModel;
import uni.gaben.iscat.model.SkinGridModel.SkinPlacement;
import uni.gaben.iscat.universe.entities.parsed.EntityRecord;
import uni.gaben.iscat.utils.ComponentsUtils;

import java.util.function.Consumer;

public class SkinGrid extends GridPane {
    private final SkinGridModel model;
    private final Consumer<String> onSkinSelected;
    private final Runnable onRandom;

    private boolean rebuilding = false;

    public SkinGrid(SkinGridModel model,
                    Consumer<String> onSkinSelected,
                    Runnable onRandom) {
        this.model = model;
        this.onSkinSelected = onSkinSelected;
        this.onRandom = onRandom;

        // Apply the same gaps as in the original FXML
        setHgap(IscatSettings.STANDARD_UNIT);
        setVgap(IscatSettings.STANDARD_UNIT);
        setAlignment(Pos.CENTER);

        // React to model changes
        model.placementsProperty().addListener((obs, old, placements) -> rebuild());

        // Wait for valid size before first rebuild
        widthProperty().addListener((obs, o, n) -> {
            if (n.doubleValue() > 0) adjustColumns();
            if (n.doubleValue() > 0 && getHeight() > 0) rebuild();
        });
        heightProperty().addListener((obs, o, n) -> {
            if (n.doubleValue() > 0 && getWidth() > 0) rebuild();
        });
    }

    /** Recalculates the number of columns based on the grid’s width. */
    private void adjustColumns() {
        double w = getWidth();
        if (w <= 0) return;
        model.setColumns(SkinGridModel.NCOL);
    }

    /** Completely rebuilds the grid content from the current placements. */
    private void rebuild() {
        if (rebuilding) return;

        ObservableList<SkinPlacement> placements = model.getPlacements();
        if (placements.isEmpty() || getWidth() <= 0 || getHeight() <= 0) {
            return;
        }

        rebuilding = true;

        getChildren().clear();
        getColumnConstraints().clear();
        getRowConstraints().clear();

        // Determine grid dimensions
        int maxRow = 0, maxCol = 0;
        for (SkinPlacement p : placements) {
            int endRow = p.row() + p.rowSpan();
            int endCol = p.col() + p.colSpan();
            if (endRow > maxRow) maxRow = endRow;
            if (endCol > maxCol) maxCol = endCol;
        }

        double hGap = getHgap();
        double vGap = getVgap();
        double availWidth  = getWidth()  - (maxCol - 1) * hGap;
        double availHeight = getHeight() - (maxRow - 1) * vGap;
        double cellW = Math.max(1, availWidth / maxCol);
        double cellH = Math.max(1, availHeight / maxRow);
        double cellSize = Math.min(cellW, cellH);

        // Row & column constraints
        for (int r = 0; r < maxRow; r++) {
            RowConstraints row = new RowConstraints(cellSize, cellSize, cellSize);
            row.setFillHeight(true);
            getRowConstraints().add(row);
        }
        for (int c = 0; c < maxCol; c++) {
            ColumnConstraints col = new ColumnConstraints(cellSize, cellSize, cellSize);
            col.setFillWidth(true);
            getColumnConstraints().add(col);
        }

        // Add buttons
        for (SkinPlacement p : placements) {
            placeButton(p, cellSize, hGap, vGap);
        }

        rebuilding = false;
    }

    private void placeButton(SkinPlacement p, double cellSize, double hGap, double vGap) {
        if (p.index() == SkinGridModel.RANDOM_INDEX) {
            Button randomButton = createRandomButton(cellSize, cellSize);
            add(randomButton, p.col(), p.row(), p.colSpan(), p.rowSpan());
            GridPane.setHgrow(randomButton, Priority.ALWAYS);
            GridPane.setVgrow(randomButton, Priority.ALWAYS);
        } else {
            double currentCellW = cellSize * p.colSpan() + (p.colSpan() - 1) * hGap;
            double currentCellH = cellSize * p.rowSpan() + (p.rowSpan() - 1) * vGap;
            SkinButton btn = createSkinButton(p.record(), currentCellW, currentCellH);
            if (p.selected()) {
                btn.getStyleClass().add("selected-skin-button");
            }
            add(btn, p.col(), p.row(), p.colSpan(), p.rowSpan());
            GridPane.setHgrow(btn, Priority.ALWAYS);
            GridPane.setVgrow(btn, Priority.ALWAYS);
        }
    }

    private Button createRandomButton(double width, double height) {
        Button button = new Button();
        button.getStyleClass().add("skin-button");
        button.setMinSize(width, height);
        button.setPrefSize(width, height);
        button.setMaxSize(width, height);
        ComponentsUtils.applyIconButton(button, "fas-dice");
        button.setOnAction(e -> onRandom.run());
        button.setFocusTraversable(false);
        return button;
    }

    private SkinButton createSkinButton(EntityRecord skin, double width, double height) {
        SkinButton button = new SkinButton(skin, width, height);
        button.setOnAction(e -> onSkinSelected.accept(skin.entityKey()));
        return button;
    }
}