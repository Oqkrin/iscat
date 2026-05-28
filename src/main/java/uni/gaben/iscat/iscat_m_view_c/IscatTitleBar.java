package uni.gaben.iscat.iscat_m_view_c;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import uni.gaben.iscat.iscat_screens.login.model.SessionUser;
import uni.gaben.iscat.utils.SessionManager;

public class IscatTitleBar extends HBox {

    // Action elements
    public final Button closeBtn      = titleBarButton("✕",  "title-bar-btn-close",      "Close");
    public final Button maximizeBtn   = titleBarButton("⬜",  "title-bar-btn-maximize",   "Maximize");
    public final Button fullscreenBtn = titleBarButton("⛶",  "title-bar-btn-fullscreen", "Fullscreen");
    public final Button minimizeBtn   = titleBarButton("—",  "title-bar-btn-minimize",   "Minimize");
    public final Button pinBtn        = titleBarButton("📌", "title-bar-btn-pin",        "Always on top");

    // Central Floating Title Label
    public final Label titleLabel     = new Label();

    public IscatTitleBar() {
        getStyleClass().add("title-bar");
        setAlignment(Pos.CENTER);

        // Generous breathing padding for floating offsets
        setPadding(new Insets(12, 20, 8, 20));

        /* * CRITICAL FIX: Forces JavaFX to intercept mouse events across the entire
         * rectangular bounds of the HBox, even on transparent sections and spacers.
         * This ensures window dragging works anywhere in the top 56px zone.
         */
        setPickOnBounds(true);

        // LEFT FLOATING CAPSULE: Pin + Fullscreen
        HBox leftCapsule = new HBox(4, pinBtn, fullscreenBtn);
        leftCapsule.getStyleClass().add("floating-action-group");
        leftCapsule.setAlignment(Pos.CENTER_LEFT);

        // MIDDLE FLOATING CAPSULE: Styled Branding Label
        titleLabel.getStyleClass().add("title-bar-title");
        HBox centerCapsule = new HBox(titleLabel);
        centerCapsule.getStyleClass().add("floating-title-capsule");
        centerCapsule.setAlignment(Pos.CENTER);

        // RIGHT FLOATING CAPSULE: Minimize + Maximize + Close
        HBox rightCapsule = new HBox(4, minimizeBtn, maximizeBtn, closeBtn);
        rightCapsule.getStyleClass().add("floating-action-group");
        rightCapsule.setAlignment(Pos.CENTER_RIGHT);

        // Flexible spacer layouts to keep the elements neatly split across screens
        Region leftSpacer  = new Region();
        Region rightSpacer = new Region();
        HBox.setHgrow(leftSpacer,  Priority.ALWAYS);
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);

        getChildren().addAll(leftCapsule, leftSpacer, centerCapsule, rightSpacer, rightCapsule);
        titleLabel.textProperty().bind(SessionManager.getInstance().usernameProperty());
    }

    private static Button titleBarButton(String text, String styleClass, String tooltip) {
        Button btn = new Button(text);
        btn.getStyleClass().setAll("title-bar-btn", styleClass);
        btn.setTooltip(new Tooltip(tooltip));
        btn.setFocusTraversable(false);
        return btn;
    }
}