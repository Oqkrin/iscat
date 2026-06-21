package uni.gaben.iscat.universe.camera;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import uni.gaben.iscat.utils.Spring;

/**
 * Memorizza il centro della visuale corrente in coordinate mondo e le dimensioni del viewport.
 * Il posizionamento centrale è guidato da due molle fisiche a smorzamento critico (una per l'asse X
 * e una per l'asse Y) che inseguono fluidamente la posizione del target impostato.
 * <p>
 * Integra inoltre algoritmi per la generazione di perturbazioni ad alta frequenza (Screen Shake)
 * e overlay cromatici (Hurt Flash) in risposta ai danni subiti dal giocatore, calcolando
 * le equazioni di conversione per il rendering della vista.
 * </p>
 */
public class CameraModel {

    /** Molla fisica a smorzamento critico per l'asse orizzontale X. */
    private final Spring springX;

    /** Molla fisica a smorzamento critico per l'asse verticale Y. */
    private final Spring springY;

    /** Flag di controllo per determinare se la telecamera ha effettuato il posizionamento iniziale (snap). */
    private boolean snapped = false;

    /** Proprietà JavaFX per la larghezza del viewport (area visibile dello schermo in pixel). */
    private final DoubleProperty screenWidth  = new SimpleDoubleProperty(1280);

    /** Proprietà JavaFX per l'altezza del viewport (area visibile dello schermo in pixel). */
    private final DoubleProperty screenHeight = new SimpleDoubleProperty(720);

    /** Proprietà JavaFX per il livello di zoom di base configurato manualmente. */
    private final DoubleProperty baseZoom = new SimpleDoubleProperty(1.25);

    /** Proprietà JavaFX per il livello di zoom reale interpolato utilizzato correntemente dal renderer. */
    private final DoubleProperty actualZoom = new SimpleDoubleProperty(1.25);

    // Variabili di stato per gli effetti visivi d'impatto
    private double shakeIntensity = 0.0;
    private double shakeTimeLeft = 0.0;
    private double shakeX = 0.0;
    private double shakeY = 0.0;
    private double hurtFlashIntensity = 0.0; // Da 0.0 (normale) a 1.0 (flash rosso totale)

    /**
     * Costruisce un nuovo modello di telecamera preimpostando le configurazioni delle molle.
     * <p>
     * La molla dell'asse X utilizza la rigidità di base, mentre l'asse Y applica un moltiplicatore
     * incrementale (definito in {@link CameraSettings#Y_STIFFNESS_MULTIPLIER}) per ridurre
     * il ritardo (lag) verticale durante i salti o le cadute.
     * </p>
     */
    public CameraModel() {
        this.springX = Spring.critico(0,
                CameraSettings.SPRING_STIFFNESS,
                CameraSettings.SPRING_MASS);

        this.springY = Spring.critico(0,
                CameraSettings.SPRING_STIFFNESS * CameraSettings.Y_STIFFNESS_MULTIPLIER,
                CameraSettings.SPRING_MASS);
    }

    /** @return Il fattore di zoom reale interpolato ed effettivo. */
    public double getZoom() { return actualZoom.get(); }

    /** @param v Il nuovo valore di zoom reale da impostare nel renderer. */
    public void setActualZoom(double v) { this.actualZoom.set(v); }

    /** @return Il livello di zoom base o nominale memorizzato. */
    public double getBaseZoom() { return baseZoom.get(); }

    /** * Imposta il livello di zoom base forzando il valore all'interno del range di sicurezza.
     * @param v Il valore di zoom desiderato.
     */
    public void setBaseZoom(double v) {
        this.baseZoom.set(Math.clamp(v, CameraSettings.MIN_MANUAL_ZOOM, CameraSettings.MAX_MANUAL_ZOOM));
    }

    /** * Incrementa o decrementa il livello di zoom base tramite un delta lineare.
     * @param delta Il coefficiente di variazione dello zoom.
     */
    public void addZoom(double delta) {
        setBaseZoom(getBaseZoom() + delta);
    }

    /** @return La molla fisica associata alla coordinata orizzontale X. */
    public Spring getSpringX() { return springX; }

    /** @return La molla fisica associata alla coordinata verticale Y. */
    public Spring getSpringY() { return springY; }

