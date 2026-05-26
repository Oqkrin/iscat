package uni.gaben.iscat.iscat_game.lib.abstracts;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.dyn4j.geometry.Circle;
import uni.gaben.iscat.iscat_game.lib.interfaces.model.LifeDeath;
import uni.gaben.iscat.iscat_game.utils.UU;
import uni.gaben.iscat.iscat_game.universe.player.PlayerSettings;
import uni.gaben.iscat.iscat_game.universe.projectiles.Projectile;
import uni.gaben.iscat.iscat_game.utils.DrawSettings;
import uni.gaben.iscat.utils.ThemeManager;
import java.util.Random;

/**
 * Pipeline di rendering astratta e standardizzata per tutte le entità di gioco.
 * Gestisce l'isolamento della matrice del Canvas e calcola gli spazi locali.
 *
 * @param <M> Tipo specifico di modello fisico associato a questa vista.
 */
public abstract class AbstractEntityView<M extends AbstractEntityModel> {
    /** Coordinate cartesiane (X, Y) e dimensioni fisiche (W, H) convertite in scala Pixel */
    protected double cx, cy, w, h;
    /** Rappresentazione dell'orientamento angolare dell'entità espressa sia in gradi che in radianti */
    protected double rotDeg, rotRad;
    /** Fattore di scala visiva indipendente dall'hitbox fisico. Default = 1.0 */
    protected double spriteScale = 1.0;
    /** Dimensioni dello sprite calcolate: hitbox × spriteScale */
    protected double sw, sh;

    /** Flag di controllo: definisce se l'effetto visivo dell'onda d'urto è attualmente in esecuzione */
    protected boolean shockwaveActive = false;
    /** Raggio corrente di espansione dell'onda d'urto espresso in pixel */
    protected double shockwaveRadius = 0.0;
    /** Livello di opacità (Alpha channel) dell'onda d'urto. Scala progressivamente da 1.0 a 0.0 */
    protected double shockwaveAlpha = 1.0;
    /** Deviazione massima istantanea applicata alle coordinate del Canvas durante lo Screen Shake */
    protected double shakeIntensity = 0.0;
    /** Accumulatore del tempo trascorso dall'attivazione dell'effetto d'urto corrente (in secondi) */
    private double shockwaveTimer = 0.0;

    /** Durata totale dell'effetto di shockwave prima dell'autodistruzione (in secondi) */
    protected double shockwaveDuration;
    /** Estensione massima del raggio dell'anello visivo al completamento del ciclo (in pixel) */
    protected double shockwaveMaxRadius;
    /** Spessore di base della linea utilizzato per il disegno dei cerchi concentrici dell'onda */
    protected double shockwaveLineWidth;
    /** Intensità iniziale del terremoto dello schermo al picco massimo dell'esplosione */
    protected double maxShakeIntensity;
    /** Interruttore logico per abilitare o disabilitare gli scostamenti di matrice dello Screen Shake */
    protected boolean shakeEnabled;

    /** Generatore di numeri pseudo-casuali per il calcolo dei vettori di scostamento dello shake */
    protected final Random random = new Random();

    /**
     * Sincronizza la posizione globale dello schermo e le metriche delle
     * dimensioni direttamente estratti dai contorni fisici reali dell'entità.
     */
    public void setPos(M e) {
        cx = UU.mToPx(e.getTransform().getTranslationX());
        cy = UU.mToPx(e.getTransform().getTranslationY());
        w = e.getWidthPx();
        h = e.getHeightPx();
        sw = w;
        sh = h;
    }

    /**
     * Estrae l'orientamento dal modello cinematico e applica l'offset strutturale di rendering.
     */
    protected void setAngle(M e) {
        rotRad = e.getTransform().getRotationAngle() + DrawSettings.BASE_ROTRAD_OFFSET;
        rotDeg = Math.toDegrees(rotRad);
    }

