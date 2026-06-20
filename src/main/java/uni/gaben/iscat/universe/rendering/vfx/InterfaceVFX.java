package uni.gaben.iscat.universe.rendering.vfx;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import uni.gaben.iscat.universe.effects.EnduranceIndicator;
import uni.gaben.iscat.utils.theme.ThemeManager;

/**
 * Utility class responsabile del rendering degli elementi di interfaccia
 * grafica (UI/HUD)
 * posizionati direttamente all'interno del mondo di gioco.
 */
public final class InterfaceVFX {

    /**
     * Costruttore privato per prevenire l'istanziamento della classe utility.
     */
    private InterfaceVFX() {
    }

    /**
     * Disegna un indicatore di variazione dell'endurance (testo fluttuante dei
     * danni o delle cure).
     * Il colore del testo viene determinato dinamicamente dal segno del valore:
     * rosso per i danni (valori negativi) e verde per la rigenerazione (valori
     * positivi),
     * attingendo direttamente dai colori definiti nel {@link ThemeManager}.
     * 
     * @param enduranceIndicator l'oggetto contenente i dati, la posizione e
     *                           l'opacità dell'indicatore fluttuante
     * @param gc                 il {@link GraphicsContext} del canvas su cui
     *                           effettuare il disegno
     */
    public static void drawEnduranceIndicator(EnduranceIndicator enduranceIndicator, GraphicsContext gc) {
        Color color = (enduranceIndicator.value < 0) ? ThemeManager.getInstance().getColorError()
                : ThemeManager.getInstance().getColorSuccess();
        gc.setFill(color);
        gc.setGlobalAlpha(enduranceIndicator.alpha);
        String text = String.format("%+.0f", enduranceIndicator.value);
        double textWidth = gc.getFont().getSize() * text.length() * 0.6;
        gc.fillText(text, enduranceIndicator.x - textWidth / 2.0, enduranceIndicator.y);
        gc.setGlobalAlpha(1.0);
    }

    /**
     * Disegna una barra degli HP bidimensionale composta da uno sfondo di danno
     * (rosso)
     * e un riempimento proporzionale alla vita rimasta (verde).
     * <p>
     * Questo metodo accetta parametri primitivi disaccoppiati per consentire il
     * disegno
     * diretto sia da entità standard che da strutture dati di batch protette o
     * locali.
     * </p>
     *
     * @param gc      il {@link GraphicsContext} del canvas su cui effettuare il
     *                disegno
     * @param x       la coordinata X dell'angolo in alto a sinistra della barra
     * @param y       la coordinata Y dell'angolo in alto a sinistra della barra
     * @param w       la larghezza totale della barra in pixel
     * @param h       l'altezza totale della barra in pixel
     * @param percent la percentuale di salute attuale, espressa come valore
     *                compreso tra {@code 0.0} e {@code 1.0}
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

    public static void drawTimeGaugeBarRaw(GraphicsContext gc, double x, double y, double w, double h, double percent) {
        gc.save();
        javafx.scene.effect.DropShadow glow = new javafx.scene.effect.DropShadow();
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