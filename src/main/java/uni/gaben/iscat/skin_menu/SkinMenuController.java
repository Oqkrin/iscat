package uni.gaben.iscat.skin_menu;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import uni.gaben.iscat.IscatNavigator;
import uni.gaben.iscat.IscatScenes;

public class SkinMenuController {

    @FXML
    private void handleSkinSelect(ActionEvent event) {
        Button btn = (Button) event.getSource();
        // TODO: cambia l'immagine con space goblin con la skin selezionata
        System.out.println("Hai cliccato una skin! ID Bottone: " + btn.getId());
    }

    @FXML
    private void handleConfirm(ActionEvent event) {
        System.out.println("Skin confermata!");
        // TODO: imposta la skin selezionata come sprite_path che usera il giocatore
        IscatNavigator.getInstance().navigateTo(IscatScenes.MENU);
    }

    @FXML
    private void handleBack(ActionEvent event) {
        System.out.println("Ritorno al menu...");
        IscatNavigator.getInstance().navigateTo(IscatScenes.MENU);
    }
}
