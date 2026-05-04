package uni.gaben.iscat.utils;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

public final class debug {

    public static void addDebugBorderToGraph(Parent root) {
        // Definiamo uno stroke riutilizzabile
        Border debugBorder = new Border(new BorderStroke(
                Color.GOLD,
                BorderStrokeStyle.DASHED,
                CornerRadii.EMPTY,
                new BorderWidths(0.8)
        ));

        for (Node n : root.getChildrenUnmodifiable()) {
            if (n instanceof Region r) {
                // Usiamo setBorder invece di setStyle!
                r.setBorder(debugBorder);

                // Se è un contenitore, scendiamo ricorsivamente
                addDebugBorderToGraph(r);
            }
        }
    }
}