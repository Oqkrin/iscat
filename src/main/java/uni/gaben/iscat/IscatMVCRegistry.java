package uni.gaben.iscat;

import uni.gaben.iscat.model.IscatViews;
import uni.gaben.iscat.controller.game.GameController;
import uni.gaben.iscat.model.game.GameModel;
import uni.gaben.iscat.view.game.GameView;
import uni.gaben.iscat.view.LoginView;
import uni.gaben.iscat.view.components.AbstractIscatStackPane;
import uni.gaben.iscat.view.IscatFXMLView;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

public final class IscatMVCRegistry {

    private static final Map<IscatViews, Supplier<AbstractIscatStackPane>> REGISTRY = new EnumMap<>(IscatViews.class);

    static {
        // Registrazione viste Custom
        REGISTRY.put(IscatViews.LOGIN_MENU, LoginView::new);
        REGISTRY.put(IscatViews.GAME, () -> new GameView(new GameController(new GameModel())));

        // Registrazione viste FXML standard
        registerFxml(IscatViews.MAIN_MENU, "MainMenu.fxml");
        registerFxml(IscatViews.SKIN_MENU, "SkinMenu.fxml");
        registerFxml(IscatViews.BESTIARY_MENU, "BestiaryMenu.fxml");
        registerFxml(IscatViews.SCORE_MENU, "ScoreMenu.fxml");
        registerFxml(IscatViews.SETTINGS_MENU, "SettingsMenu.fxml");
        registerFxml(IscatViews.CREDITS, "CreditsMenu.fxml");
        registerFxml(IscatViews.LEADERBOARD_MENU, "LeaderboardMenu.fxml");
        registerFxml(IscatViews.TUTORIAL_MENU, "TutorialMenu.fxml");
        registerFxml(IscatViews.ENTITY_EDITOR, "EntityEditorMenu.fxml");
    }

    private IscatMVCRegistry() {
    }

    private static void registerFxml(IscatViews view, String fxmlName) {
        REGISTRY.put(view, () -> new IscatFXMLView("/uni/gaben/iscat/fxml/" + fxmlName));
    }

    public static AbstractIscatStackPane getMVC(IscatViews scene) {
        Supplier<AbstractIscatStackPane> factory = REGISTRY.get(scene);
        if (factory == null) {
            throw new IllegalArgumentException("Nessuna View registrata nel sistema per la scena: " + scene);
        }
        return factory.get();
    }
}