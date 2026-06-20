package uni.gaben.iscat.controller.menus;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import org.kordamp.ikonli.javafx.FontIcon;
import uni.gaben.iscat.IscatNavigator;
import uni.gaben.iscat.controller.interfaces.IscatFxmlController;
import uni.gaben.iscat.model.IscatViews;
import uni.gaben.iscat.utils.SessionManager;
import uni.gaben.iscat.view.components.AnimatedCanvas;

import java.util.List;

public class MainMenuController implements IscatFxmlController {

    @FXML private Button playButton;
    @FXML private Button tutorialButton;
    @FXML private Button settingsButton;
    @FXML private Button scoreButton;
    @FXML private Button skinButton;
    @FXML private Button bestiaryButton;
    @FXML private Button logoutButton;
    @FXML private Button quitButton;
    @FXML private Button leaderboardButton;
    @FXML private Button creditsButton;

    private final AnimatedCanvas skin = new AnimatedCanvas(128);
    private final AnimatedCanvas mobCanvas = new AnimatedCanvas(128);

    private StackPane contentRoot;

    private final DoubleProperty sideButtonsMaxSide = new SimpleDoubleProperty(0);

    @FXML
    public void initialize() {
        List<Button> sideButtons = List.of(leaderboardButton, scoreButton, bestiaryButton, skinButton);

        for (Button btn : sideButtons) {
            btn.widthProperty().addListener((obs, oldVal, newVal) -> updateMaxSide(newVal.doubleValue()));
            btn.heightProperty().addListener((obs, oldVal, newVal) -> updateMaxSide(newVal.doubleValue()));
        }

        sideButtonsMaxSide.addListener((obs, oldVal, newVal) -> {
            double size = newVal.doubleValue();
            for (Button btn : sideButtons) {
                btn.setMinWidth(size);
                btn.setMaxWidth(size);
                btn.setMinHeight(size);
                btn.setMaxHeight(size);
            }
        });

        setIcon(playButton,        "fas-rocket");
        setIcon(tutorialButton,    "fas-graduation-cap");
        setIcon(settingsButton,     "fas-cog");
        setIcon(scoreButton,       "fas-eye");
        setIcon(skinButton,        "fas-gift");
        setIcon(bestiaryButton,    "fas-bug");
        setIcon(logoutButton,      "fas-sign-out-alt");
        setIcon(quitButton,        "fas-door-open");
        setIcon(leaderboardButton, "fas-list-ol");
        setIcon(creditsButton,     "fab-creative-commons");
    }

    private void updateMaxSide(double value) {
        if (value > sideButtonsMaxSide.get()) {
            sideButtonsMaxSide.set(value);
        }
    }

    @FXML public void playGame()            { navigate(IscatViews.GAME);             }
    @FXML public void openTutorialMenu()    { navigate(IscatViews.TUTORIAL_MENU);    }
    @FXML public void openSettingsMenu()    { navigate(IscatViews.SETTINGS_MENU);   }
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

        try {
            switch (iconCode) {
                case "fas-gift" -> {
                    SessionManager.playerSkinProperty().addListener((observable, oldValue, newValue) -> {
                        skin.loadSkin(newValue, 64, 64);
                        skin.resize(128.0);
                    });
                    skin.loadSkin(SessionManager.getPlayerSkin(), 32, 32);
                    skin.resize(128.0);
                    skin.setFrameDuration(0.20);
                    btn.setGraphic(skin);
                }
                case "fas-bug" -> {
                    mobCanvas.loadSkin("/uni/gaben/iscat/sprites/enemies/iscat_mob.png", 32, 32);
                    mobCanvas.resize(128.0);
                    mobCanvas.setFrameDuration(0.20);
                    btn.setGraphic(mobCanvas);
                }
                case "fas-eye", "fas-list-ol" -> {
                    FontIcon icon = new FontIcon(iconCode);
                    icon.setIconSize(128);
                    btn.setGraphic(icon);
                }
                default -> {
                    FontIcon icon = new FontIcon(iconCode);
                    icon.setIconSize(32);
                    btn.setGraphic(icon);
                }
            }
        } catch (Exception e) {
            System.err.println("Impossibile caricare l'icona: " + iconCode + " per il bottone " + btn.getId());
        }
    }

    @Override
    public void setPointerToView(StackPane pointer) {
        this.contentRoot = pointer;
    }
}