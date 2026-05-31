package uni.gaben.iscat.screens.main_menu;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.layout.StackPane;
import org.kordamp.ikonli.javafx.FontIcon;
import uni.gaben.iscat.IscatNavigator;
import uni.gaben.iscat.model.IscatViews;
import uni.gaben.iscat.controller.IscatFxmlController;

public class MenuController implements IscatFxmlController {

    @FXML private Button playButton;
    @FXML private Button optionsButton;
    @FXML private Button scoreButton;
    @FXML private Button skinButton;
    @FXML private Button bestiaryButton;
    @FXML private Button logoutButton;
    @FXML private Button quitButton;
    @FXML private Button leaderboardButton;

    private StackPane contentRoot;

    @FXML
    public void initialize() {
        setIcon(playButton,      "fas-rocket");
        setIcon(optionsButton,   "fas-cog");
        setIcon(scoreButton,     "fas-eye");
        setIcon(skinButton,      "fas-gift");
        setIcon(bestiaryButton,  "fas-bug");
        setIcon(logoutButton,    "fas-sign-out-alt");
        setIcon(quitButton,      "fas-door-open");
        setIcon(leaderboardButton,"fas-door-open");
    }

    @FXML public void playGame()            { navigate(IscatViews.GAME);             }
    @FXML public void openOptionsMenu()     { navigate(IscatViews.OPTIONS_MENU);     }
    @FXML public void openScoreMenu()       { navigate(IscatViews.SCORE_MENU);       }
    @FXML public void openSkinMenu()        { navigate(IscatViews.SKIN_MENU);        }
    @FXML public void openBestiaryMenu()    { navigate(IscatViews.BESTIARY_MENU);    }
    @FXML public void logout()              { navigate(IscatViews.LOGIN_MENU);       }
    @FXML public void openLeaderboardMenu() {navigate(IscatViews.LEADERBOARD_MENU);  }
    @FXML public void quit()                { Platform.exit();                       }

    private void navigate(IscatViews scene) {
        IscatNavigator.getInstance().navigateWithFade(scene);
    }

    private void setIcon(Button btn, String iconCode) {
        if (btn == null) return;
        FontIcon icon = new FontIcon(iconCode);
        icon.iconSizeProperty().bind(btn.textProperty().length().multiply(10));
        btn.setGraphic(icon);
        btn.setContentDisplay(ContentDisplay.TOP);
    }

    @Override
    public void setContentRoot(StackPane contentRoot) {
        this.contentRoot = contentRoot;
    }
}