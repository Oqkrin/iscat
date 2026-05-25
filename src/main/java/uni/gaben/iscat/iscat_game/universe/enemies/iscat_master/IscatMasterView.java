package uni.gaben.iscat.iscat_game.universe.enemies.iscat_master;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import uni.gaben.iscat.iscat_game.lib.abstracts.AbstractEntityView;
import uni.gaben.iscat.iscat_game.lib.interfaces.view.Drawable;
import uni.gaben.iscat.iscat_game.lib.interfaces.view.DrawableSpriteSheet;
import uni.gaben.iscat.iscat_game.utils.UU;
import uni.gaben.iscat.utils.AudioManager;
import uni.gaben.iscat.utils.sprite.SpriteSheetsAnimator;
import uni.gaben.iscat.utils.sprite.SpriteSheetsParser;
import uni.gaben.iscat.utils.sprite.SpritesLibrary;
import java.util.Random;

import static uni.gaben.iscat.iscat_game.universe.enemies.iscat_master.IscatMasterSettings.*;
import static uni.gaben.iscat.iscat_game.universe.enemies.iscat_master.IscatMasterModel.AnimationState;

public class IscatMasterView extends AbstractEntityView<IscatMasterModel>
        implements Drawable<IscatMasterModel>, DrawableSpriteSheet {

    private static final String PATH_MAIN     = "/uni/gaben/iscat/sprites/enemies/iscat_master.png";
    private static final String PATH_ENTRANCE = "/uni/gaben/iscat/sprites/enemies/iscat_master_entrance.png";
    private static final String PATH_ATTACK1  = "/uni/gaben/iscat/sprites/enemies/iscat_master_attack1.png";
    private static final String PATH_ATTACK2  = "/uni/gaben/iscat/sprites/enemies/iscat_master_attack2.png";
    private static final String PATH_ATTACK3  = "/uni/gaben/iscat/sprites/enemies/iscat_master_attack3.png";
    private static final String PATH_ATTACK4  = "/uni/gaben/iscat/sprites/enemies/iscat_master_attack4.png";
    private static final String PATH_DEATH    = "/uni/gaben/iscat/sprites/enemies/iscat_master_death.png";

    private final SpriteSheetsParser mainSheet;
    private final SpriteSheetsParser entranceSheet;
    private final SpriteSheetsParser attack1Sheet;
    private final SpriteSheetsParser attack2Sheet;
    private final SpriteSheetsParser attack3Sheet;
    private final SpriteSheetsParser attack4Sheet;
    private final SpriteSheetsParser deathSheet;

    private final SpriteSheetsAnimator mainAnimator;
    private final SpriteSheetsAnimator entranceAnimator;
    private final SpriteSheetsAnimator attack1Animator;
    private final SpriteSheetsAnimator attack2Animator;
    private final SpriteSheetsAnimator attack3Animator;
    private final SpriteSheetsAnimator attack4Animator;
    private final SpriteSheetsAnimator deathAnimator;

    private SpriteSheetsParser activeSheet;
    private SpriteSheetsAnimator activeAnimator;

    // SHOCKWAVE & SCREEN SHAKE
    private boolean shockwaveActive = false;
    private double shockwaveRadius = 0.0;
    private double shockwaveAlpha = 1.0;

    private final Random random = new Random();
    private double shakeIntensity = 10.0;

    private static final double SHOCKWAVE_MAX_RADIUS = 5000.0;
    private static final double SHOCKWAVE_EXPANSION_SPEED = 650.0;
    private static final double SHOCKWAVE_LINE_WIDTH = 15.0;
    private static final double MAX_SHAKE_INTENSITY = 24.0; // Forza massima dello shake (in pixel)

    public IscatMasterView() {
        spriteScale = IscatMasterSettings.ISCATMASTER.scale;

        mainSheet     = load(PATH_MAIN);
        entranceSheet = load(PATH_ENTRANCE);
        attack1Sheet  = load(PATH_ATTACK1);
        attack2Sheet  = load(PATH_ATTACK2);
        attack3Sheet  = load(PATH_ATTACK3);
        attack4Sheet  = load(PATH_ATTACK4);
        deathSheet    = load(PATH_DEATH);

        mainAnimator     = animator(mainSheet);
        entranceAnimator = animator(entranceSheet);
        attack1Animator  = animator(attack1Sheet);
        attack2Animator  = animator(attack2Sheet);
        attack3Animator  = animator(attack3Sheet);
        attack4Animator  = animator(attack4Sheet);
        deathAnimator    = animator(deathSheet);

        activeSheet    = entranceSheet;
        activeAnimator = entranceAnimator;
    }

    private SpriteSheetsParser load(String path) {
        return SpritesLibrary.getInstance().getSprite(path, (int) ISCATMASTER.dimSprite, (int) ISCATMASTER.dimSprite);
    }

    private SpriteSheetsAnimator animator(SpriteSheetsParser sheet) {
        return new SpriteSheetsAnimator(
                1.0 / 6.0,
                sheet != null ? sheet.getTotalFrames() : 1,
                sheet != null ? sheet.getTotalStates() : 1
        );
    }

    @Override
    public SpriteSheetsParser getSpriteSheet() { return activeSheet; }

    @Override
    public SpriteSheetsAnimator getAnimator() { return activeAnimator; }


    @Override
    public void draw(IscatMasterModel entity, GraphicsContext gc) {
        if (entity == null) return;

        if (!entity.isEntranceDone()) {
            activeSheet    = entranceSheet;
            activeAnimator = entranceAnimator;
            entranceAnimator.update(UU.UNIVERSE_TICK);

            if (entranceAnimator.hasCompletedCycle()) {
                entity.setEntranceDone(true);
                entity.setEnabled(true);

                this.shockwaveActive = true;
                this.shockwaveRadius = 0.0;
                this.shockwaveAlpha = 1.0;
                this.shakeIntensity = MAX_SHAKE_INTENSITY;

                AudioManager.getInstance().playSFX("shockwave");
                AudioManager.getInstance().playSFX("laugh");
            }

        } else {
            switch (entity.getAnimationState()) {
                case ATTACK1 -> { activeSheet = attack1Sheet; activeAnimator = attack1Animator; }
                case ATTACK2 -> { activeSheet = attack2Sheet; activeAnimator = attack2Animator; }
                case ATTACK3 -> { activeSheet = attack3Sheet; activeAnimator = attack3Animator; }
                case ATTACK4 -> { activeSheet = attack4Sheet; activeAnimator = attack4Animator; }
                case DEATH   -> { activeSheet = deathSheet;   activeAnimator = deathAnimator;   }
                default      -> { activeSheet = mainSheet;    activeAnimator = mainAnimator;     }
            }

            activeAnimator.update(UU.UNIVERSE_TICK);

            AnimationState state = entity.getAnimationState();

            if (state != AnimationState.IDLE && state != AnimationState.DEATH
                    && activeAnimator.hasCompletedCycle()) {
                activeAnimator.reset();
                entity.setAnimationState(AnimationState.IDLE);
            }

            if (state == AnimationState.DEATH && deathAnimator.hasCompletedCycle()) {
                entity.completeKill();
            }
        }

        // APPLICAZIONE DELLO SCREEN SHAKE
        gc.save(); // Salva lo stato globale prima di traslare la telecamera
        if (shockwaveActive && shakeIntensity > 0.1) {
            // Genera uno spostamento casuale X e Y proporzionale all'intensità rimasta
            double shakeX = (random.nextDouble() * 2.0 - 1.0) * shakeIntensity;
            double shakeY = (random.nextDouble() * 2.0 - 1.0) * shakeIntensity;
            gc.translate(shakeX, shakeY);
        }

        // Tutto quello che viene disegnato qui dentro tremerà (Boss, Sprite, Bounding Box)
        setPos(entity);
        setAngle(entity);
        setupGraphicsContextAndDrawContent(entity, gc, 270.0);

        if (entity.getAnimationState() != AnimationState.DEATH) {
            drawHpBar(entity, gc);
        }

        if (shockwaveActive) {
            drawShockwave(entity, gc);
            drawShockwave(entity, gc);
        }

        gc.restore(); // Ripristina la telecamera normale a fine frame, azzerando lo shake per la UI globale
    }

    /** Gestisce il disegno di un'onda d'urto potenziata con effetto Glow ed energia interna */
    private void drawShockwave(IscatMasterModel entity, GraphicsContext gc) {
        shockwaveRadius += SHOCKWAVE_EXPANSION_SPEED * UU.UNIVERSE_TICK;

        double progress = shockwaveRadius / SHOCKWAVE_MAX_RADIUS;
        shockwaveAlpha = Math.max(0.0, 1.0 - progress);

        // Abbassa gradualmente l'intensità dello shake man mano che l'onda si allontana
        shakeIntensity = MAX_SHAKE_INTENSITY * (1.0 - progress);

        if (progress >= 1.0) {
            shockwaveActive = false;
            shakeIntensity = 0.0;
        } else {
            gc.save();

            double centerX = UU.mToPx(entity.getTransform().getTranslationX());
            double centerY = UU.mToPx(entity.getTransform().getTranslationY());

            double diameter = shockwaveRadius * 2;
            double topLeftX = centerX - shockwaveRadius;
            double topLeftY = centerY - shockwaveRadius;

            // Crea un riempimento interno che svanisce molto velocemente rispetto al bordo
            double fillAlpha = shockwaveAlpha * 0.15;
            gc.setFill(Color.rgb(255, 255, 255, fillAlpha));
            gc.fillOval(topLeftX, topLeftY, diameter, diameter);

            // Disegnamo un cerchio molto spesso ma quasi invisibile
            gc.setStroke(Color.rgb(255, 255, 255, shockwaveAlpha * 0.3));
            gc.setLineWidth(SHOCKWAVE_LINE_WIDTH * 3.5);
            gc.strokeOval(topLeftX, topLeftY, diameter, diameter);

            // EFFETTO GLOW
            gc.setStroke(Color.rgb(255, 255, 255, shockwaveAlpha * 0.6));
            gc.setLineWidth(SHOCKWAVE_LINE_WIDTH * 1.8);
            gc.strokeOval(topLeftX, topLeftY, diameter, diameter);

            // Il cuore dell'onda rimane bianco solido puro
            gc.setStroke(Color.rgb(255, 255, 255, shockwaveAlpha));
            gc.setLineWidth(SHOCKWAVE_LINE_WIDTH);
            gc.strokeOval(topLeftX, topLeftY, diameter, diameter);

            gc.restore();
        }
    }

    @Override
    protected void drawContent(IscatMasterModel entity, GraphicsContext gc,
                               double x, double y, double width, double height) {
        drawSprite(gc, x, y, width, height);
    }
}