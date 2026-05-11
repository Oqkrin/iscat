package uni.gaben.iscat.skin_menu;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import uni.gaben.iscat.IscatSceneAbstract;

import java.io.IOException;

public class SkinMenuScene extends IscatSceneAbstract {

    private final StackPane contentRoot;

    public SkinMenuScene() {
        super(new StackPane(), true);   // Per avere lo sfondo con le stelle
        this.contentRoot = getContentRoot();
        loadFXML();
    }

    private void loadFXML() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/uni/gaben/iscat/fxml/player_skin_choose_menu.fxml")
            );

            Parent fxmlContent = loader.load();

            // Scaliamo dato che senno è piccolo
            double targetWidth = 300;
            double targetHeight = 150;

            double scaleX = targetWidth / fxmlContent.prefWidth(-1);
            double scaleY = targetHeight / fxmlContent.prefHeight(-1);
            double scale = Math.min(scaleX, scaleY);

            fxmlContent.setScaleX(scale);
            fxmlContent.setScaleY(scale);

            // Centriamo
            fxmlContent.setTranslateX((targetWidth - fxmlContent.prefWidth(-1) * scale) / 2);
            fxmlContent.setTranslateY((targetHeight - fxmlContent.prefHeight(-1) * scale) / 2);

            // Aggiungiamo il contenuto FXML scalato
            contentRoot.getChildren().add(fxmlContent);
            contentRoot.setAlignment(Pos.CENTER);

        } catch (IOException e) {
            System.err.println("ERRORE: Impossibile caricare player_skin_choose_menu.fxml");
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
