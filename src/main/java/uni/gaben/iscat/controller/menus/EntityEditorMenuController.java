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
import uni.gaben.iscat.model.EntityEditorModel;
import uni.gaben.iscat.model.IscatViews;
import uni.gaben.iscat.universe.entities.*;
import uni.gaben.iscat.universe.entities.json.EntityJsonLoader;
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

    @FXML private ComboBox<String> comboEntitySelect;
    @FXML private VBox paneIdentity, paneVisuals, panePhysics, paneBehavioural, paneAdvancedAI, paneAudio;
    @FXML private StackPane previewContainer;
    @FXML private Label skinNameLabel, lblOriginPath;
    @FXML private Button btnApply, btnSaveOverwrite, btnSaveNew, btnBack;
    @FXML private Button btnChooseSkin, btnChooseSound;

    private Path entitiesRoot;
    private StackPane viewRoot;
    private AnimatedCanvas previewCanvas;
    private EntityEditorModel model;
    private EntityEditorUIBuilder uiBuilder;

    public static String targetEntityKeyToLoad = null;

    @FXML
    public void initialize() {
        // Ensure the external entities root is available (auto‑create if missing)
        entitiesRoot = ExternalResourceResolver.getEntitiesRoot();
        if (entitiesRoot == null) {
            entitiesRoot = Path.of("entities");
            // Let the resolver know about it so other parts of the app can use it
            ExternalResourceResolver.init(entitiesRoot);
            System.out.println("[EntityEditor] Created default external root: " + entitiesRoot.toAbsolutePath());
        }

        previewCanvas = new AnimatedCanvas(196.0);
        previewContainer.getChildren().add(previewCanvas);

        model = new EntityEditorModel();
        uiBuilder = new EntityEditorUIBuilder(this::rebuildAllUI);

        ComponentsUtils.applyIconButton(btnBack, "fas-arrow-left");
        ComponentsUtils.applyIconButton(btnApply, "fas-play");
        ComponentsUtils.applyIconButton(btnSaveOverwrite, "fas-save");
        ComponentsUtils.applyIconButton(btnSaveNew, "fas-plus-circle");
        ComponentsUtils.applyIconButton(btnChooseSkin, "fas-image");
        ComponentsUtils.applyIconButton(btnChooseSound, "fas-volume-up");

        populateEntityCombo();

        if (targetEntityKeyToLoad != null) {
            comboEntitySelect.getSelectionModel().select(targetEntityKeyToLoad);
            loadEntity(targetEntityKeyToLoad);
            targetEntityKeyToLoad = null;
        } else if (!comboEntitySelect.getItems().isEmpty()) {
            comboEntitySelect.getSelectionModel().selectFirst();
            loadEntity(comboEntitySelect.getItems().get(0));
        }

        comboEntitySelect.setOnAction(e -> {
            String sel = comboEntitySelect.getSelectionModel().getSelectedItem();
            if (sel != null) loadEntity(sel);
        });

        registerEscHandler();
    }

    private void populateEntityCombo() {
        List<String> keys = new ArrayList<>(EntityFactory.getCache().keySet());
        keys.sort(String::compareToIgnoreCase);
        comboEntitySelect.getItems().setAll(keys);
    }

    private void loadEntity(String entityKey) {
        JSONObject raw = EntityFactory.getRawJson(entityKey);
        if (raw == null) {
            model.setCurrentJson(new JSONObject().put("entitykey", "new_entity"));
            model.setOriginPath(null);
        } else {
            model.setCurrentJson(EntityRecordParser.convertKeysToLowerCase(new JSONObject(raw.toString())));
            model.setOriginPath(EntityFactory.getOriginPath(entityKey));
        }
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

    // ── Action Handlers ─────────────────────────────────────────────────────

    @FXML
    private void handleApply() {
        try {
            EntityRecord record = EntityRecordParser.parse(model.getCurrentJson());
            EntityFactory.addOrUpdateEntity(record);
            System.out.println("Applied to runtime cache: " + record.entityKey());
            updatePreview();
        } catch (Exception e) {
            System.err.println("Failed to parse JSON: " + e.getMessage());
        }
    }

    @FXML
    private void handleSaveOverwrite() {
        String origin = model.getOriginPath();
        if (origin == null || origin.isEmpty()) {
            handleSaveNew();   // no known origin – treat as new custom
            return;
        }
        handleApply();

        Path originPath = Path.of(origin);
        if (Files.isRegularFile(originPath)) {
            EntityJsonLoader.saveJsonToFile(model.getCurrentJson(), origin);
            System.out.println("Overwrote file: " + origin);
            return;
        }

        // Origin is internal (classpath) – create an override in the core folder
        String fileName = originPath.getFileName().toString();
        Path coreDir = entitiesRoot.resolve("json/core");
        try {
            Files.createDirectories(coreDir);
        } catch (IOException e) {
            System.err.println("Cannot create core directory: " + e.getMessage());
            return;
        }
        Path overrideFile = coreDir.resolve(fileName);
        EntityJsonLoader.saveJsonToFile(model.getCurrentJson(), overrideFile.toAbsolutePath().toString());
        model.setOriginPath(overrideFile.toAbsolutePath().toString());
        lblOriginPath.setText("Origin: " + model.getOriginPath());
        System.out.println("Saved override to: " + overrideFile);
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
        EntityJsonLoader.saveJsonToFile(model.getCurrentJson(), targetFile.toAbsolutePath().toString());

        model.setOriginPath(targetFile.toAbsolutePath().toString());
        lblOriginPath.setText("Origin: " + model.getOriginPath());
        System.out.println("Saved new entity to: " + model.getOriginPath());

        if (!comboEntitySelect.getItems().contains(key)) {
            comboEntitySelect.getItems().add(key);
            comboEntitySelect.getSelectionModel().select(key);
        }
    }

    @FXML
    private void handleChooseSkin() {
        Path customDir = entitiesRoot.resolve("sprites/custom");
        try { Files.createDirectories(customDir); } catch (IOException e) {
            System.err.println("Cannot create custom sprite directory: " + e.getMessage());
            return;
        }

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
        try { Files.createDirectories(sfxCustomDir); } catch (IOException e) {
            System.err.println("Cannot create custom SFX directory: " + e.getMessage());
            return;
        }

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
                // Update the default audio field – you can expand this later
                model.getCurrentJson().put("stepsound", soundName);
                refreshUI();
            } catch (IOException e) {
                System.err.println("Failed to copy sound: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        handleBack();
    }

    public void handleBack() {
        IscatNavigator.getInstance().navigateWithFade(IscatViews.BESTIARY_MENU);
    }

    // ── ISCAT Menu Controller Interface ─────────────────────────────────────
    @Override
    public void setPointerToView(StackPane pointer) {
        this.viewRoot = pointer;
    }

    @Override
    public Pane getRootPane() {
        // Navigate up to the outermost container used as the root pane
        return (Pane) btnBack.getParent().getParent();
    }

    @Override
    public void registerEscHandler() {
        // Use the scene of the root pane when available
        if (getRootPane().getScene() != null) {
            getRootPane().getScene().setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    handleBack();
                }
            });
        } else {
            // If the scene is not yet ready, listen for it
            getRootPane().sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    newScene.setOnKeyPressed(event -> {
                        if (event.getCode() == KeyCode.ESCAPE) {
                            handleBack();
                        }
                    });
                }
            });
        }
    }
}