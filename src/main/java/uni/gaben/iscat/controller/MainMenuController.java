package uni.gaben.iscat.controller;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.layout.StackPane;
import org.kordamp.ikonli.javafx.FontIcon;
import uni.gaben.iscat.IscatNavigator;
import uni.gaben.iscat.model.IscatViews;
import uni.gaben.iscat.universe.entity.player.PlayerSettings;
import uni.gaben.iscat.view.components.AnimatedCanvas;

import javax.swing.event.ChangeListener;
import java.beans.PropertyChangeListener;

public class MainMenuController implements IscatFxmlController {

    @FXML private Button playButton;
    @FXML private Button optionsButton;
    @FXML private Button scoreButton;
    @FXML private Button skinButton;
    @FXML private Button bestiaryButton;
    @FXML private Button logoutButton;
    @FXML private Button quitButton;
    @FXML private Button leaderboardButton;
    @FXML private Button creditsButton;
    AnimatedCanvas skin = new AnimatedCanvas(128);

    private StackPane contentRoot;

    DoubleProperty playMaxSide =  new SimpleDoubleProperty(0);
    DoubleProperty settingsMaxSide =   new SimpleDoubleProperty(0);
    DoubleProperty skinMaxSide =  new SimpleDoubleProperty(0);

    @FXML
    public void initialize() {
        playButton.widthProperty().addListener((obs, oldVal, newVal) -> {
            if(newVal.doubleValue() > playButton.getHeight()) {
                playMaxSide.set(newVal.doubleValue());
            }
        });

        playButton.heightProperty().addListener((obs, oldVal, newVal) -> {
            if(newVal.doubleValue() > playButton.getWidth()) {
                playMaxSide.set(newVal.doubleValue());
            }
        });

        playMaxSide.addListener((obs, oldVal, newVal) -> {
           playButton.minHeightProperty().set(newVal.doubleValue());
           playButton.maxHeightProperty().set(newVal.doubleValue());
           playButton.minWidthProperty().set(newVal.doubleValue());
           playButton.maxWidthProperty().set(newVal.doubleValue());
        });

        setIcon(playButton,      "fas-rocket");
        setIcon(optionsButton,   "fas-cog");
        setIcon(scoreButton,     "fas-eye");
        setIcon(skinButton,      "fas-gift");
        setIcon(bestiaryButton,  "fas-bug");
        setIcon(logoutButton,    "fas-sign-out-alt");
        setIcon(quitButton,      "fas-door-open");
        setIcon(leaderboardButton,"fas-door-open");
        setIcon(creditsButton, "fab-creative-commons");
    }

    @FXML public void playGame()            { navigate(IscatViews.GAME);             }
    @FXML public void openOptionsMenu()     { navigate(IscatViews.OPTIONS_MENU);     }
    @FXML public void openScoreMenu()       { navigate(IscatViews.SCORE_MENU);       }
    @FXML public void openSkinMenu()        { navigate(IscatViews.SKIN_MENU);        }
    @FXML public void openBestiaryMenu()    { navigate(IscatViews.BESTIARY_MENU);    }
    @FXML public void logout()              { navigate(IscatViews.LOGIN_MENU);       }
    @FXML public void openLeaderboardMenu() { navigate(IscatViews.LEADERBOARD_MENU); }
    @FXML public void openCreditsMenu()     { navigate(IscatViews.CREDITS);          }
    @FXML public void quit()                { Platform.exit();                       }

    private void navigate(IscatViews scene) {
        IscatNavigator.getInstance().navigateWithFade(scene);
    }

    private void setIcon(Button btn, String iconCode) {
        if (btn == null) return;

        if (!iconCode.equals("fas-gift")) {
            FontIcon icon = new FontIcon(iconCode);
            icon.setIconSize(32);
            btn.setGraphic(icon);
        } else {

            PlayerSettings.playerSkinProperty().addListener((observable, oldValue, newValue) -> {
                skin.loadSkin(newValue, 32, 32);
                skin.resize(128.0);
            });

            skin.loadSkin(PlayerSettings.getPlayerSkin(), 32, 32);
            skin.resize(128.0);
            skin.setFrameDuration(0.20);

            btn.setGraphic(skin);
        }
    }

    @Override
    public void setContentRoot(StackPane contentRoot) {
        this.contentRoot = contentRoot;
    }
}



