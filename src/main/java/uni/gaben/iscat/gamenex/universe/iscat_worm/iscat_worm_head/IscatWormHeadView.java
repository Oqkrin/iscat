package uni.gaben.iscat.gamenex.universe.iscat_worm.iscat_worm_head;

import javafx.scene.canvas.GraphicsContext;
import uni.gaben.iscat.gamenex.lib.abstracts.AbstractEntityView;
import uni.gaben.iscat.gamenex.lib.interfaces.view.Drawable;
import uni.gaben.iscat.gamenex.lib.interfaces.view.DrawableSpriteSheet;
import uni.gaben.iscat.gamenex.lib.utils.UU;
import uni.gaben.iscat.gamenex.model.GamenexModel;
import uni.gaben.iscat.utils.sprite.SpriteSheetsAnimator;
import uni.gaben.iscat.utils.sprite.SpriteSheetsParser;
import uni.gaben.iscat.utils.sprite.SpritesLibrary;

import static uni.gaben.iscat.gamenex.universe.iscat_worm.iscat_worm_head.IscatWormHeadSettings.*;

/**
 * Vista per la testa dell'Iscat Worm.
 * Sfrutta il sistema centralizzato di animazione e rendering per garantire
 * fluidità e coerenza cromatica con il tema globale.
 */
public class IscatWormHeadView extends AbstractEntityView implements Drawable<IscatWormHeadModel>, DrawableSpriteSheet {

    private static final String SPRITE_PATH = "/uni/gaben/iscat/sprites/iscat_worm_head.png";
    public static final double DRAW_SIZE = DIM_SPRITE * SCALE;

    private final SpriteSheetsParser spriteSheet;
    private final SpriteSheetsAnimator animator;

    public IscatWormHeadView() {
        // 1. Recupero dello spritesheet dalla libreria condivisa
        this.spriteSheet = SpritesLibrary.getInstance().getSprite(
                SPRITE_PATH,
                (int) DIM_SPRITE,
                (int) DIM_SPRITE
        );

        // 2. Configurazione dell'animatore
        // frameDuration impostata a ~0.035s per riflettere il divisore 0.35 del codice originale
        this.animator = new SpriteSheetsAnimator(
                0.035,
                spriteSheet.getTotalFrames(),
                spriteSheet.getTotalStates()
        );
    }

    // --- Implementazione dei getter richiesti da Drawable ---

    @Override
    public SpriteSheetsParser getSpriteSheet() {
        return spriteSheet;
    }

    @Override
    public SpriteSheetsAnimator getAnimator() {
        return animator;
    }

    @Override
    public void draw(IscatWormHeadModel entity, GraphicsContext gc) {
        // Avanzamento temporale dell'animazione
        animator.update(UU.UNIVERSE_TICK);

        // Setup trasformazioni e dimensioni
        setPos(entity);
        setAngle(entity);
        setSize(DRAW_SIZE);

        gc.save();

        // Centratura e rotazione dello sprite (+180 per correggere l'orientamento)
        gc.translate(cx, cy);
        gc.rotate(rotDeg + 180);

        // Rendering dello sprite: gestisce automaticamente il frame corrente e il tinting
        drawSprite(gc, 0, 0, w, h);

        gc.restore();

        // Overlay della barra della salute
        drawHpBar(entity, gc);
    }
}