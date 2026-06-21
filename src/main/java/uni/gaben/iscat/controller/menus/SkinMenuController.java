package uni.gaben.iscat.controller.menus;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import uni.gaben.iscat.controller.components.InfoCardController;
import uni.gaben.iscat.controller.interfaces.IscatMenuController;
import uni.gaben.iscat.database.IscatDB;
import uni.gaben.iscat.model.SkinGridModel;
import uni.gaben.iscat.model.user.SessionUser;
import uni.gaben.iscat.universe.entities.parsed.EntityFactory;
import uni.gaben.iscat.universe.entities.parsed.EntityRecord;
import uni.gaben.iscat.utils.ComponentsUtils;
import uni.gaben.iscat.utils.SessionManager;
import uni.gaben.iscat.view.skin.SkinGrid;

import java.util.*;

public class SkinMenuController implements IscatMenuController {

    @FXML public VBox skinsVbox;
    @FXML private StackPane skinStackPane;
    @FXML private Button confirm;
    @FXML private Button cancel;
    @FXML private InfoCardController infoCardController;

    private final SkinGridModel gridModel = new SkinGridModel();
    private final Random rng = new Random();

    private SkinGrid skinGridComponent;   // the new custom view
    private StackPane contentRoot;

    private String selectedSkinPath;
    private String selectedSkinKey = "player1";

    @FXML
    public void initialize() {
        ComponentsUtils.applyIconButton(confirm, "fas-check");
        ComponentsUtils.applyIconButton(cancel, "fas-arrow-left");

        loadSkinsFromJson();

        // Build and insert the custom skin grid
        skinGridComponent = new SkinGrid(
                gridModel,
                this::onSkinSelected,
                this::selectRandom
        );
        skinsVbox.getChildren().addFirst(skinGridComponent);
        VBox.setVgrow(skinGridComponent, Priority.ALWAYS);

        // React to selection changes (info panel update)
        gridModel.selectedKeyProperty().addListener((obs, oldKey, newKey) -> {
            if (newKey != null && !newKey.equals(oldKey)) {
                refreshInfoZone();
            }
        });

        preselectSkin();
        registerEscHandler();
    }

    private void loadSkinsFromJson() {
        List<EntityRecord> skins = new ArrayList<>();
        Map<String, EntityRecord> globalCache = EntityFactory.getCache();
        globalCache.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .sorted(Comparator.comparing(entry -> entry.getValue().bestiaryOrder()))
                .forEach(entry -> {
                    String key = entry.getKey().toLowerCase().trim();
                    if (entry.getValue().player() != null || key.contains("player")) {
                        skins.add(entry.getValue());
                    }
                });
        gridModel.getSkins().setAll(skins);
    }

    private void onSkinSelected(String key) {
        EntityRecord skin = EntityFactory.getCache().get(key);
        if (skin != null) {
            selectSkin(skin.entityKey(), skin.spritePath(), skin.name());
        }
    }

    private void selectSkin(String key, String path, String name) {
        this.selectedSkinKey = key;
        this.selectedSkinPath = path;
        gridModel.setSelectedKey(key);
        ComponentsUtils.playSpawnTween(skinGridComponent);
    }

    private void selectRandom() {
        List<EntityRecord> skins = gridModel.getSkins();
        if (skins.isEmpty()) return;
        EntityRecord randomSkin;
        do {
            randomSkin = skins.get(rng.nextInt(skins.size()));
        } while (randomSkin.spritePath().equals(selectedSkinPath) && skins.size() > 1);
        selectSkin(randomSkin.entityKey(), randomSkin.spritePath(), randomSkin.name());
    }

    private void preselectSkin() {
        String currentKey = SessionManager.getPlayerSkinKey();
        EntityRecord current = EntityFactory.getCache().get(currentKey);
        if (current != null) {
            selectSkin(current.entityKey(), current.spritePath(), current.name());
        } else if (!gridModel.getSkins().isEmpty()) {
            EntityRecord first = gridModel.getSkins().getFirst();
            selectSkin(first.entityKey(), first.spritePath(), first.name());
        } else {
            this.selectedSkinKey = "player1";
            this.selectedSkinPath = "/uni/gaben/iscat/sprites/players/player1.png";
        }
    }

    /* ---------- Info card ---------- */
    private void refreshInfoZone() {
        if (selectedSkinKey == null || infoCardController == null) return;
        EntityRecord selected = EntityFactory.getCache().get(selectedSkinKey);
        if (selected == null) {
            infoCardController.updateInfo("N/A", "Nessun dato caricato per " + selectedSkinKey);
        } else {
            infoCardController.updateEntityInfo(selected);
        }
    }

    @FXML
    private void handleConfirm(ActionEvent event) {
        if (selectedSkinPath != null && selectedSkinKey != null) {
            SessionManager.setPlayerSkin(selectedSkinPath);
            SessionManager.setPlayerSkinKey(selectedSkinKey);
            SessionUser user = SessionManager.getInstance().getCurrentUser();
            if (user != null) {
                IscatDB.getInstance().executeAsync(() ->
                        IscatDB.getInstance().getSettingsDAO().updatePlayerSkin(user.id(), selectedSkinKey)
                );
            }
        }
        handleBack();
    }

    @FXML
    private void handleBack(ActionEvent event) {
        handleBack();
    }

    @Override
    public Pane getRootPane() {
        return skinStackPane;
    }

    @Override
    public void setPointerToView(StackPane pointer) {
        this.contentRoot = pointer;
    }
}