package uni.gaben.iscat.skin_menu;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import uni.gaben.iscat.IscatNavigator;
import uni.gaben.iscat.IscatScenes;
import uni.gaben.iscat.IscatSettings;
import uni.gaben.iscat.gamenex.view.GamenexScene;

public class SkinMenuController {

    private String selectedSkinId;

    @FXML
    private void handleSkinSelect(ActionEvent event) {
        Button btn = (Button) event.getSource();
        // TODO: cambia l'immagine con space goblin con la skin selezionata
        selectedSkinId = btn.getId();
    }

    @FXML
    private void handleConfirm(ActionEvent event) {
        if (selectedSkinId == null) {
            System.out.println("Nessuna skin selezionata!");
            return;
        }
        switch (selectedSkinId) {
            case "skin1" -> IscatSettings.player_skin = "/uni/gaben/iscat/sprites/player1.png";
            case "skin2" -> IscatSettings.player_skin = "/uni/gaben/iscat/sprites/player2.png";
            case "skin3" -> IscatSettings.player_skin = "/uni/gaben/iscat/sprites/player3.png";
            case "skin4" -> IscatSettings.player_skin = "/uni/gaben/iscat/sprites/player4.png";
            case "skin5" -> IscatSettings.player_skin = "/uni/gaben/iscat/sprites/player5.png";
            case "skin6" -> IscatSettings.player_skin = "/uni/gaben/iscat/sprites/player6.png";
            case "skin7" -> IscatSettings.player_skin = "/uni/gaben/iscat/sprites/player7.png";
            case "skin8" -> IscatSettings.player_skin = "/uni/gaben/iscat/sprites/player8.png";
            case "skin9" -> IscatSettings.player_skin = "/uni/gaben/iscat/sprites/player9.png";
        }

        reloadPlayerSkin();

        System.out.println("Skin cambiata in: " + IscatSettings.player_skin);
        IscatNavigator.getInstance().navigateTo(IscatScenes.MENU);

    }

    @FXML
    private void handleBack(ActionEvent event) {
        System.out.println("Ritorno al menu...");
        IscatNavigator.getInstance().navigateTo(IscatScenes.MENU);
    }

    private void reloadPlayerSkin() {
        try {
            // Recupera la scena di gioco e ricarica lo sprite
            GamenexScene gameScene = (GamenexScene) IscatNavigator.getInstance()
                    .getScene(IscatScenes.GAMEN);

            if (gameScene != null) {
                gameScene.reloadPlayerSkin();
            } else {
                System.err.println("GameScene non trovata");
            }
        } catch (Exception e) {
            System.err.println("Errore durante il cambio skin: " + e.getMessage());
        }
    }
}
