package uni.gaben.iscat.skin_menu;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import uni.gaben.iscat.IscatNavigator;
import uni.gaben.iscat.IscatScenes;
import uni.gaben.iscat.IscatSettings;
import uni.gaben.iscat.gamenex.view.GamenexScene;

import java.util.Objects;

public class SkinMenuController {

    @FXML private ImageView skinPreview;
    private String selectedSkinId;
    private String selectedSkinPath;

    @FXML
    private void handleSkinSelect(ActionEvent event) {
        Button btn = (Button) event.getSource();
        selectedSkinId = btn.getId();
        updatePreview();
    }

    private void updatePreview() {
        if (skinPreview == null) return;

        String path = switch (selectedSkinId) {
            case "skin1" -> "/uni/gaben/iscat/sprites/player1.png";
            case "skin2" -> "/uni/gaben/iscat/sprites/player2.png";
            case "skin3" -> "/uni/gaben/iscat/sprites/player3.png";
            case "skin4" -> "/uni/gaben/iscat/sprites/player4.png";
            case "skin5" -> "/uni/gaben/iscat/sprites/player5.png";
            case "skin6" -> "/uni/gaben/iscat/sprites/player6.png";
            case "skin7" -> "/uni/gaben/iscat/sprites/player7.png";
            case "skin8" -> "/uni/gaben/iscat/sprites/player8.png";
            case "skin9" -> "/uni/gaben/iscat/sprites/player9.png";
            default -> "/uni/gaben/iscat/sprites/player1.png";
        };

        selectedSkinPath = path;

        try {
            Image newImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream(path)));
            skinPreview.setImage(newImage);
        } catch (Exception e) {
            System.err.println("Errore caricamento preview: " + path);
        }
    }

    @FXML
    private void handleConfirm(ActionEvent event) {
        if (selectedSkinPath == null) {
            System.out.println("Nessuna skin selezionata!");
            return;
        }

        IscatSettings.player_skin = selectedSkinPath;

        // Ricarica sprite nel gioco
        reloadPlayerSkin();

        System.out.println("Skin confermata: " + selectedSkinPath);
        IscatNavigator.getInstance().navigateTo(IscatScenes.MENU);
    }


    @FXML
    private void handleBack(ActionEvent event) {
        IscatNavigator.getInstance().navigateTo(IscatScenes.MENU);
    }

    private void reloadPlayerSkin() {
        try {
            GamenexScene gameScene = (GamenexScene) IscatNavigator.getInstance()
                    .getScene(IscatScenes.GAMEN);

            if (gameScene != null) {
                gameScene.reloadPlayerSkin();
            }
        } catch (Exception e) {
            System.err.println("Errore reload sprite gioco: " + e.getMessage());
        }
    }
}
