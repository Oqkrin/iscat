package uni.gaben.iscat.universe.rendering.vfx;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import uni.gaben.iscat.universe.effects.EnduranceIndicator;
import uni.gaben.iscat.utils.theme.ThemeManager;

/**
 * Gestore grafico dedicato agli elementi di interfaccia (UI/HUD) nel mondo di gioco (World Space UI).
 * Renderizza indicatori di danno fluttuanti e barre di stato (HP/Time Gauge) applicando filtri di illuminazione (DropShadow glow).
 */
public final class InterfaceVFX {

    private InterfaceVFX() {}

    /**
     * Disegna un testo fluttuante indicante variazioni di endurance (danni o cure).
     * Applica automaticamente una codifica a colori basata sul segno del valore (Errore/Danno vs Successo/Cura).
     */
    public static void drawEnduranceIndicator(EnduranceIndicator enduranceIndicator, GraphicsContext gc) {
        Color color = (enduranceIndicator.value < 0) ? ThemeManager.getInstance().getColorError() : ThemeManager.getInstance().getColorSuccess();
        gc.setFill(color);
        gc.setGlobalAlpha(enduranceIndicator.alpha);

        String text = String.format("%+.0f", enduranceIndicator.value);
        double textWidth = gc.getFont().getSize() * text.length() * 0.6;
        gc.fillText(text, enduranceIndicator.x - textWidth / 2.0, enduranceIndicator.y);
        gc.setGlobalAlpha(1.0);
    }

    /**
     * Renderizza una barra della salute bidimensionale (sfondo rosso di danno, riempimento verde).
     * Applica un effetto shader di luminescenza perimetrale (DropShadow) basato sui colori del tema.
     *
     * @param percent La percentuale di salute attuale normalizzata tra {@code 0.0} e {@code 1.0}.
     */
    public static void drawHpBarRaw(GraphicsContext gc, double x, double y, double w, double h, double percent) {
        gc.save();

        DropShadow glow = new DropShadow();
        glow.setColor(ThemeManager.getInstance().getColorSuccess().deriveColor(0, 1, 1, 0.6));
        glow.setRadius(5);
        glow.setSpread(0.2);
        gc.setEffect(glow);

        gc.setFill(ThemeManager.getInstance().getColorError());
        gc.fillRect(x, y, w, h);
        gc.setFill(ThemeManager.getInstance().getColorSuccess());
        gc.fillRect(x, y, w * percent, h);
        gc.restore();
    }

    /**
     * Disegna la barra temporale delle abilità (Time Gauge) utilizzando parametri primitivi disaccoppiati.
     * Isola l'effetto glow sul colore d'accento primario per differenziarla visivamente dalle barre HP.
     */
    public static void drawTimeGaugeBarRaw(GraphicsContext gc, double x, double y, double w, double h, double percent) {
        gc.save();

        DropShadow glow = new DropShadow();
        glow.setColor(ThemeManager.getInstance().getAccentPrimary().deriveColor(0, 1, 1, 0.8));
        glow.setRadius(8);
        glow.setSpread(0.4);
        gc.setEffect(glow);

        gc.setFill(ThemeManager.getInstance().getBgSecondary());
        gc.fillRect(x, y, w, h);
        gc.setFill(ThemeManager.getInstance().getAccentPrimary());
        gc.fillRect(x, y, w * percent, h);
        gc.restore();
    }
}