    /**
     * Attiva istantaneamente gli effetti visivi d'impatto (schermata rossa e tremolio).
     * Calcola la magnitudo dello scuotimento in pixel mondo basandosi sulla severità del danno.
     *
     * @param damageSeverity Coefficiente d'intensità del danno subito dal personaggio.
     */
    public void triggerHurtEffects(double damageSeverity) {
        this.shakeIntensity = damageSeverity * 25.0; // Potenza di traslazione in pixel mondo
        this.shakeTimeLeft = 0.35;                   // Durata dello shake in secondi (350ms)
        this.hurtFlashIntensity = 1.0;               // Opacità iniziale massima dell'overlay rosso
    }

    /**
     * Esegue il decremento e il decadimento temporale degli effetti di scuotimento e flash rosso.
     * Deve essere invocato ad ogni ciclo di aggiornamento logico (frame tick).
     *
     * @param dt Il tempo trascorso dall'ultimo frame espresso in frazioni di secondo (Delta Time).
     */
    public void updateEffects(double dt) {
        // 1. Dissolvenza lineare dell'overlay rosso di danno
        if (hurtFlashIntensity > 0) {
            hurtFlashIntensity = Math.max(0, hurtFlashIntensity - dt * 4.5); // Esaurimento in circa 220ms
        }

        // 2. Calcolo del disturbo stocastico ad alta frequenza per lo scuotimento
        if (shakeTimeLeft > 0) {
            shakeTimeLeft -= dt;
            // Rumore casuale normalizzato scalato per la percentuale di vita residua dell'effetto
            double activePower = shakeIntensity * (shakeTimeLeft / 0.35);
            this.shakeX = (Math.random() * 2.0 - 1.0) * activePower;
            this.shakeY = (Math.random() * 2.0 - 1.0) * activePower;
        } else {
            this.shakeX = 0.0;
            this.shakeY = 0.0;
        }
    }

    /** @return L'intensità corrente dell'overlay di danno (valore compreso tra 0.0 e 1.0). */
    public double getHurtFlashIntensity() {
        return this.hurtFlashIntensity;
    }

    /** * @return La coordinata X del centro telecamera, comprensiva dell'offset stocastico dello shake.
     */
    public double getX() {
        return springX.getPosition() + shakeX;
    }

    /** * @return La coordinata Y del centro telecamera, comprensiva dell'offset stocastico dello shake.
     */
    public double getY() {
        return springY.getPosition() + shakeY;
    }

    /** @return {@code true} se la telecamera ha già eseguito il primo snap iniziale, {@code false} altrimenti. */
    public boolean isSnapped() { return snapped; }

    /** @param snapped Il nuovo stato di ancoraggio (snap) da assegnare. */
    public void setSnapped(boolean snapped) { this.snapped = snapped; }

    /** @return La larghezza del viewport dello schermo in pixel. */
    public double getScreenWidth() { return screenWidth.get(); }

    /** @return La proprietà JavaFX associata alla larghezza dello schermo. */
    public DoubleProperty screenWidthProperty() { return screenWidth; }

    /** @param width La nuova larghezza in pixel dell'area dello schermo. */
    public void setScreenWidth(double width) { this.screenWidth.set(width); }

    /** @return L'altezza del viewport dello schermo in pixel. */
    public double getScreenHeight() { return screenHeight.get(); }

    /** @return La proprietà JavaFX associata all'altezza dello schermo. */
    public DoubleProperty screenHeightProperty() { return screenHeight; }

    /** @param height La nuova altezza in pixel dell'area dello schermo. */
    public void setScreenHeight(double height) { this.screenHeight.set(height); }

    /** @return La coordinata X del punto centrale dello schermo (metà della larghezza). */
    public double getScreenCenterX() { return getScreenWidth() / 2.0; }

    /** @return La coordinata Y del punto centrale dello schermo (metà della altezza). */
    public double getScreenCenterY() { return getScreenHeight() / 2.0; }

    /**
     * Calcola la coordinata X assoluta nel mondo corrispondente al margine sinistro del viewport.
     * <p>
     * Questo valore definisce il punto di traslazione per il contesto grafico affinché il centro
     * geometrico calcolato coincida con la mezzeria del display.
     * </p>
     *
     * @return La coordinata X d'origine dell'estremità sinistra in pixel mondo.
     */
    public double getViewportLeftX() {
        return getX() - (getScreenWidth() / getZoom()) / 2.0;
    }

    /**
     * Calcola la coordinata Y assoluta nel mondo corrispondente al margine superiore del viewport.
     *
     * @return La coordinata Y d'origine dell'estremità superiore in pixel mondo.
     */
    public double getViewportTopY() {
        return getY() - (getScreenHeight() / getZoom()) / 2.0;
    }
}