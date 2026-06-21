package uni.gaben.iscat.universe.effects;

import javafx.scene.paint.Color;
import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.camera.CameraModel;
import uni.gaben.iscat.universe.entities.interfaces.Removable;
import uni.gaben.iscat.universe.entities.interfaces.Stateful;
import uni.gaben.iscat.utils.Updatable;
import uni.gaben.iscat.utils.theme.ThemeManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generatore ed emettitore di effetti particellari d'impatto (Hit Spark).
 * <p>
 * Questa classe modella e controlla un burst stocastico di micro-particelle cromatiche
 * combinato con un'onda d'urto circolare espansiva (Shockwave). L'intero sistema viene calcolato
 * in coordinate mondo (pixel mondo) e applica equazioni di attenuazione non lineare.
 * </p>
 * <p>
 * L'animazione di espansione e dissolvenza sfrutta un'equazione di interpolazione esponenziale
 * inversa (Ease Out Expo) per conferire all'effetto un feeling visivo secco, dinamico ed immediato.
 * </p>
 */
public class HitSpark implements Stateful, Updatable, Removable {

    /** Istanza condivisa del generatore di numeri pseudo-casuali per la dispersione vettoriale. */
    private static final Random RANDOM = new Random();

    // ===== Configurazione parametri particellari (Pixel Mondo) =====
    private static final int PARTICLE_COUNT = 15;
    private static final double PARTICLE_RADIUS_MIN = 8;
    private static final double PARTICLE_RADIUS_MAX = 16;
    private static final double EXPLOSION_RADIUS_MIN = 25;
    private static final double EXPLOSION_RADIUS_MAX = 90;
    private static final double SHOCKWAVE_RADIUS_MIN = 40;
    private static final double SHOCKWAVE_RADIUS_MAX = 80;
    private static final double SHOCKWAVE_LINE_WIDTH = 3;

    /** Palette cromatica dinamica estratta in tempo reale dal {@link ThemeManager} dell'applicazione. */
    private static final Color[] PALETTE = {
            ThemeManager.getInstance().getAccentPrimary(),
            ThemeManager.getInstance().getAccentSecondary(),
            ThemeManager.getInstance().getAccentTertiary(),
    };

    /** Lista interna contenente le singole istanze geometriche delle particelle generate. */
    private final List<SparkParticle> particles = new ArrayList<>();

    /** Componente circolare delegata al rendering dell'onda d'urto espansiva a contorno. */
    private final ShockwaveCircle shockwave;

    /** Flag logico che attesta l'esaurimento temporale completo di ogni sotto-componente visiva. */
    private boolean expired = false;

    /** Tempo cumulativo totale (in secondi) trascorso dall'istanziazione dell'effetto. */
    private double elapsed = 0.0;

    /** Durata massima di sopravvivenza calcolata come limite superiore tra tutte le componenti del burst. */
    private double duration;

    /**
     * Metodo Factory statico per l'allocazione e il calcolo iniziale di un effetto di impatto.
     * Converte il vettore di posizionamento fisico espresso in metri nelle corrispondenti unità pixel del mondo di gioco.
     *
     * @param worldPos      Il vettore bidimensionale {@link Vector2} indicante il punto dell'impatto in metri.
     * @param camera        Il modello {@link CameraModel} di riferimento per la gestione della vista.
     * @param velocity      Vettore di velocità applicato (può essere integrato per calcolare l'angolo di eiezione).
     * @param particleCount Quantità discreta di particelle da generare all'interno del burst.
     * @return Una nuova istanza configurata di {@code HitSpark}.
     */
    public static HitSpark create(Vector2 worldPos, CameraModel camera,
                                  Vector2 velocity, int particleCount) {
        double px = UU.mToPx(worldPos.x);
        double py = UU.mToPx(worldPos.y);
        return new HitSpark(px, py, particleCount);
    }

    /**
     * Sovraccarico del metodo Factory statico che istanzia l'effetto applicando il numero di particelle di default.
     *
     * @param worldPos Il vettore bidimensionale {@link Vector2} del punto dell'impatto in metri.
     * @param camera   Il modello della telecamera di riferimento.
     * @param velocity Vettore di velocità associato all'impatto.
     * @return Una nuova istanza configurata di {@code HitSpark}.
     */
    public static HitSpark create(Vector2 worldPos, CameraModel camera, Vector2 velocity) {
        return create(worldPos, camera, velocity, PARTICLE_COUNT);
    }

