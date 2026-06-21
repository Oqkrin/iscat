package uni.gaben.iscat.controller.menus;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import org.json.JSONObject;
import uni.gaben.iscat.IscatNavigator;
import uni.gaben.iscat.controller.interfaces.IscatMenuController;
import uni.gaben.iscat.controller.components.ConfirmationOverlayController;
import uni.gaben.iscat.model.EntityEditorModel;
import uni.gaben.iscat.model.IscatViews;
import uni.gaben.iscat.universe.entities.parsed.EntityFactory;
import uni.gaben.iscat.universe.entities.parsed.EntityJsonLoader;
import uni.gaben.iscat.universe.entities.parsed.EntityRecord;
import uni.gaben.iscat.universe.entities.parsed.EntityRecordParser;
import uni.gaben.iscat.utils.ComponentsUtils;
import uni.gaben.iscat.utils.ExternalResourceResolver;
import uni.gaben.iscat.utils.sprite.SpriteResolver;
import uni.gaben.iscat.view.editor.EntityEditorUIBuilder;
import uni.gaben.iscat.view.components.AnimatedCanvas;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.*;

public class EntityEditorMenuController implements IscatMenuController {

    @FXML private ComboBox<String> comboCustom;
    @FXML private ComboBox<String> comboCore;
    @FXML private ComboBox<String> comboPlayers;

    @FXML private VBox paneIdentity, paneVisuals, panePhysics, paneBehavioural, paneAdvancedAI, paneAudio;
    @FXML private StackPane previewContainer;
    @FXML private Label skinNameLabel, lblOriginPath;
    @FXML private Button btnApply, btnSaveOverwrite, btnSaveNew, btnBack;
    @FXML private Button btnChooseSkin, btnChooseSound;

    @FXML private StackPane confirmOverlay;
    @FXML private ConfirmationOverlayController confirmOverlayController;

    private Path entitiesRoot;
    private StackPane viewRoot;
    private AnimatedCanvas previewCanvas;
    private EntityEditorModel model;
    private EntityEditorUIBuilder uiBuilder;

    public static String targetEntityKeyToLoad = null;

    @FXML
    public void initialize() {
        entitiesRoot = ExternalResourceResolver.getEntitiesRoot();
        if (entitiesRoot == null) {
            entitiesRoot = Path.of("entities");
            ExternalResourceResolver.init(entitiesRoot);
        }

        previewCanvas = new AnimatedCanvas(196.0);
        previewContainer.getChildren().add(previewCanvas);

        model = new EntityEditorModel();
        uiBuilder = new EntityEditorUIBuilder(() -> {
            model.markDirty();
            rebuildAllUI();
        });

        ComponentsUtils.applyIconButton(btnBack, "fas-arrow-left");
        ComponentsUtils.applyIconButton(btnApply, "fas-play");
        ComponentsUtils.applyIconButton(btnSaveOverwrite, "fas-save");
        ComponentsUtils.applyIconButton(btnSaveNew, "fas-plus-circle");
        ComponentsUtils.applyIconButton(btnChooseSkin, "fas-image");
        ComponentsUtils.applyIconButton(btnChooseSound, "fas-volume-up");

        populateCombos();

        if (targetEntityKeyToLoad != null) {
            selectEntityInCombo(targetEntityKeyToLoad);
            loadEntity(targetEntityKeyToLoad);
            targetEntityKeyToLoad = null;
        } else {
            loadFirstAvailable();
        }

        comboCustom.setOnAction(e -> handleComboSelection(comboCustom));
        comboCore.setOnAction(e -> handleComboSelection(comboCore));
        comboPlayers.setOnAction(e -> handleComboSelection(comboPlayers));

        registerEscHandler();
    }

    // ------------------------------------------------------------------------
    // Combo population & filtering
    // ------------------------------------------------------------------------
    private void populateCombos() {
        List<String> customKeys = new ArrayList<>();
        List<String> coreKeys = new ArrayList<>();
        List<String> playerKeys = new ArrayList<>();

        for (Map.Entry<String, EntityRecord> entry : EntityFactory.getCache().entrySet()) {
            String key = entry.getKey();
            EntityRecord rec = entry.getValue();
            boolean isPlayer = rec != null && rec.player() != null;
            boolean isCustom = isCustom(key);

            if (isPlayer) playerKeys.add(key);
            else if (isCustom) customKeys.add(key);
            else coreKeys.add(key);
        }

        sortAndSet(comboCustom, customKeys);
        sortAndSet(comboCore, coreKeys);
        sortAndSet(comboPlayers, playerKeys);
    }

    private boolean isCustom(String key) {
        String origin = EntityFactory.getOriginPath(key);
        return origin != null && origin.toLowerCase().contains("custom");
    }

    private void sortAndSet(ComboBox<String> combo, List<String> list) {
        list.sort(String::compareToIgnoreCase);
        combo.getItems().setAll(list);
        if (!list.isEmpty()) combo.getSelectionModel().selectFirst();
    }

