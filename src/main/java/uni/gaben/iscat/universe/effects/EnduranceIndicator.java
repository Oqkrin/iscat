package uni.gaben.iscat.universe.effects;

import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.camera.CameraModel;
import uni.gaben.iscat.universe.entities.interfaces.Removable;
import uni.gaben.iscat.universe.entities.interfaces.Stateful;
import uni.gaben.iscat.utils.Updatable;

/**
 * Indicatore grafico fluttuante dell'Endurance (Floating Text / Combat Text).
 * <p>
 * Questa classe gestisce il ciclo di vita, il movimento traslatorio verticale e la dissolvenza
 * alfa di un testo numerico indicante variazioni di endurance (danni subiti o cure ricevute).
 * </p>
 * <p>
 * Include un metodo factory statico che trasforma i vettori fisici del mondo reale in
 * coordinate geometriche bidimensionali proiettate sullo schermo, tenendo conto del
 * fattore di zoom e della matrice di spostamento della telecamera.
 * </p>
 */
public class EnduranceIndicator implements Stateful, Updatable, Removable {

    /** Durata complessiva dell'effetto a schermo espressa in secondi prima della rimozione. */
    public static final double LIFETIME = 1.2;

    /** Velocità lineare di ascesa verticale dell'indicatore espressa in pixel al secondo. */
    public static final double RISE_SPEED = 60.0;

    /** Font tipografico predefinito utilizzato per il rendering del testo numerico. */
    public static final Font FONT = Font.font("Miracode", FontWeight.BOLD, 18);

    /** Coordinata d'origine orizzontale X proiettata sullo schermo. */
    public final double x;

    /** Coordinata verticale Y corrente sullo schermo (varia progressivamente verso l'alto). */
    public double y;

    /** Il valore numerico da visualizzare (valori negativi indicano danno, valori positivi indicano cura). */
    public final double value;

    /** Tempo cumulativo trascorso dall'istanziazione dell'effetto. */
    private double duration = 0.0;

    /** Coefficiente di opacità (Alpha blending) per la dissolvenza visiva, compreso tra 0.0 e 1.0. */
    public double alpha = 1.0;

    /** Flag di stato logico che segnala al motore grafico la necessità di distruggere l'oggetto. */
    private boolean shouldRemove = false;

    /**
     * Costruttore privato interno. L'allocazione esterna è delegata al metodo factory statico
     * {@link #create(Vector2, CameraModel, double, double, double)}.
     *
     * @param screenX Coordinata X di destinazione sul viewport.
     * @param screenY Coordinata Y di destinazione sul viewport.
     * @param value   Il valore numerico associato all'evento di variazione.
     */
    private EnduranceIndicator(double screenX, double screenY, double value) {
        this.x = screenX;
        this.y = screenY;
        this.value = value;
    }

    /**
     * Fabbrica e calcola un nuovo indicatore fluttuante traducendo le coordinate fisiche globali in pixel di schermo.
     * <p>
     * L'equazione di proiezione applicata mappa il vettore mondo basandosi sulla posizione
     * del mirino della telecamera, riscalando i delta per il fattore di zoom e ri-ancorando
     * il baricentro al centro geometrico del canvas:
     * </p>
     * <p>
     * $$P_{\text{screen}} = (P_{\text{world\_px}} - \text{Cam}_{\text{world}}) \cdot \text{zoom} + \frac{\text{Canvas}}{2.0}$$
     * </p>
     *
     * @param worldPos     Il vettore bidimensionale {@link Vector2} della posizione fisica nel mondo di gioco.
     * @param camera       Il modello {@link CameraModel} della telecamera per estrarre coordinate e zoom attuali.
     * @param value        La variazione numerica quantitativa da mostrare a schermo.
     * @param canvasWidth  La larghezza attuale del canvas grafico in pixel.
     * @param canvasHeight L'altezza attuale del canvas grafico in pixel.
     * @return Una nuova istanza configurata di {@code EnduranceIndicator}.
     */
    public static EnduranceIndicator create(Vector2 worldPos, CameraModel camera,
                                            double value, double canvasWidth, double canvasHeight) {
        double px = UU.mToPx(worldPos.x);
        double py = UU.mToPx(worldPos.y);
        double zoom = camera.getZoom();

        double screenX = (px - camera.getX()) * zoom + canvasWidth / 2.0;
        double screenY = (py - camera.getY()) * zoom + canvasHeight / 2.0;

        return new EnduranceIndicator(screenX, screenY, value);
    }

    /**
     * Aggiorna la logica interna dell'indicatore per ogni fotogramma di gioco (frame tick).
     * Modifica la coordinata Y riducendola linearmente in base alla velocità di ascesa e ricalcola
     * il decadimento del canale alpha tramite interpolazione lineare invertita rispetto alla vita residua.
     *
     * @param dt Il tempo parziale trascorso dall'ultimo ciclo espresso in frazioni di secondo (Delta Time).
     */
    @Override
    public void update(double dt) {
        duration += dt;
        if (duration >= LIFETIME) {
            shouldRemove = true;
            return;
        }
        y -= RISE_SPEED * dt;
        alpha = 1.0 - (duration / LIFETIME);
    }

    /** {@inheritDoc} */
    @Override
    public boolean shouldRemove() {
        return shouldRemove;
    }

    /** {@inheritDoc} */
    @Override
    public boolean setShouldRemove(boolean remove) {
        this.shouldRemove = remove;
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public int getState() { return 0; }

    /** {@inheritDoc} */
    @Override
    public void setState(int state) { /* Non utilizzato */ }

    /** {@inheritDoc} */
    @Override
    public double getStateTime() { return duration; }

    /** {@inheritDoc} */
    @Override
    public void setStateTime(double stateTime) { this.duration = stateTime; }

    /** {@inheritDoc} */
    @Override
    public void updateStateTime(double dt) { duration += dt; }
}