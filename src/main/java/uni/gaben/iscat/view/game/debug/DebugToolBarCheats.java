package uni.gaben.iscat.view.game.debug;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import uni.gaben.iscat.IscatSettings;
import uni.gaben.iscat.controller.game.GameController;
import uni.gaben.iscat.utils.design.CssHelper;

/**
 * Pannello dedicato all'attivazione dei trucchi e alterazione delle statistiche del giocatore.
 */
public class DebugToolBarCheats extends VBox {

    public DebugToolBarCheats(GameController controller, Runnable onBack) {
        final double SU = IscatSettings.STANDARD_UNIT;

        setPadding(new Insets(SU /2));
        setAlignment(Pos.TOP_CENTER);
        setPickOnBounds(false);
        setFocusTraversable(false);
        setSpacing(SU /2);

        Button btnBack = new Button("← INDIETRO");
        btnBack.setFocusTraversable(false);
        CssHelper.stilePulsanteMenu(btnBack);
        CssHelper.testoSecondario(btnBack);
        btnBack.getStyleClass().add("debug-btn-back");
        btnBack.setPadding(new Insets(SU /4, SU /2, SU /4, SU /2));
        btnBack.setOnAction(e -> onBack.run());

        HBox topBar = new HBox(btnBack);
        topBar.setAlignment(Pos.TOP_LEFT);

        Label mainTitle = new Label("CHEAT MODIFIERS");
        CssHelper.testoPrimario(mainTitle);
        mainTitle.getStyleClass().add("debug-main-title");
        mainTitle.setPadding(new Insets(SU /2, 0, SU /4, 0));

        // Health & Damage
        Label lblHealth = createSectionLabel("PLAYER HEALTH & DAMAGE");
        FlowPane healthFlow = new FlowPane(SU /2, SU /2);
        healthFlow.setAlignment(Pos.CENTER);

        healthFlow.getChildren().addAll(
                createCheatButton("HEAL 100",   "color-heal",    () -> controller.debugHeal(100)),
                createCheatButton("HEAL 1000",  "color-heal",    () -> controller.debugHeal(1000)),
                createCheatButton("HEAL 10000", "color-heal",    () -> controller.debugHeal(10000)),
                createCheatButton("DAMAGE 100",   "color-damage",  () -> controller.debugDamage(100)),
                createCheatButton("DAMAGE 1000",  "color-damage",  () -> controller.debugDamage(1000)),
                createCheatButton("DAMAGE 10000", "color-damage",  () -> controller.debugDamage(10000))
        );

        // Special modes
        Label lblStates = createSectionLabel("SPECIAL MODES");
        FlowPane statesFlow = new FlowPane(SU /2, SU /2);
        statesFlow.setAlignment(Pos.CENTER);
        statesFlow.getChildren().add(
                createCheatButton("GODMODE", "color-god", controller::debugToggleGodMode)
        );

        // Level progression
        Label lblLevels = createSectionLabel("LEVEL PROGRESSION");
        HBox levelsBox = new HBox(SU * 0.75);
        levelsBox.setAlignment(Pos.CENTER);
        levelsBox.getChildren().addAll(
                createCheatButton("LEVEL UP ▲",   "color-level-up", controller::debugLevelUp),
                createCheatButton("LEVEL DOWN ▼", "color-level-down", controller::debugLevelDown)
        );

        getChildren().addAll(topBar, mainTitle, lblHealth, healthFlow, lblStates, statesFlow, lblLevels, levelsBox);
    }

    private Button createCheatButton(String text, String colorStyleClass, Runnable action) {
        final double SU = IscatSettings.STANDARD_UNIT;
        Button btn = new Button(text);
        btn.setFocusTraversable(false);
        CssHelper.stilePulsanteMenu(btn);
        CssHelper.testoPrimario(btn);
        btn.getStyleClass().addAll("cheat-button", colorStyleClass);
        btn.setPadding(new Insets(SU /4, SU /2, SU /4, SU /2));
        btn.setOnAction(e -> action.run());
        return btn;
    }

    private Label createSectionLabel(String text) {
        Label lbl = new Label(text);
        CssHelper.testoSecondario(lbl);
        lbl.getStyleClass().add("debug-section-label");
        lbl.setPadding(new Insets(IscatSettings.STANDARD_UNIT /4, 0, IscatSettings.STANDARD_UNIT /4, 0));
        return lbl;
    }
}