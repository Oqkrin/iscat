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
import java.util.stream.Collectors;

public class EntityEditorMenuController implements IscatMenuController {

    // Old 3-combo fields removed; now using 2-combo approach
    @FXML private ComboBox<String> comboCategory;   // Enemies / Players / Custom
    @FXML private ComboBox<String> comboEntity;     // filtered list of entity keys

    @FXML private VBox paneIdentity, paneVisuals, panePhysics, paneBehavioural, paneAdvancedAI, paneAudio;
    @FXML private StackPane previewContainer;
    @FXML private Label skinNameLabel, lblOriginPath;
    @FXML private Button btnApply, btnSaveOverwrite, btnSaveNew, btnBack;
    @FXML private Button btnChooseSkin, btnChooseSound;

    @FXML private StackPane confirmOverlay;
    @FXML private ConfirmationOverlayController confirmOverlayController;

    private static final String CAT_ENEMIES = "Enemies";
    private static final String CAT_PLAYERS = "Players";
    private static final String CAT_CUSTOM  = "Custom";

    private Path entitiesRoot;
    private StackPane viewRoot;
    private AnimatedCanvas previewCanvas;
    private EntityEditorModel model;
    private EntityEditorUIBuilder uiBuilder;
    private boolean isUpdatingCombos = false;

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

        ComponentsUtils.applyIconButton(btnBack,         "fas-arrow-left");
        ComponentsUtils.applyIconButton(btnApply,        "fas-play");
        ComponentsUtils.applyIconButton(btnSaveOverwrite,"fas-save");
        ComponentsUtils.applyIconButton(btnSaveNew,      "fas-plus-circle");
        ComponentsUtils.applyIconButton(btnChooseSkin,   "fas-image");
        ComponentsUtils.applyIconButton(btnChooseSound,  "fas-volume-up");

        // Populate category combo
        comboCategory.getItems().setAll(CAT_ENEMIES, CAT_PLAYERS, CAT_CUSTOM);
        comboCategory.setOnAction(e -> onCategoryChanged());

        // Wire entity combo
        comboEntity.setOnAction(e -> {
            if (isUpdatingCombos) return;
            String sel = comboEntity.getSelectionModel().getSelectedItem();
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
        });

        // Decide starting state
        if (targetEntityKeyToLoad != null) {
            String keyToLoad = targetEntityKeyToLoad;
            targetEntityKeyToLoad = null;
            selectAndLoadEntity(keyToLoad);
        } else {
            // Default to enemies category
            comboCategory.getSelectionModel().select(CAT_ENEMIES);
            onCategoryChanged();
            if (!comboEntity.getItems().isEmpty()) {
                loadEntity(comboEntity.getItems().getFirst());
            }
        }