    private void loadFirstAvailable() {
        if (!comboCustom.getItems().isEmpty()) loadEntity(comboCustom.getValue());
        else if (!comboCore.getItems().isEmpty()) loadEntity(comboCore.getValue());
        else if (!comboPlayers.getItems().isEmpty()) loadEntity(comboPlayers.getValue());
    }

    private void selectEntityInCombo(String key) {
        if (comboCustom.getItems().contains(key)) comboCustom.getSelectionModel().select(key);
        else if (comboCore.getItems().contains(key)) comboCore.getSelectionModel().select(key);
        else if (comboPlayers.getItems().contains(key)) comboPlayers.getSelectionModel().select(key);
    }

    private void handleComboSelection(ComboBox<String> combo) {
        String sel = combo.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        if (model.isDirty() && confirmOverlayController != null) {
            confirmOverlayController.ask(
                    "Unsaved Changes",
                    "You have unsaved modifications. Discard them?",
                    () -> loadEntity(sel)
            );
        } else {
            loadEntity(sel);
        }
    }

    // ------------------------------------------------------------------------
    // Entity loading & UI refresh
    // ------------------------------------------------------------------------
    private void loadEntity(String entityKey) {
        JSONObject raw = EntityFactory.getRawJson(entityKey);
        if (raw == null) {
            model.setCurrentJson(new JSONObject().put("entitykey", "new_entity"));
            model.setOriginPath(null);
        } else {
            model.setCurrentJson(EntityRecordParser.convertKeysToLowerCase(new JSONObject(raw.toString())));
            model.setOriginPath(EntityFactory.getOriginPath(entityKey));
        }
        model.clearDirty();
        refreshUI();
    }

    private void refreshUI() {
        JSONObject json = model.getCurrentJson();
        lblOriginPath.setText("Origin: " + (model.getOriginPath() != null ? model.getOriginPath() : "Unsaved/Custom"));
        skinNameLabel.setText(json.optString("name", "UNKNOWN"));
        rebuildAllUI();
        updatePreview();
    }

    private void rebuildAllUI() {
        uiBuilder.buildUI(model.getCurrentJson(),
                paneIdentity, paneVisuals, panePhysics, paneBehavioural, paneAdvancedAI, paneAudio);
    }

    private void updatePreview() {
        JSONObject json = model.getCurrentJson();
        String spriteName = json.optString("spritename", "");
        if (spriteName.toLowerCase().endsWith(".png")) {
            spriteName = spriteName.substring(0, spriteName.length() - 4);
        }
        String folder = json.has("player") ? "players" : "enemies";
        if (!spriteName.isEmpty()) {
            InputStream is = SpriteResolver.resolve(folder, spriteName);
            if (is != null) {
                previewCanvas.setFrameDuration(0.1);
                try {
                    previewCanvas.loadSkin(is, json.optInt("framew", 32), json.optInt("frameh", 32));
                    previewCanvas.resize(196.0);
                } catch (Exception e) {
                    System.err.println("Could not load preview skin: " + spriteName);
                }
            } else {
                System.err.println("Preview sprite not found: " + spriteName);
            }
        }
        skinNameLabel.setText(json.optString("name", "UNKNOWN"));
    }

    // ------------------------------------------------------------------------
    // Save & Apply logic
    // ------------------------------------------------------------------------
    @FXML
    private void handleApply() {
        try {
            EntityRecord record = EntityRecordParser.parse(model.getCurrentJson());
            EntityFactory.addOrUpdateEntity(record);
            model.clearDirty();
            refreshUI();
        } catch (Exception e) {
            System.err.println("Failed to parse JSON: " + e.getMessage());
        }
    }

    @FXML
    private void handleSaveOverwrite() {
        String origin = model.getOriginPath();
        if (origin == null || origin.isEmpty()) {
            handleSaveNew();
            return;
        }
        Path originPath = Path.of(origin);
        if (Files.isRegularFile(originPath)) {
            confirmOverwrite(originPath, () -> {
                handleApply();
                EntityJsonLoader.saveJsonToFile(model.getCurrentJson(), origin);
                model.clearDirty();
                repopulateCombosIfNeeded();
            });
            return;
        }

        // Origin is internal (classpath) – create an override in core folder
        String fileName = originPath.getFileName().toString();
        Path coreDir = entitiesRoot.resolve("json/core");
        try {
            Files.createDirectories(coreDir);
        } catch (IOException e) {
            System.err.println("Cannot create core directory: " + e.getMessage());
            return;
        }
        Path overrideFile = coreDir.resolve(fileName);
        if (Files.exists(overrideFile)) {
            confirmOverwrite(overrideFile, () -> {
                handleApply();
                EntityJsonLoader.saveJsonToFile(model.getCurrentJson(), overrideFile.toAbsolutePath().toString());
                model.setOriginPath(overrideFile.toAbsolutePath().toString());
                lblOriginPath.setText("Origin: " + model.getOriginPath());
                model.clearDirty();
                repopulateCombosIfNeeded();
            });
        } else {
            handleApply();
            EntityJsonLoader.saveJsonToFile(model.getCurrentJson(), overrideFile.toAbsolutePath().toString());
            model.setOriginPath(overrideFile.toAbsolutePath().toString());
            lblOriginPath.setText("Origin: " + model.getOriginPath());
            model.clearDirty();
            repopulateCombosIfNeeded();
        }
    }