    /**
     * Costruttore primario interno. Configura la dispersione radiale casuale di particelle e shockwave
     * mappando distanze, raccordi angolari, tempi di vita atomici e tonalità cromatiche.
     *
     * @param worldX        Coordinata X di origine dell'impatto in pixel mondo.
     * @param worldY        Coordinata Y di origine dell'impatto in pixel mondo.
     * @param particleCount Numero di particelle da inserire nella simulazione.
     */
    private HitSpark(double worldX, double worldY, int particleCount) {
        for (int i = 0; i < particleCount; i++) {
            double angle = RANDOM.nextDouble() * 2 * Math.PI;
            double distance = RANDOM.nextDouble() * (EXPLOSION_RADIUS_MAX - EXPLOSION_RADIUS_MIN) + EXPLOSION_RADIUS_MIN;
            double endX = worldX + Math.cos(angle) * distance;
            double endY = worldY + Math.sin(angle) * distance;
            double radius = RANDOM.nextDouble() * (PARTICLE_RADIUS_MAX - PARTICLE_RADIUS_MIN) + PARTICLE_RADIUS_MIN;
            Color color = PALETTE[RANDOM.nextInt(PALETTE.length)];
            double particleDuration = RANDOM.nextDouble() * 0.6 + 1.2; // Compresa tra 1.2 e 1.8 secondi
            particles.add(new SparkParticle(worldX, worldY, endX, endY, radius, color, particleDuration));
        }

        // Configurazione geometrica e temporale della Shockwave circolare
        double shockwaveDuration = RANDOM.nextDouble() * 0.6 + 1.2;
        double startRadius = 0.1;
        double endRadius = RANDOM.nextDouble() * (SHOCKWAVE_RADIUS_MAX - SHOCKWAVE_RADIUS_MIN) + SHOCKWAVE_RADIUS_MIN;
        double startAlpha = 0.5;
        double endAlpha = 0.0;
        shockwave = new ShockwaveCircle(worldX, worldY, startRadius, endRadius, startAlpha, endAlpha, shockwaveDuration);

        // Calcolo della durata globale massima del sistema
        duration = particles.stream().mapToDouble(p -> p.duration).max().orElse(1.5);
        duration = Math.max(duration, shockwave.duration);
    }

    /**
     * Aggiorna lo stato logico e la timeline matematica del sistema di particelle e dell'onda d'urto.
     * Quando tutte le componenti interne esauriscono il proprio ciclo vitale, l'effetto si auto-estingue.
     *
     * @param dt Il passo temporale trascorso dall'ultimo frame espresso in secondi (Delta Time).
     */
    @Override
    public void update(double dt) {
        elapsed += dt;
        if (elapsed >= duration) {
            expired = true;
            return;
        }
        boolean anyAlive = false;

        // Aggiornamento sequenziale delle singole particelle
        for (SparkParticle p : particles) {
            p.update(elapsed);
            if (!p.isDead()) anyAlive = true;
        }

        // Aggiornamento dei vincoli dell'onda d'urto
        shockwave.update(elapsed);
        if (!shockwave.isDead()) anyAlive = true;

        if (!anyAlive) expired = true;
    }

    /** {@inheritDoc} */
    @Override public boolean shouldRemove() { return expired; }

    /** {@inheritDoc} */
    @Override public boolean setShouldRemove(boolean remove) { this.expired = remove; return true; }

    /** @return La lista contenente le particelle aggregate generate dal burst. */
    public List<SparkParticle> getParticles() { return particles; }

    /** @return L'oggetto Shockwave associato all'effetto d'impatto. */
    public ShockwaveCircle getShockwave() { return shockwave; }

    /** @return {@code true} se il sistema ha terminato la riproduzione, {@code false} altrimenti. */
    public boolean isExpired() { return expired; }

    /**
     * Modello geometrico e logico di una singola particella cromatica elementare del burst.
     * Si sposta radialmente dal baricentro applicando un'attenuazione esponenziale delle coordinate
     * e riducendo progressivamente raggio ed opacità.
     */
    public static class SparkParticle {
        private final double startX, startY;
        private final double endX, endY;
        private final double startRadius;
        private final double endRadius;
        private final Color color;
        private final double duration;
        private boolean dead = false;

        // Proprietà geometriche istantanee calcolate dinamicamente frame per frame
        private double x, y, radius, alpha;

        /**
         * Costruttore della singola unità particellare.
         */
        SparkParticle(double startX, double startY, double endX, double endY,
                      double startRadius, Color color, double duration) {
            this.startX = startX;
            this.startY = startY;
            this.endX = endX;
            this.endY = endY;
            this.startRadius = startRadius;
            this.endRadius = 0.1; // Riduzione del raggio fin quasi allo zero
            this.color = color;
            this.duration = duration;
            this.alpha = 1.0;
        }