    /**
     * IL MOTORE DEL PIPELINE STANDARD (Template Method)
     * Isola lo stack delle trasformazioni, calcola il punto di rotazione al centro,
     * e richiama il disegno specifico in coordinate locali (-w/2, -h/2).
     */
    public final void setupGraphicsContextAndDrawContent(M entity, GraphicsContext gc, double assetAngularOffsetDeg) {
        setPos(entity);
        setAngle(entity);

        gc.save();

        // Applica lo shake all'intero contesto grafico di questa entità (se attivo)
        applyScreenShake(gc);

        if(entity instanceof Projectile p) {
            gc.setFill(p.getType().color);
        }

        // Trasla l'origine del Canvas esattamente al centro dell'entità
        gc.save();
        gc.translate(cx, cy);
        gc.rotate(rotDeg + assetAngularOffsetDeg);
        // Delega l'esecuzione del disegno interno alle classi derivate.
        // In drawContent(), (0,0) corrisponde perfettamente al centro dell'entità
        drawContent(entity, gc, -sw / 2, -sh / 2, sw, sh);
        gc.restore();

        // Disegna la barra della vita (se attiva)
        if (entity instanceof LifeDeath ld) {
            drawHpBar(ld, gc);
        }

        // Aggiorna e disegna l'anello dello shockwave (se attivo)
        updateAndDrawShockwave(entity, gc);

        gc.restore();
    }

    /**
     * Metodo astratto implementato dalle singole visual views per gestire i propri contenuti grafici.
     */
    protected abstract void drawContent(M entity, GraphicsContext gc, double x, double y, double width, double height);

    /**
     * Inizializza e attiva una sequenza di shockwave configurando i parametri geometrici e cinematici.
     * Deve essere invocato singolarmente all'interno di un evento (es. morte del nemico, skill o spawn).
     *
     * @param duration Durata complessiva dell'effetto visivo in secondi.
     * @param maxRadius Raggio finale massimo raggiunto dall'anello dell'onda d'urto in pixel.
     * @param lineWidth Spessore del tratto del perimetro dell'onda principale.
     * @param maxShake Intensità massima dei pixel di spostamento dello schermo al frame zero.
     * @param enableShake True per accoppiare l'effetto visivo al movimento sussultorio della telecamera.
     */
    public void triggerShockwave(double duration, double maxRadius, double lineWidth, double maxShake, boolean enableShake) {
        this.shockwaveActive = true;
        this.shockwaveTimer = 0.0;
        this.shockwaveDuration = duration;
        this.shockwaveMaxRadius = maxRadius;
        this.shockwaveLineWidth = lineWidth;
        this.maxShakeIntensity = maxShake;
        this.shakeEnabled = enableShake;

        this.shockwaveAlpha = 1.0;
        this.shockwaveRadius = 0.0;
        this.shakeIntensity = enableShake ? maxShake : 0.0;
    }

    /**
     * Inizializza e attiva una sequenza rapida di shockwave utilizzando configurazioni hardware standard.
     * Comodo overload per attivazioni al volo con raggio ($2500\text{ px}$) e spessore di linea ($15\text{ px}$) predefiniti.
     *
     * @param duration Durata complessiva dell'effetto visivo in secondi.
     * @param enableShake True per attivare lo scuotimento della telecamera durante l'espansione.
     */
    public void triggerShockwave(double duration, boolean enableShake) {
        triggerShockwave(duration, 2500.0, 15.0, 24.0, enableShake);
    }

    /**
     * Altera la matrice di trasformazione del contesto grafico applicando una traslazione casuale 2D.
     * L'effetto simula lo Screen Shake e degrada matematicamente in base alla dissipazione dell'intensità.
     */
    private void applyScreenShake(GraphicsContext gc) {
        if (shockwaveActive && shakeEnabled && shakeIntensity > 0.1) {
            double shakeX = (random.nextDouble() * 2.0 - 1.0) * shakeIntensity;
            double shakeY = (random.nextDouble() * 2.0 - 1.0) * shakeIntensity;
            gc.translate(shakeX, shakeY);
        }
    }

