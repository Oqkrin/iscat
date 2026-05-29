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

    private enum InfoMode {
        DESCRIPTION, STATS, EXTRA
    }

    private final BestiaryData bestiaryData = new BestiaryData();
    private Map<String, BestiaryData.Enemy> enemies = new LinkedHashMap<>();

    private static final double DISPLAY_SIZE = 160.0;
    private String currentEnemyId = null;
    private InfoMode currentInfoMode = InfoMode.DESCRIPTION;

    @FXML private StackPane previewContainer;
    @FXML private Label skinNameLabel;
    @FXML private Label rightCardHeader;
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
     * Carica il nemico mantenendo la tab informativa precedentemente scelta dall'utente.
     */
    private void showEnemyById(String id) {
        if (id == null) return;

        String cleanId = id.trim();
        BestiaryData.Enemy enemy = enemies.get(cleanId);

        if (enemy == null) {
            System.err.println("ERRORE BESTIARIO: Impossibile trovare il nemico con l'EntityKey '" + cleanId + "'!");
            return;
        }

        currentEnemyId = cleanId;
        skinNameLabel.setText(enemy.name().toUpperCase());

        refreshInfoZone();

        previewCanvas.setFrameDuration(0.20);
        previewCanvas.loadSkin(enemy.sprite(), enemy.frameW(), enemy.frameH());
        previewCanvas.resize(DISPLAY_SIZE);

        playSpawnTween(previewContainer);
    }

    /**
     * Metodo centralizzato che smista i dati e aggiorna i titoli in base alla modalità attiva.
     */
    private void refreshInfoZone() {
        if (currentEnemyId == null) return;
        BestiaryData.Enemy enemy = enemies.get(currentEnemyId);
        if (enemy == null) return;

        switch (currentInfoMode) {
            case DESCRIPTION -> {
                rightCardHeader.setText("DESCRIPTION!");
                description.setText(enemy.description());
            }
            case STATS -> {
                rightCardHeader.setText("STATS!");
                description.setText(String.format("""
                    === STATISTICHE DI BASE ===
                    
                    ❤ Punti Vita: %d HP
                    ⚡ Velocità Massima: %d m/s
                    ✨ Ricompensa Esperienza: %d XP
                    """,
                        enemy.initLife(),
                        enemy.maxVelocity(),
                        enemy.xpReward()
                ));
            }
            case EXTRA -> {
                rightCardHeader.setText("EXTRA INFO!");
                description.setText(String.format("""
                    === COMPORTAMENTO IA ===
                    
                    👁 Raggio di Avvistamento: %d unità
                    ⚔ Raggio di Combattimento: %d unità
                    ⏱ Cooldown Attacco: %d secondi
                    
                    ID Interno: %s
                    """,
                        enemy.detectionRange(),
                        enemy.combatRange(),
                        enemy.fireCooldownS(),
                        enemy.entityKey()
                ));
            }
        }
    }

    @FXML
    private void showDescription() {
        currentInfoMode = InfoMode.DESCRIPTION;
        refreshInfoZone();
    }

    @FXML
    private void showStats() {
        currentInfoMode = InfoMode.STATS;
        refreshInfoZone();
    }

    @FXML
    private void showExtra() {
        currentInfoMode = InfoMode.EXTRA;
        refreshInfoZone();
    }

    @FXML
    private void handleBack(ActionEvent event) {
        IscatNavigator.getInstance().navigateWithFade(IscatViews.MAIN_MENU);
    }

    @FXML
    private void selectRandom() {
        var validIds = enemies.keySet().stream()
                .filter(id -> !enemies.get(id).name().toUpperCase().equals(skinNameLabel.getText()))
                .toList();

        if (validIds.isEmpty()) return;

        String randomId = validIds.get(ThreadLocalRandom.current().nextInt(validIds.size()));
        showEnemyById(randomId);
    }

    @Override public void setContentRoot(StackPane contentRoot) { this.contentRoot = contentRoot; }

    private void setupButtonHoverTween(Button b) {}
    private void playSpawnTween(StackPane p) {}
}