        /**
         * Calcola la posizione, il raggio e l'alfa correnti applicando la formula logistica Ease Out Expo:
         * <p>
         * $$f(t) = 1 - 2^{-10 \cdot t}$$
         * </p>
         *
         * @param elapsed Il tempo parziale cumulativo di vita del sistema.
         */
        void update(double elapsed) {
            double progress = Math.min(elapsed / duration, 1.0);

            // Formula Ease Out Expo
            double t = (progress == 1.0) ? 1.0 : 1.0 - Math.pow(2, -10 * progress);

            x = startX + (endX - startX) * t;
            y = startY + (endY - startY) * t;
            radius = startRadius + (endRadius - startRadius) * t;
            alpha = 1.0 - t; // Dissolvenza lineare in funzione dello smoothing

            if (progress >= 1.0) dead = true;
        }

        /** @return {@code true} se la particella ha concluso il ciclo vitale, {@code false} altrimenti. */
        public boolean isDead() { return dead; }
        /** @return La coordinata X attuale della particella in pixel mondo. */
        public double getX() { return x; }
        /** @return La coordinata Y attuale della particella in pixel mondo. */
        public double getY() { return y; }
        /** @return Il raggio geometrico attuale della particella in pixel mondo. */
        public double getRadius() { return radius; }
        /** @return Il coefficiente di opacità corrente della particella (0.0 - 1.0). */
        public double getAlpha() { return alpha; }
        /** @return La tonalità di colore JavaFX assegnata alla particella. */
        public Color getColor() { return color; }
    }

    /**
     * Rappresentazione geometrica di un anello d'onda d'urto bidimensionale (Shockwave Ring).
     * Si espande radialmente dal centro del punto d'impatto attenuando la propria visibilità.
     */
    public static class ShockwaveCircle {
        private final double centerX, centerY;
        private final double startRadius, endRadius;
        private final double startAlpha, endAlpha;
        private final double duration;
        private double currentRadius, currentAlpha;
        private boolean dead = false;

        /**
         * Costruttore preconfigurato per l'anello d'onda d'urto.
         */
        ShockwaveCircle(double cx, double cy,
                        double startRadius, double endRadius,
                        double startAlpha, double endAlpha,
                        double duration) {
            this.centerX = cx;
            this.centerY = cy;
            this.startRadius = startRadius;
            this.endRadius = endRadius;
            this.startAlpha = startAlpha;
            this.endAlpha = endAlpha;
            this.duration = duration;
        }

        /**
         * Ricalcola il raggio espanso e l'alfa corrente applicando l'attenuazione esponenziale Ease Out Expo.
         *
         * @param elapsed Il tempo parziale cumulativo di vita del sistema.
         */
        void update(double elapsed) {
            double progress = Math.min(elapsed / duration, 1.0);

            // Formula Ease Out Expo
            double t = (progress == 1.0) ? 1.0 : 1.0 - Math.pow(2, -10 * progress);

            currentRadius = startRadius + (endRadius - startRadius) * t;
            currentAlpha = startAlpha + (endAlpha - startAlpha) * t;

            if (progress >= 1.0) dead = true;
        }

        /** @return {@code true} se la shockwave ha concluso l'espansione, {@code false} altrimenti. */
        public boolean isDead() { return dead; }
        /** @return La coordinata X del centro geometrico dell'anello in pixel mondo. */
        public double getCenterX() { return centerX; }
        /** @return La coordinata Y del centro geometrico dell'anello in pixel mondo. */
        public double getCenterY() { return centerY; }
        /** @return Il raggio d'espansione corrente in pixel mondo. */
        public double getRadius() { return currentRadius; }
        /** @return L'opacità attuale del canale alfa dell'anello. */
        public double getAlpha() { return currentAlpha; }
        /** @return Spessore fisso della linea di tracciamento dell'anello (Stroke Width). */
        public double getLineWidth() { return SHOCKWAVE_LINE_WIDTH; }
    }
    /** {@inheritDoc} */
    @Override public int getState() { return 0; }
    /** {@inheritDoc} */
    @Override public void setState(int state) {}
    /** {@inheritDoc} */
    @Override public double getStateTime() { return elapsed; }
    /** {@inheritDoc} */
    @Override public void setStateTime(double stateTime) { this.elapsed = stateTime; }
    /** {@inheritDoc} */
    @Override public void updateStateTime(double dt) { elapsed += dt; }
}