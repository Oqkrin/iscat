package uni.gaben.iscat.screens.bestiary;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import uni.gaben.iscat.IscatNavigator;
import uni.gaben.iscat.view.AnimatedCanvas;
import uni.gaben.iscat.model.IscatViews;
import uni.gaben.iscat.controller.IscatFxmlController;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class BestiaryMenuController implements IscatFxmlController {

    private final BestiaryData bestiaryData = new BestiaryData();
    private Map<String, BestiaryData.Enemy> enemies = new LinkedHashMap<>();

    private static final double DISPLAY_SIZE = 160.0;

    @FXML private StackPane previewContainer;
    @FXML private Label skinNameLabel;
    @FXML private TextArea description;
    @FXML private VBox enemyButtonsBox;

    private StackPane contentRoot;
    private AnimatedCanvas previewCanvas;

    @FXML
    public void initialize() {
        previewCanvas = new AnimatedCanvas(DISPLAY_SIZE);
        previewContainer.getChildren().add(previewCanvas);

        description.setEditable(false);
        description.setWrapText(true);

        enemies = bestiaryData.loadEnemies();

        createEnemyButtons();

        if (!enemies.isEmpty()) {
            String firstEnemyId = enemies.keySet().iterator().next();
            showEnemyById(firstEnemyId);
        }
    }

    /**
     * Genera dinamicamente la lista dei pulsanti dei mostri,
     * applicando l'icona del teschio e il tween "Godot-style" per l'hover.
     */
    private void createEnemyButtons() {
        enemyButtonsBox.getChildren().clear();

        for (BestiaryData.Enemy enemy : enemies.values()) {
            String safeId = enemy.entityKey().trim();

            Button button = new Button(enemy.name());
            button.setPrefWidth(250.0);
            button.setPrefHeight(26.0);
            button.setId(safeId);

            setupButtonHoverTween(button);

            button.setOnAction(e -> showEnemyById(safeId));

            enemyButtonsBox.getChildren().add(button);
        }
    }

    /**
     * Aggiorna la vista con i dettagli del mostro selezionato
     * e fa scattare l'animazione di spawn sulla preview box.
     */
    private void showEnemyById(String id) {
        if (id == null) return;

        String cleanId = id.trim();
        BestiaryData.Enemy enemy = enemies.get(cleanId);

        if (enemy == null) {
            System.err.println("ERRORE BESTIARIO: Impossibile trovare il nemico con l'EntityKey '" + cleanId + "' nella mappa!");
            return;
        }

        skinNameLabel.setText(enemy.name().toUpperCase());
        description.setText(enemy.description());

        previewCanvas.setFrameDuration(0.20);
        previewCanvas.loadSkin(
                enemy.sprite(),
                enemy.frameW(),
                enemy.frameH()
        );

        previewCanvas.resize(DISPLAY_SIZE);

        playSpawnTween(previewContainer);
    }

    @FXML
    private void handleBack(ActionEvent event) {
        IscatNavigator.getInstance()
                .navigateWithFade(IscatViews.MAIN_MENU);
    }

    @FXML
    private void selectRandom() {
        var validIds = enemies.keySet().stream()
                .filter(id ->
                        !enemies.get(id)
                                .name()
                                .toUpperCase()
                                .equals(skinNameLabel.getText()))
                .toList();

        if (validIds.isEmpty()) return;

        String randomId = validIds.get(
                ThreadLocalRandom.current().nextInt(validIds.size())
        );

        showEnemyById(randomId);
    }

    @Override
    public void setContentRoot(StackPane contentRoot) {
        this.contentRoot = contentRoot;
    }
}