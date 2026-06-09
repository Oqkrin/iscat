package uni.gaben.iscat.view.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import uni.gaben.iscat.utils.SessionManager;

public class IscatTitleBar extends StackPane {

    // Ability elements
    public final Button closeBtn      = titleBarButton("✕",  "title-bar-btn-close",      "Close");
    public final Button maximizeBtn   = titleBarButton("⬜",  "title-bar-btn-maximize",   "Maximize");
    public final Button fullscreenBtn = titleBarButton("⛶",  "title-bar-btn-fullscreen", "Fullscreen");
    public final Button minimizeBtn   = titleBarButton("—",  "title-bar-btn-minimize",   "Minimize");
    public final Button pinBtn        = titleBarButton("📌", "title-bar-btn-pin",        "Always on top");

    // Central Floating Title Label
    public final Label titleLabel     = new Label();

    public IscatTitleBar() {
        getStyleClass().add("title-bar");

        /* * CRITICAL FIX: Forces JavaFX to intercept mouse events across the entire
         * rectangular bounds of the StackPane, even on transparent zones.
         */
        setPickOnBounds(true);

        // LEFT FLOATING CAPSULE: Pin + Fullscreen
        HBox leftCapsule = new HBox(6, pinBtn, fullscreenBtn);
        leftCapsule.getStyleClass().add("floating-action-group");
        leftCapsule.setAlignment(Pos.CENTER_LEFT);

        // MIDDLE FLOATING CAPSULE: Styled Branding Label
        titleLabel.getStyleClass().add("title-bar-title");
        titleLabel.getStyleClass().add("label-small");
        HBox centerCapsule = new HBox(titleLabel);
        centerCapsule.getStyleClass().add("floating-title-capsule");
        centerCapsule.setAlignment(Pos.CENTER);
        centerCapsule.visibleProperty().bind(titleLabel.textProperty().isNotNull());

        // RIGHT FLOATING CAPSULE: Minimize + Maximize + Close
        HBox rightCapsule = new HBox(6, minimizeBtn, maximizeBtn, closeBtn);
        rightCapsule.getStyleClass().add("floating-action-group");
        rightCapsule.setAlignment(Pos.CENTER_RIGHT);

        // Prevent layout containers from forcing full-width expansion inside StackPane
        leftCapsule.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        centerCapsule.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        rightCapsule.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        // GEOMETRIC FIX: Explicitly align capsules to their absolute screen locations
        StackPane.setAlignment(leftCapsule, Pos.CENTER_LEFT);
        StackPane.setAlignment(centerCapsule, Pos.CENTER);
        StackPane.setAlignment(rightCapsule, Pos.CENTER_RIGHT);

        // Elegant breathing margins keeping capsules balanced off window edges
        StackPane.setMargin(leftCapsule, new Insets(0, 0, 0, 20));
        StackPane.setMargin(rightCapsule, new Insets(0, 20, 0, 20));
        StackPane.setMargin(centerCapsule, new Insets(10, 0, 0, 0));

        // Center capsule placed first in the scene graph layout tree
        getChildren().addAll(centerCapsule, leftCapsule, rightCapsule);
        titleLabel.textProperty().bind(SessionManager.getInstance().usernameProperty());
    }

    private static Button titleBarButton(String text, String styleClass, String tooltip) {
        Button btn = new Button(text);
        btn.getStyleClass().setAll("button", "title-bar-btn", styleClass);
        btn.setTooltip(new Tooltip(tooltip));
        btn.setFocusTraversable(false);
        return btn;
    }
}