        registerEscHandler();
    }

    /** Called when the category combo changes. Rebuilds the entity list. */
    private void onCategoryChanged() {
        String cat = comboCategory.getValue();
        if (cat == null) return;

        isUpdatingCombos = true;
        List<String> keys = getEntityKeysForCategory(cat);
        keys.sort(String::compareToIgnoreCase);
        comboEntity.getItems().setAll(keys);
        if (!keys.isEmpty()) {
            comboEntity.getSelectionModel().selectFirst();
        } else {
            comboEntity.getSelectionModel().clearSelection();
        }
        isUpdatingCombos = false;

        // Auto-load first entity in new category
        if (!keys.isEmpty()) {
            loadEntity(keys.getFirst());
        }
    }

    /** Returns all entity keys belonging to a category. */
    private List<String> getEntityKeysForCategory(String cat) {
        return EntityFactory.getCache().entrySet().stream()
                .filter(e -> e.getValue() != null)
                .filter(e -> {
                    EntityRecord rec = e.getValue();
                    boolean isPlayer = rec.player() != null;
                    boolean isCustom  = isCustom(e.getKey());
                    return switch (cat) {
                        case CAT_PLAYERS -> isPlayer;
                        case CAT_CUSTOM  -> isCustom && !isPlayer;
                        case CAT_ENEMIES -> !isPlayer && !isCustom;
                        default          -> false;
                    };
                })
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private boolean isCustom(String key) {
        String origin = EntityFactory.getOriginPath(key);
        return origin != null && origin.toLowerCase().contains("custom");
    }

    /** Navigate to the right category and select the entity in the entity combo. */
    private void selectAndLoadEntity(String key) {
        String cat = categoryOf(key);
        isUpdatingCombos = true;
        comboCategory.getSelectionModel().select(cat);
        List<String> keys = getEntityKeysForCategory(cat);
        keys.sort(String::compareToIgnoreCase);
        comboEntity.getItems().setAll(keys);
        comboEntity.getSelectionModel().select(key);
        isUpdatingCombos = false;
        loadEntity(key);
    }

    private String categoryOf(String key) {
        EntityRecord rec = EntityFactory.getCache().get(key);
        if (rec != null && rec.player() != null) return CAT_PLAYERS;
        if (isCustom(key)) return CAT_CUSTOM;
        return CAT_ENEMIES;
    }

    private void loadEntity(String entityKey) {
        JSONObject raw = EntityFactory.getRawJson(entityKey);
        if (raw == null) {
            // New blank entity
            JSONObject newJson = new JSONObject();
            newJson.put("entitykey", "new_entity");
            newJson.put("ai", new JSONObject());
            newJson.put("audio", new JSONObject());
            model.setCurrentJson(newJson);
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
        lblOriginPath.setText("Origin: " + (model.getOriginPath() != null ? model.getOriginPath() : "Unsaved/New"));
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
                refreshEntityListInCurrentCategory();
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
        Runnable doSave = () -> {
            handleApply();
            EntityJsonLoader.saveJsonToFile(model.getCurrentJson(), overrideFile.toAbsolutePath().toString());
            model.setOriginPath(overrideFile.toAbsolutePath().toString());
            lblOriginPath.setText("Origin: " + model.getOriginPath());
            model.clearDirty();
            refreshEntityListInCurrentCategory();
        };
        if (Files.exists(overrideFile)) {
            confirmOverwrite(overrideFile, doSave);
        } else {
            doSave.run();
        }
    }

    @FXML
    private void handleSaveNew() {
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
        // 1. Write to disk
        EntityJsonLoader.saveJsonToFile(model.getCurrentJson(), targetFile.toAbsolutePath().toString());
        // 2. Update origin
        model.setOriginPath(targetFile.toAbsolutePath().toString());
        // 3. Push into raw cache so the entity is immediately recognized as custom
        JSONObject savedJson = model.getCurrentJson();
        EntityFactory.registerRawJson(key, savedJson, targetFile.toAbsolutePath().toString());
        // 4. Also apply runtime record
        handleApply();
        model.clearDirty();
        lblOriginPath.setText("Origin: " + model.getOriginPath());
        // 5. Refresh UI – switch to Custom category and select the key
        isUpdatingCombos = true;
        comboCategory.getSelectionModel().select(CAT_CUSTOM);
        List<String> customKeys = getEntityKeysForCategory(CAT_CUSTOM);
        customKeys.sort(String::compareToIgnoreCase);
        comboEntity.getItems().setAll(customKeys);
        comboEntity.getSelectionModel().select(key);
        isUpdatingCombos = false;
    }

    /** Rebuilds the entity list for whichever category is currently selected. */
    private void refreshEntityListInCurrentCategory() {
        String cat = comboCategory.getValue();
        if (cat == null) return;
        isUpdatingCombos = true;
        List<String> keys = getEntityKeysForCategory(cat);
        keys.sort(String::compareToIgnoreCase);
        comboEntity.getItems().setAll(keys);
        isUpdatingCombos = false;
    }

    private void confirmOverwrite(Path filePath, Runnable onYes) {
        if (confirmOverlayController == null) { onYes.run(); return; }
        String fileName = filePath.getFileName().toString();
        confirmOverlayController.ask(
                "Overwrite?",
                "File \"" + fileName + "\" already exists.\nOverwrite it?",
                onYes
        );
    }

    @FXML
    private void handleChooseSkin() {
        Path customDir = entitiesRoot.resolve("sprites/custom");
        try { Files.createDirectories(customDir); } catch (IOException e) { /* ok */ }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Sprite Sheet");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Images", "*.png"));
        File initialDir = customDir.toFile();
        if (initialDir.exists()) chooser.setInitialDirectory(initialDir);

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
                model.markDirty();
                refreshUI();
            } catch (IOException e) {
                System.err.println("Failed to copy sprite: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleChooseSound() {
        Path sfxCustomDir = entitiesRoot.resolve("audio/SFX/custom");
        try { Files.createDirectories(sfxCustomDir); } catch (IOException e) { /* ok */ }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Sound Effect");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("WAV Files", "*.wav"));
        File initialDir = sfxCustomDir.toFile();
        if (initialDir.exists()) chooser.setInitialDirectory(initialDir);

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
                model.markDirty();
                refreshUI();
            } catch (IOException e) {
                System.err.println("Failed to copy sound: " + e.getMessage());
            }
        }
    }

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