    @FXML
    private void handleSaveNew() {
        handleApply();
        String key = model.getCurrentJson().optString("entitykey", "new_entity");
        String safeName = key.replaceAll("[^a-zA-Z0-9_\\-]", "_") + ".json";

        Path customDir = entitiesRoot.resolve("json/custom");
        try {
            Files.createDirectories(customDir);
        } catch (IOException e) {
            System.err.println("Cannot create custom directory: " + e.getMessage());
            return;
        }

        Path targetFile = customDir.resolve(safeName);
        if (Files.exists(targetFile)) {
            confirmOverwrite(targetFile, () -> saveNewToFile(targetFile, key));
        } else {
            saveNewToFile(targetFile, key);
        }
    }

    private void saveNewToFile(Path targetFile, String key) {
        EntityJsonLoader.saveJsonToFile(model.getCurrentJson(), targetFile.toAbsolutePath().toString());
        model.setOriginPath(targetFile.toAbsolutePath().toString());
        lblOriginPath.setText("Origin: " + model.getOriginPath());
        model.clearDirty();
        populateCombos();
        selectEntityInCombo(key);
    }

    private void repopulateCombosIfNeeded() { populateCombos(); }

    private void confirmOverwrite(Path filePath, Runnable onYes) {
        if (confirmOverlayController == null) {
            onYes.run();
            return;
        }
        String fileName = filePath.getFileName().toString();
        confirmOverlayController.ask(
                "Overwrite?",
                "File \"" + fileName + "\" already exists.\nOverwrite it?",
                onYes
        );
    }

    // ------------------------------------------------------------------------
    // Asset pickers
    // ------------------------------------------------------------------------
    @FXML
    private void handleChooseSkin() {
        Path customDir = entitiesRoot.resolve("sprites/custom");
        try { Files.createDirectories(customDir); } catch (IOException e) { /*...*/ }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Sprite Sheet");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Images", "*.png"));
        chooser.setInitialDirectory(customDir.toFile());

        File selected = chooser.showOpenDialog(getRootPane().getScene().getWindow());
        if (selected != null) {
            try {
                String folder = model.getCurrentJson().has("player") ? "players" : "enemies";
                Path targetDir = entitiesRoot.resolve("sprites/custom/" + folder);
                Files.createDirectories(targetDir);
                Path dest = targetDir.resolve(selected.getName());
                Files.copy(selected.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);

                String spriteName = selected.getName();
                if (spriteName.toLowerCase().endsWith(".png")) {
                    spriteName = spriteName.substring(0, spriteName.length() - 4);
                }
                model.getCurrentJson().put("spritename", spriteName);
                refreshUI();
            } catch (IOException e) {
                System.err.println("Failed to copy sprite: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleChooseSound() {
        Path sfxCustomDir = entitiesRoot.resolve("audio/SFX/custom");
        try { Files.createDirectories(sfxCustomDir); } catch (IOException e) { /*...*/ }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Sound Effect");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("WAV Files", "*.wav"));
        chooser.setInitialDirectory(sfxCustomDir.toFile());

        File selected = chooser.showOpenDialog(getRootPane().getScene().getWindow());
        if (selected != null) {
            try {
                Path dest = sfxCustomDir.resolve(selected.getName());
                Files.copy(selected.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
                String soundName = selected.getName();
                if (soundName.toLowerCase().endsWith(".wav")) {
                    soundName = soundName.substring(0, soundName.length() - 4);
                }
                model.getCurrentJson().put("stepsound", soundName);
                refreshUI();
            } catch (IOException e) {
                System.err.println("Failed to copy sound: " + e.getMessage());
            }
        }
    }

    // ------------------------------------------------------------------------
    // Navigation & life‑cycle
    // ------------------------------------------------------------------------
    @FXML private void handleBack(ActionEvent event) { handleBack(); }

    public void handleBack() {
        if (model.isDirty() && confirmOverlayController != null) {
            confirmOverlayController.ask(
                    "Unsaved Changes", "Go back without saving?",
                    () -> IscatNavigator.getInstance().navigateWithFade(IscatViews.BESTIARY_MENU)
            );
        } else {
            IscatNavigator.getInstance().navigateWithFade(IscatViews.BESTIARY_MENU);
        }
    }

    @Override public void setPointerToView(StackPane pointer) { this.viewRoot = pointer; }
    @Override public Pane getRootPane() { return (Pane) btnBack.getParent().getParent(); }

    @Override public void registerEscHandler() {
        if (getRootPane().getScene() != null) {
            getRootPane().getScene().setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ESCAPE) handleBack();
            });
        } else {
            getRootPane().sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    newScene.setOnKeyPressed(event -> {
                        if (event.getCode() == KeyCode.ESCAPE) handleBack();
                    });
                }
            });
        }
    }
}