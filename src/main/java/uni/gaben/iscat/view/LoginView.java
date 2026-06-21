package uni.gaben.iscat.view;

import javafx.scene.layout.StackPane;
import uni.gaben.iscat.controller.menus.LoginMenuController;
import uni.gaben.iscat.model.login.LoginModel;
import uni.gaben.iscat.view.components.AbstractIscatStackPane;

/**
 * Vista dedicata alla gestione della schermata di autenticazione (Login).
 * Carica il layout FXML corrispondente e delega i cicli di vita di apertura
 * e chiusura al rispettivo controller logico.
 */
public class LoginView extends AbstractIscatStackPane {

    private LoginMenuController fxmlController;

    /**
     * Costruisce la vista di login caricando il file FXML associato
     * e inizializzando il relativo controller con un nuovo modello di login.
     */
    public LoginView() {
        super(null, true);
        StackPane content = loadFxml("/uni/gaben/iscat/fxml/LoginMenu.fxml", (LoginMenuController ctrl) -> {
            fxmlController = ctrl;
            ctrl.setLoginModel(new LoginModel());
            ctrl.setup();
        });

        this.getChildren().add(content);
    }

    /**
     * Invocato quando la vista viene mostrata a schermo.
     * Attiva le procedure di inizializzazione visiva o i listener del controller.
     */
    @Override
    public void onShow() {
        super.onShow();
        if (fxmlController != null) fxmlController.onShow();
    }

    /**
     * Invocato quando la vista viene nascosta o sostituita.
     * Rilascia le risorse o disattiva i listener attivi nel controller.
     */
    @Override
    public void onHide() {
        super.onHide();
        if (fxmlController != null) fxmlController.onHide();
    }
}