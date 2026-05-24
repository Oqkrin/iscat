package uni.gaben.iscat.iscat_m_view_c;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/**
 * The type Title bar.
 */
public class IscatTitleBar extends HBox {

    /**
     * The Close btn.
     */
    public final Button closeBtn      = makeBtn("✕",  "title-bar-btn-close",      "Close");
    /**
     * The Maximize btn.
     */
    public final Button maximizeBtn   = makeBtn("⬜",  "title-bar-btn-maximize",   "Maximize");
    /**
     * The Fullscreen btn.
     */
    public final Button fullscreenBtn = makeBtn("⛶",  "title-bar-btn-fullscreen", "Fullscreen");
    /**
     * The Minimize btn.
     */
    public final Button minimizeBtn   = makeBtn("—",  "title-bar-btn-minimize",   "Minimize");
    /**
     * The Pin btn.
     */
    public final Button pinBtn        = makeBtn("📌", "title-bar-btn-pin",        "Always on top");

    /**
     * Instantiates a new Title bar.
     */
    public IscatTitleBar() {
        getStyleClass().add("title-bar");
        setAlignment(Pos.CENTER);
        setPadding(new Insets(5, 10, 5, 10));

        HBox left  = new HBox(pinBtn);
        left.setAlignment(Pos.CENTER_LEFT);

        HBox center = new HBox(4, maximizeBtn, closeBtn, fullscreenBtn);
        center.setAlignment(Pos.CENTER);

        HBox right = new HBox(minimizeBtn);
        right.setAlignment(Pos.CENTER_RIGHT);

        Region leftSpacer  = new Region();
        Region rightSpacer = new Region();
        HBox.setHgrow(leftSpacer,  Priority.ALWAYS);
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);

        getChildren().addAll(left, leftSpacer, center, rightSpacer, right);
    }

    private static Button makeBtn(String text, String styleClass, String tooltip) {
        Button btn = new Button(text);
        btn.getStyleClass().setAll("title-bar-btn", styleClass);
        btn.setTooltip(new Tooltip(tooltip));
        btn.setFocusTraversable(false);
        return btn;
    }
}