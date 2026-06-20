package uni.gaben.iscat.view;

import javafx.scene.layout.StackPane;
import uni.gaben.iscat.controller.LoginMenuController;
import uni.gaben.iscat.model.login.LoginModel;
import uni.gaben.iscat.view.components.AbstractIscatStackPane;

public class LoginView extends AbstractIscatStackPane {

    private LoginMenuController fxmlController;

    public LoginView() {
        super(null, true);
        StackPane content = loadFxml("/uni/gaben/iscat/fxml/LoginMenu.fxml", (LoginMenuController ctrl) -> {
            fxmlController = ctrl;
            ctrl.setLoginModel(new LoginModel());
            ctrl.setup();
        });

        this.getChildren().add(content);
    }

    @Override
    public void onShow() {
        super.onShow();
        if (fxmlController != null) fxmlController.onShow();
    }

    @Override
    public void onHide() {
        super.onHide();
        if (fxmlController != null) fxmlController.onHide();
    }
}