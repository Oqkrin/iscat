package uni.gaben.iscat;

import uni.gaben.iscat.model.IscatViews;
import uni.gaben.iscat.controller.game.GameController;
import uni.gaben.iscat.model.game.GameModel;
import uni.gaben.iscat.view.game.GameView;
import uni.gaben.iscat.view.LoginView;
import uni.gaben.iscat.view.components.AbstractIscatStackPane;
import uni.gaben.iscat.view.IscatFXMLView;

public class IscatMVCRegistry {
    private IscatMVCRegistry() {
    }

    public static AbstractIscatStackPane getMVC(IscatViews scene) {
        return switch (scene) {
            case LOGIN_MENU -> new LoginView();
            case GAME -> {
                GameController gameController = new GameController(new GameModel());
                yield new GameView(gameController);
            }
            case MAIN_MENU -> new IscatFXMLView("/uni/gaben/iscat/fxml/MainMenu.fxml");
            case SKIN_MENU -> new IscatFXMLView("/uni/gaben/iscat/fxml/SkinMenu.fxml");
            case BESTIARY_MENU -> new IscatFXMLView("/uni/gaben/iscat/fxml/BestiaryMenu.fxml");
            case SCORE_MENU -> new IscatFXMLView("/uni/gaben/iscat/fxml/ScoreMenu.fxml");
            case SETTINGS_MENU -> new IscatFXMLView("/uni/gaben/iscat/fxml/SettingsMenu.fxml");
            case CREDITS -> new IscatFXMLView("/uni/gaben/iscat/fxml/CreditsMenu.fxml");
            case LEADERBOARD_MENU -> new IscatFXMLView("/uni/gaben/iscat/fxml/LeaderboardMenu.fxml");
            case TUTORIAL_MENU -> new IscatFXMLView("/uni/gaben/iscat/fxml/TutorialMenu.fxml");
            case ENTITY_EDITOR -> new IscatFXMLView("/uni/gaben/iscat/fxml/EntityEditorMenu.fxml");
        };
    }
}