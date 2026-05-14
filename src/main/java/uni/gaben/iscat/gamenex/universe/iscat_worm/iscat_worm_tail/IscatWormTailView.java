package uni.gaben.iscat.gamenex.universe.iscat_worm.iscat_worm_tail;

import javafx.scene.canvas.GraphicsContext;
import uni.gaben.iscat.gamenex.lib.abstracts.AbstractEntityView;
import uni.gaben.iscat.gamenex.lib.interfaces.view.Drawable;
import uni.gaben.iscat.gamenex.lib.interfaces.view.DrawableSpriteSheet;
import uni.gaben.iscat.gamenex.model.GamenexModel;
import uni.gaben.iscat.utils.sprite.SpriteSheetsAnimator;
import uni.gaben.iscat.utils.sprite.SpriteSheetsParser;
import uni.gaben.iscat.utils.sprite.SpritesLibrary;

import static uni.gaben.iscat.gamenex.universe.iscat_worm.iscat_worm_tail.IscatWormTailSettings.*;

public class IscatWormTailView extends AbstractEntityView implements Drawable<IscatWormTailModel>, DrawableSpriteSheet {

    // Costanti centralizzate
    private static final String SPRITE_PATH = "/uni/gaben/iscat/sprites/iscat_worm_tail.png";
    public static final double DRAW_SIZE = DIM_SPRITE * SCALE;

    private final SpriteSheetsParser spriteSheet;
    private final SpriteSheetsAnimator animator;

    public IscatWormTailView() {
        // 1. Carichiamo lo spritesheet tramite la libreria dedicata
        this.spriteSheet = SpritesLibrary.getInstance().getSprite(
                SPRITE_PATH,
                (int) DIM_SPRITE,
                (int) DIM_SPRITE
        );

        // 2. Inizializziamo l'animatore (es. 12 FPS -> circa 0.08s per frame)
        this.animator = new SpriteSheetsAnimator(
                0.08,
                spriteSheet.getTotalFrames(),
                spriteSheet.getTotalStates()
        );
    }

    // --- Metodi richiesti dall'interfaccia Drawable ---

    @Override
    public SpriteSheetsParser getSpriteSheet() {
        return spriteSheet;
    }

    @Override
    public SpriteSheetsAnimator getAnimator() {
        return animator;
    }

    @Override
    public void draw(IscatWormTailModel entity, GraphicsContext gc) {
        // Avanzamento del tempo dell'animazione
        animator.update(GamenexModel.TICKUNIT);

        // Setup coordinate e dimensioni
        setPos(entity);
        setAngle(entity);
        setSize(DRAW_SIZE);

        gc.save();

        // Traslazione e rotazione (manteniamo +180 se il verso dello sprite è invertito)
        gc.translate(cx, cy);
        gc.rotate(rotDeg + 180);

        // Disegno automatico: drawSprite gestisce internamente il ritaglio del frame
        // calcolato dall'animator e l'applicazione del colore (Tinting) dal ThemeManager.
        drawSprite(gc, 0, 0, w, h);

        gc.restore();

        // Barra HP opzionale (spesso le code dei vermi condividono la vita con la testa,
        // ma se è un'entità separata, questo la disegna correttamente).
        drawHpBar(entity, gc);
    }
}