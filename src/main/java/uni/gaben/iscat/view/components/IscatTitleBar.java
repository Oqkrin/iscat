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

/**
 * Barra del titolo personalizzata per le finestre dell'applicazione ISCAT.
 * Gestisce i pulsanti di controllo della finestra (chiusura, ridimensionamento, pinning)
 * e mostra dinamicamente il nome dell'utente in sessione al centro della barra.
 */
public class IscatTitleBar extends StackPane {

    /** Altezza predefinita in pixel della barra del titolo. */
    public static final int TITLE_BAR_HEIGHT = 56;

    /** Pulsante per la chiusura della finestra. */
    public final Button closeButton = titleBarButton("✕",  "title-bar-btn-close",      "Close");

    /** Pulsante per massimizzare la finestra. */
    public final Button maximizeButton = titleBarButton("⬜",  "title-bar-btn-maximize",   "Maximize");

    /** Pulsante per attivare la modalità a schermo intero. */
    public final Button fullscreenButton = titleBarButton("⛶",  "title-bar-btn-fullscreen", "Fullscreen");

    /** Pulsante per minimizzare la finestra nel vassoio di sistema. */
    public final Button minimizeButton = titleBarButton("—",  "title-bar-btn-minimize",   "Minimize");

    /** Pulsante per mantenere la finestra sempre in primo piano (pinning). */
    public final Button pinButton = titleBarButton("📌", "title-bar-btn-pin",        "Always on top");

    /** Etichetta testuale centrale legata al nome utente della sessione corrente. */
    public final Label titleLabel     = new Label();

    /**
     * Costruisce e inizializza la barra del titolo.
     * Configura i raggruppamenti di pulsanti laterali, la capsula centrale per il titolo
     * e applica i vincoli geometrici di allineamento e binding dei dati.
     */
    public IscatTitleBar() {
        getStyleClass().add("title-bar");

        setPickOnBounds(true);

        HBox leftCapsule = new HBox(6, pinButton, fullscreenButton);
        leftCapsule.getStyleClass().add("floating-action-group");
        leftCapsule.setAlignment(Pos.CENTER_LEFT);

        titleLabel.getStyleClass().add("title-bar-title");
        titleLabel.getStyleClass().add("label-small");
        HBox centerCapsule = new HBox(titleLabel);
        centerCapsule.getStyleClass().add("floating-title-capsule");
        centerCapsule.setAlignment(Pos.CENTER);
        centerCapsule.visibleProperty().bind(titleLabel.textProperty().isNotNull());

        HBox rightCapsule = new HBox(6, minimizeButton, maximizeButton, closeButton);
        rightCapsule.getStyleClass().add("floating-action-group");
        rightCapsule.setAlignment(Pos.CENTER_RIGHT);

        leftCapsule.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        centerCapsule.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        rightCapsule.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        StackPane.setAlignment(leftCapsule, Pos.CENTER_LEFT);
        StackPane.setAlignment(centerCapsule, Pos.CENTER);
        StackPane.setAlignment(rightCapsule, Pos.CENTER_RIGHT);

        StackPane.setMargin(leftCapsule, new Insets(0, 0, 0, 20));
        StackPane.setMargin(rightCapsule, new Insets(0, 20, 0, 20));
        StackPane.setMargin(centerCapsule, new Insets(10, 0, 0, 0));

        getChildren().addAll(centerCapsule, leftCapsule, rightCapsule);
        titleLabel.textProperty().bind(SessionManager.getInstance().usernameProperty());
    }

    /**
     * Helper statico per la creazione e la formattazione uniforme dei pulsanti della barra del titolo.
     */
    private static Button titleBarButton(String text, String styleClass, String tooltip) {
        Button btn = new Button(text);
        btn.getStyleClass().setAll("button", "title-bar-btn", styleClass);
        btn.setTooltip(new Tooltip(tooltip));
        btn.setFocusTraversable(false);
        return btn;
    }
}