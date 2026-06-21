package uni.gaben.iscat.controller.menus;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import org.json.JSONObject;
import uni.gaben.iscat.IscatNavigator;
import uni.gaben.iscat.controller.interfaces.IscatMenuController;
import uni.gaben.iscat.model.EntityEditorModel;
import uni.gaben.iscat.model.IscatViews;
import uni.gaben.iscat.universe.entities.*;
import uni.gaben.iscat.universe.entities.json.EntityJsonLoader;
import uni.gaben.iscat.utils.ComponentsUtils;
import uni.gaben.iscat.view.editor.EntityEditorUIBuilder;
import uni.gaben.iscat.view.components.AnimatedCanvas;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EntityEditorMenuController implements IscatMenuController {

    @FXML private ComboBox<String> comboEntitySelect;
    @FXML private VBox paneIdentity, paneVisuals, panePhysics, paneBehavioural, paneAdvancedAI, paneAudio;
    @FXML private StackPane previewContainer;
    @FXML private Label skinNameLabel, lblOriginPath;
    @FXML private Button btnApply, btnSaveOverwrite, btnSaveNew, btnBack;

    private StackPane viewRoot;
    private AnimatedCanvas previewCanvas;
    private EntityEditorModel model;
    private EntityEditorUIBuilder uiBuilder;

    // Static target for navigation (set before showing this screen)
    public static String targetEntityKeyToLoad = null;

    @FXML
    public void initialize() {
        previewCanvas = new AnimatedCanvas(196.0);
        previewContainer.getChildren().add(previewCanvas);

        model = new EntityEditorModel();
        uiBuilder = new EntityEditorUIBuilder(this::rebuildAllUI);

        // Apply icons to buttons
        ComponentsUtils.applyIconButton(btnBack, "fas-arrow-left");
        ComponentsUtils.applyIconButton(btnApply, "fas-play");
        ComponentsUtils.applyIconButton(btnSaveOverwrite, "fas-save");
        ComponentsUtils.applyIconButton(btnSaveNew, "fas-plus-circle");

        populateEntityCombo();

        // Load either the target entity or the first available
        if (targetEntityKeyToLoad != null) {
            comboEntitySelect.getSelectionModel().select(targetEntityKeyToLoad);
            loadEntity(targetEntityKeyToLoad);
            targetEntityKeyToLoad = null; // consume
        } else if (!comboEntitySelect.getItems().isEmpty()) {
            comboEntitySelect.getSelectionModel().selectFirst();
            loadEntity(comboEntitySelect.getItems().get(0));
        }

        // Listener for combo selection
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

    /**
     * Loads an entity from the factory cache and populates the model.
     */
    private void loadEntity(String entityKey) {
        JSONObject raw = EntityFactory.getRawJson(entityKey);
        if (raw == null) {
            // Create a blank template
            model.setCurrentJson(new JSONObject().put("entitykey", "new_entity"));
            model.setOriginPath(null);
        } else {
            // Deep copy and normalise keys to lowercase
            model.setCurrentJson(EntityRecordParser.convertKeysToLowerCase(new JSONObject(raw.toString())));
            model.setOriginPath(EntityFactory.getOriginPath(entityKey));
        }
        refreshUI();
    }

    /**
     * Refreshes all UI components (labels, form, preview) from the current model.
     */
    private void refreshUI() {
        JSONObject json = model.getCurrentJson();
        lblOriginPath.setText("Origin: " + (model.getOriginPath() != null ? model.getOriginPath() : "Unsaved/Custom"));
        skinNameLabel.setText(json.optString("name", "UNKNOWN"));
        rebuildAllUI();
        updatePreview();
    }

    /**
     * Rebuilds the entire form using the UI builder.
     */
    private void rebuildAllUI() {
        uiBuilder.buildUI(
                model.getCurrentJson(),
                paneIdentity, paneVisuals, panePhysics, paneBehavioural,
                paneAdvancedAI, paneAudio
        );
    }

    /**
     * Updates the preview canvas and the name label.
     */
    private void updatePreview() {
        JSONObject json = model.getCurrentJson();
        String spriteName = json.optString("spritename", "");
        if (spriteName.toLowerCase().endsWith(".png")) {
            spriteName = spriteName.substring(0, spriteName.length() - 4);
        }
        String folder = json.has("player") ? "players" : "enemies";
        if (!spriteName.isEmpty()) {
            String path = "/uni/gaben/iscat/sprites/" + folder + "/" + spriteName + ".png";
            previewCanvas.setFrameDuration(0.1);
            try {
                previewCanvas.loadSkin(path, json.optInt("framew", 32), json.optInt("frameh", 32));
                previewCanvas.resize(196.0);
            } catch (Exception e) {
                System.err.println("Could not load preview skin: " + path);
            }
        }
        skinNameLabel.setText(json.optString("name", "UNKNOWN"));
    }

    // ============================================================
    //  ACTION HANDLERS
    // ============================================================

    @FXML
    private void handleApply() {
        try {
            EntityRecord record = EntityRecordParser.parse(model.getCurrentJson());
            EntityFactory.addOrUpdateEntity(record);
            System.out.println("Applied to runtime cache: " + record.entityKey());
            updatePreview(); // ensure preview matches updated data
        } catch (Exception e) {
            System.err.println("Failed to parse JSON: " + e.getMessage());
        }
    }

    @FXML
    private void handleSaveOverwrite() {
        if (model.getOriginPath() == null || model.getOriginPath().isEmpty()) {
            System.err.println("Cannot overwrite: No origin path known. Use Save As New.");
            return;
        }
        handleApply(); // first apply to runtime
        EntityJsonLoader.saveJsonToFile(model.getCurrentJson(), model.getOriginPath());
        System.out.println("Overwrote file: " + model.getOriginPath());
    }

    @FXML
    private void handleSaveNew() {
        handleApply(); // apply to runtime first
        String key = model.getCurrentJson().optString("entitykey", "new_entity");
        String safeName = key.replaceAll("[^a-zA-Z0-9_\\-]", "_") + ".json";

        File customDir = new File("src/main/resources/uni/gaben/iscat/json/custom");
        if (!customDir.exists()) {
            customDir.mkdirs();
        }

        File targetFile = new File(customDir, safeName);
        EntityJsonLoader.saveJsonToFile(model.getCurrentJson(), targetFile.getAbsolutePath());

        model.setOriginPath(targetFile.getAbsolutePath());
        lblOriginPath.setText("Origin: " + model.getOriginPath());
        System.out.println("Saved new entity to: " + model.getOriginPath());

        // Add to combo if not already present
        if (!comboEntitySelect.getItems().contains(key)) {
            comboEntitySelect.getItems().add(key);
            comboEntitySelect.getSelectionModel().select(key);
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        handleBack();
    }

    public void handleBack() {
        IscatNavigator.getInstance().navigateWithFade(IscatViews.BESTIARY_MENU);
    }

    // ============================================================
    //  ESCAPE HANDLER (optional)
    // ============================================================

    public void registerEscHandler() {
        Pane root = getRootPane();
        if (root != null) {
            root.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    handleBack();
                }
            });
        }
    }

    // ============================================================
    //  ISCAT MENU CONTROLLER INTERFACE METHODS
    // ============================================================

    @Override
    public void setPointerToView(StackPane pointer) {
        this.viewRoot = pointer;
    }

    @Override
    public Pane getRootPane() {
        // Returns the top-level pane; assuming btnBack is inside the main VBox.
        return (Pane) btnBack.getParent().getParent();
    }
}