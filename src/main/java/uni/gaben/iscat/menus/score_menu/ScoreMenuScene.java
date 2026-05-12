package uni.gaben.iscat.menus.score_menu;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.SceneAntialiasing;
import javafx.scene.layout.StackPane;
import uni.gaben.iscat.IscatSceneAbstract;

import java.io.IOException;

public class ScoreMenuScene extends IscatSceneAbstract {

    private final StackPane contentRoot;

    public ScoreMenuScene() {
        super(new StackPane(), true, SceneAntialiasing.DISABLED);   // Per avere lo sfondo con le stelle
        this.contentRoot = getContentRoot();
        loadFXML();
    }

    private void loadFXML() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/uni/gaben/iscat/fxml/score_menu.fxml")
            );

            Parent fxmlContent = loader.load();

            if (fxmlContent instanceof javafx.scene.layout.Region region) {
                // Permettiamo alla region di rimpicciolirsi fino a zero
                region.setMinSize(0, 0);
                region.prefWidthProperty().bind(contentRoot.widthProperty());
                region.prefHeightProperty().bind(contentRoot.heightProperty());
            }

            contentRoot.getChildren().add(fxmlContent);
            StackPane.setAlignment(fxmlContent, Pos.CENTER);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void initStyles() {
    }

    @Override
    protected void initNodes() {

    }

    @Override
    protected void initLayout() {

    }

    @Override
    protected void initBindings() {

    }

    @Override
    protected void initEventHandlers() {

    }

    @Override
    public void onShow() {
        if (getStarryBackground() != null) {
            getStarryBackground().setFollowMouse(true);
        }
    }
}
