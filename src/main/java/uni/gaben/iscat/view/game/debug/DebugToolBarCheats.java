package uni.gaben.iscat.view.game.debug;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import uni.gaben.iscat.controller.game.GameController;
import uni.gaben.iscat.utils.design.CssHelper;

/**
 * Pannello dedicato all'attivazione dei trucchi e alterazione delle statistiche del giocatore.
 */
public class DebugToolBarCheats extends VBox {

    /**
     * Inizializza il pannello dei trucchi strutturando i bottoni per la gestione della salute,
     * delle modalità speciali (Godmode) e dei livelli del giocatore.
     *
     * @param controller Il controller di gioco corrente
     * @param onBack     Azione per tornare alla schermata precedente della toolbar
     */
    public DebugToolBarCheats(GameController controller, Runnable onBack) {
        super(12);
        setPadding(new Insets(12, 20, 20, 20));
        setAlignment(Pos.TOP_CENTER);

        Button btnBack = new Button("← INDIETRO");
        btnBack.setFocusTraversable(false);
        CssHelper.stilePulsanteMenu(btnBack);
        CssHelper.testoSecondario(btnBack);
        btnBack.getStyleClass().add("debug-btn-back");
        btnBack.setOnAction(e -> onBack.run());

        HBox topBar = new HBox(btnBack);
        topBar.setAlignment(Pos.TOP_LEFT);

        Label mainTitle = new Label("CHEAT MODIFIERS");
        CssHelper.testoPrimario(mainTitle);
        mainTitle.getStyleClass().add("debug-main-title");

        Label lblHealth = createSectionLabel("PLAYER HEALTH & DAMAGE");
        FlowPane healthFlow = new FlowPane(8, 8);
        healthFlow.setAlignment(Pos.CENTER);

        Button btnHeal100 = createCheatButton("HEAL 100", "color-heal");
        btnHeal100.setOnAction(e -> controller.debugHeal(100));
        Button btnHeal1000 = createCheatButton("HEAL 1000", "color-heal");
        btnHeal1000.setOnAction(e -> controller.debugHeal(1000));
        Button btnHeal10000 = createCheatButton("HEAL 10000", "color-heal");
        btnHeal10000.setOnAction(e -> controller.debugHeal(10000));

        Button btnDmg100 = createCheatButton("DAMAGE 100", "color-damage");
        btnDmg100.setOnAction(e -> controller.debugDamage(100));
        Button btnDmg1000 = createCheatButton("DAMAGE 1000", "color-damage");
        btnDmg1000.setOnAction(e -> controller.debugDamage(1000));
        Button btnDmg10000 = createCheatButton("DAMAGE 10000", "color-damage");
        btnDmg10000.setOnAction(e -> controller.debugDamage(10000));

        healthFlow.getChildren().addAll(btnHeal100, btnHeal1000, btnHeal10000, btnDmg100, btnDmg1000, btnDmg10000);

        Label lblStates = createSectionLabel("SPECIAL MODES");
        FlowPane statesFlow = new FlowPane(10, 10);
        statesFlow.setAlignment(Pos.CENTER);

        Button btnGod = createCheatButton("GODMODE", "color-god");
        btnGod.setOnAction(e -> controller.debugToggleGodMode());

        statesFlow.getChildren().addAll(btnGod);

        Label lblLevels = createSectionLabel("LEVEL PROGRESSION");
        HBox levelsBox = new HBox(12);
        levelsBox.setAlignment(Pos.CENTER);

        Button btnLvlUp = createCheatButton("LEVEL UP ▲", "color-level-up");
        btnLvlUp.setOnAction(e -> controller.debugLevelUp());

        Button btnLvlDown = createCheatButton("LEVEL DOWN ▼", "color-level-down");
        btnLvlDown.setOnAction(e -> controller.debugLevelDown());

        levelsBox.getChildren().addAll(btnLvlUp, btnLvlDown);

        getChildren().addAll(topBar, mainTitle, lblHealth, healthFlow, lblStates, statesFlow, lblLevels, levelsBox);
    }

    private Button createCheatButton(String text, String colorStyleClass) {
        Button btn = new Button(text);
        btn.setFocusTraversable(false);
        btn.setPrefHeight(32);
        CssHelper.stilePulsanteMenu(btn);
        CssHelper.testoPrimario(btn);
        btn.getStyleClass().addAll("cheat-button", colorStyleClass);
        return btn;
    }

    private Label createSectionLabel(String text) {
        Label lbl = new Label(text);
        CssHelper.testoSecondario(lbl);
        lbl.getStyleClass().add("debug-section-label");
        return lbl;
    }
}