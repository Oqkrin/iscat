package uni.gaben.iscat.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.layout.StackPane;
import org.kordamp.ikonli.javafx.FontIcon;
import uni.gaben.iscat.IscatNavigator;
import uni.gaben.iscat.model.IscatViews;
import uni.gaben.iscat.universe.entity.player.PlayerSettings;
import uni.gaben.iscat.view.components.AnimatedCanvas;

public class MainMenuController implements IscatFxmlController {

    @FXML private Button playButton;
    @FXML private Button optionsButton;
    @FXML private Button scoreButton;
    @FXML private Button skinButton;
    @FXML private Button bestiaryButton;
    @FXML private Button logoutButton;
    @FXML private Button quitButton;
    @FXML private Button leaderboardButton;
    AnimatedCanvas skin = new AnimatedCanvas(128);

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

        if (!iconCode.equals("fas-gift")) {
            FontIcon icon = new FontIcon(iconCode);
            icon.setIconSize(18);
            btn.setGraphic(icon);
        } else {

            PlayerSettings.playerSkinProperty().addListener((observable, oldValue, newValue) -> {
                skin.loadSkin(newValue, 32, 32);
                skin.resize(32.0);
            });

            skin.loadSkin(PlayerSettings.getPlayerSkin(), 32, 32);
            skin.resize(32.0);
            skin.setFrameDuration(0.20);

            btn.setGraphic(skin);
        }

        btn.setContentDisplay(ContentDisplay.LEFT);
        btn.setGraphicTextGap(14.0);
    }

    @Override
    public void setContentRoot(StackPane contentRoot) {
        this.contentRoot = contentRoot;
    }
}