    /**
     * Gestisce la timeline logica e il rendering sul Canvas dell'onda d'urto particellare.
     * Disegna tre ovali concentrici con livelli di opacità e spessori differenziati per simulare un effetto Glow.
     */
    private void updateAndDrawShockwave(AbstractEntityModel entity, GraphicsContext gc) {
        if (!shockwaveActive) return;

        // Avanzamento della timeline basato sul tempo reale di simulazione del motore
        shockwaveTimer += UU.UNIVERSE_TICK;
        double progress = shockwaveTimer / shockwaveDuration;

        // Interrompe l'effetto e resetta lo stato quando la transizione temporale giunge al 100%
        if (progress >= 1.0) {
            shockwaveActive = false;
            shakeIntensity = 0.0;
            return;
        }

        // Interpolazione lineare dei parametri grafici basata sul progresso temporale dell'onda
        shockwaveRadius = progress * shockwaveMaxRadius;
        shockwaveAlpha = Math.max(0.0, 1.0 - progress);
        shakeIntensity = maxShakeIntensity * (1.0 - progress);

        gc.save();
        double centerX = UU.mToPx(entity.getTransform().getTranslationX());
        double centerY = UU.mToPx(entity.getTransform().getTranslationY());
        double diameter = shockwaveRadius * 2;
        double topLeftX = centerX - shockwaveRadius;
        double topLeftY = centerY - shockwaveRadius;

        // Canale 1: Riempimento interno soft semitrasparente
        double fillAlpha = shockwaveAlpha * 0.15;
        gc.setFill(Color.rgb(255, 255, 255, fillAlpha));
        gc.fillOval(topLeftX, topLeftY, diameter, diameter);

        // Canale 2: Bordo esterno sfumato ad ampio raggio (Glow Esterno)
        gc.setStroke(Color.rgb(255, 255, 255, shockwaveAlpha * 0.3));
        gc.setLineWidth(shockwaveLineWidth * 3.5);
        gc.strokeOval(topLeftX, topLeftY, diameter, diameter);

        // Canale 3: Corona circolare intermedia a contrasto medio
        gc.setStroke(Color.rgb(255, 255, 255, shockwaveAlpha * 0.6));
        gc.setLineWidth(shockwaveLineWidth * 1.8);
        gc.strokeOval(topLeftX, topLeftY, diameter, diameter);

        // Canale 4: Nucleo perimetrale solido e nitido dell'onda d'urto
        gc.setStroke(Color.rgb(255, 255, 255, shockwaveAlpha));
        gc.setLineWidth(shockwaveLineWidth);
        gc.strokeOval(topLeftX, topLeftY, diameter, diameter);
        gc.restore();
    }

    /**
     * Renderizza sul Canvas una barra della salute bicromatica fluttuante sopra le coordinate geometriche dell'entità.
     */
    protected void drawHpBar(LifeDeath entity, GraphicsContext gc) {
        gc.setFill(ThemeManager.getInstance().getColorError());
        gc.fillRect(cx - w / 2, cy - h / 2 - PlayerSettings.HP_BAR_OFFSET_Y, w, PlayerSettings.HP_BAR_HEIGHT);
        gc.setFill(ThemeManager.getInstance().getColorSuccess());
        gc.fillRect(cx - w / 2, cy - h / 2 - PlayerSettings.HP_BAR_OFFSET_Y, w * (entity.getLife() / entity.getMaxLife()), PlayerSettings.HP_BAR_HEIGHT);
    }

    /**
     * Visualizzatore Debug delle Collisioni geometriche basato sulle coordinate attuali.
     */
    public void drawDebugCollision(M e, GraphicsContext gc) {
        setPos(e); setAngle(e);
        gc.save(); gc.translate(cx, cy); gc.rotate(rotDeg);
        gc.setStroke(Color.LIME); gc.setLineWidth(1.5);
        if (e.getFixtureCount() > 0 && e.getFixture(0).getShape() instanceof Circle) {
            double radiusPx = w / 2; gc.strokeOval(-radiusPx, -radiusPx, w, h);
        } else {
            gc.strokeRect(-w / 2, -h / 2, w, h);
        }
        gc.setStroke(Color.RED); gc.strokeLine(0, 0, w / 2, 0);
        gc.restore();
    }
}