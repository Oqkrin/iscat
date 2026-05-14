package uni.gaben.iscat.gamenex.universe.player;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import uni.gaben.iscat.gamenex.lib.abstracts.AbstractEntityView;
import uni.gaben.iscat.gamenex.lib.interfaces.view.Drawable;
import uni.gaben.iscat.gamenex.model.GamenexModel;
import uni.gaben.iscat.gamenex.universe.UniverseController;
import uni.gaben.iscat.gamenex.universe.UniverseModel;
import uni.gaben.iscat.gamenex.universe.UniverseSettings;
import uni.gaben.iscat.utils.ThemeManager;
import uni.gaben.iscat.utils.sprite.AnimationController;
import uni.gaben.iscat.utils.sprite.SpriteDrawer;
import uni.gaben.iscat.utils.sprite.SpriteLibrary;

import java.util.Random;

public class PlayerView extends AbstractEntityView implements Drawable<PlayerModel> {

    private static final Random RANDOM = new Random();

    // Logica di Sprite e Animazione
    private SpriteDrawer sprite;
    private final AnimationController anim = new AnimationController();

    public PlayerView() {
        // Inizializzazione basata sulla skin attuale
        updateSprite(PlayerSettings.getPlayerSkin());

        // Listener per cambiare skin a runtime
        PlayerSettings.playerSkinProperty().addListener((obs, old, newValue) -> updateSprite(newValue));
    }

    private void updateSprite(String path) {
        // Usiamo la libreria per non ricaricare la stessa immagine più volte
        // frameSize e totalFrames dovrebbero venire da PlayerSettings o dedotti
        this.sprite = SpriteLibrary.getInstance().getSprite(path, 32, 32);
    }

    @Override
    public void draw(PlayerModel entity, GraphicsContext gc) {
        setPos(entity);
        setAngle(entity);
        setSize(PlayerSettings.DIMENSIONE_SPRITE);

        // 2. Logica Animazione Dinamica
        double speed = entity.getLinearVelocity().getMagnitude();
        if (speed > 0.1) {
            anim.setState(1); // Riga 1: Movimento/Thruster attivi
            anim.setSpeed(1.0 + (speed / PlayerSettings.VELOCITA_MAX)); // Più corre, più veloce l'animazione
        } else {
            anim.setState(0); // Riga 0: Idle/Fermo
            anim.setSpeed(1.0);
        }
        anim.update(GamenexModel.TICKUNIT);

        // 3. Rendering
        gc.save();
        gc.translate(cx, cy);
        gc.rotate(rotDeg);

        // Effetto scia (ora usa i colori del tema)
        drawThrustEffect(gc, entity);

        // Disegno dello Sprite tramite il sistema centralizzato (gestisce il tint)
        if (sprite != null) {
            int frame = anim.getCurrentFrameIdx(sprite.getTotalFrames(), GamenexModel.TICKUNIT*10); //10 fps
            int row = anim.getCurrentState();
            sprite.draw(gc, row, frame, 0, 0, w, h); // x,y sono 0 perché siamo traslati
        }

        gc.restore();
        drawHpBar(entity, gc);
    }

    private void drawThrustEffect(GraphicsContext gc, PlayerModel entity) {
        double vx = entity.getLinearVelocity().x;
        double vy = entity.getLinearVelocity().y;
        double speed = Math.sqrt(vx * vx + vy * vy) * UniverseSettings.SCALE;

        double intensity = Math.min(speed / (PlayerSettings.VELOCITA_MAX * UniverseSettings.SCALE), 1.0);
        if (intensity < 0.01) return;

        int particleCount = (int) (PlayerSettings.THRUST_MIN_PARTICLES + intensity * PlayerSettings.THRUST_EXTRA_PARTICLES);

        // Prendiamo il colore primario dal ThemeManager (dinamico!)
        Color accentColor = ThemeManager.getInstance().getColor("accent-primary", Color.CYAN);

        for (int i = 0; i < particleCount; i++) {
            double baseY = h / 2;
            double maxThrustHeight = h * PlayerSettings.THRUST_HEIGHT_FACTOR;
            double offsetY = baseY + RANDOM.nextDouble() * maxThrustHeight;
            double distanceRatio = (offsetY - baseY) / maxThrustHeight;
            double maxSpreadX = w * PlayerSettings.THRUST_SPREAD_X_FACTOR * (1 + distanceRatio * 2);
            double offsetX = (RANDOM.nextDouble() - 0.5) * maxSpreadX * 2;
            double size = (PlayerSettings.THRUST_MIN_PARTICLE_SIZE + RANDOM.nextDouble() * PlayerSettings.THRUST_PARTICLE_SIZE_VARIATION) * (1.2 - distanceRatio * 0.5);

            gc.setFill(getParticleColor(distanceRatio, intensity, RANDOM.nextDouble(), accentColor));
            gc.fillRect(offsetX - size / 2, offsetY - size / 2, size, size);
        }
    }

    // Il metodo getParticleColor rimane simile ma ora accetta l'accent dinamico
    private static Color getParticleColor(double distanceRatio, double intensity, double colorMix, Color accent) {
        double alphaMix = Math.min(1.0, (1.0 - distanceRatio * 0.4) * intensity);
        double brightness = (colorMix < 0.3) ? 1.5 : (colorMix < 0.6) ? 1.0 : 0.5;
        double alpha = (colorMix < 0.3) ? 0.8 : (colorMix < 0.6) ? 0.5 : 0.2;

        return Color.color(
                Math.min(1.0, brightness * accent.getRed()),
                Math.min(1.0, brightness * accent.getGreen()),
                Math.min(1.0, brightness * accent.getBlue()),
                Math.min(1.0, alpha * alphaMix));
    }
}