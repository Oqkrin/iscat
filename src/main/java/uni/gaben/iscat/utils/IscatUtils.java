package uni.gaben.iscat.utils;

import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

import static uni.gaben.iscat.utils.rapporto_aureo.GeometriaAurea.phiMinore;

public class IscatUtils {
    private IscatUtils() {
        /* Classe utils da non inizializzare */
    }

    public static void scalaCentraRispettoParent(Stage stage, Rectangle2D parentRect) {
        parentRect = phiMinore(parentRect);
        stage.setWidth(parentRect.getWidth());
        stage.setHeight(parentRect.getHeight());
        stage.setX(parentRect.getMinX());
        stage.setY(parentRect.getMinY());
    }

    public static ObservableList<Screen> getSchermiCorrenti(Stage stage) {
        return Screen.getScreensForRectangle(
                stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight()
        );
    }
}
