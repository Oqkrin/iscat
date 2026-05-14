package uni.gaben.iscat.gamenex.universe.iscat_worm.iscat_worm_body_part;

import javafx.scene.canvas.GraphicsContext;
import uni.gaben.iscat.gamenex.lib.abstracts.AbstractEntityView;
import uni.gaben.iscat.gamenex.lib.interfaces.view.Drawable;
import uni.gaben.iscat.gamenex.lib.interfaces.view.DrawableSpriteSheet;
import uni.gaben.iscat.gamenex.model.GamenexModel;
import uni.gaben.iscat.utils.sprite.SpriteSheetsAnimator;
import uni.gaben.iscat.utils.sprite.SpriteSheetsParser;
import uni.gaben.iscat.utils.sprite.SpritesLibrary;

import static uni.gaben.iscat.gamenex.universe.iscat_worm.iscat_worm_body_part.IscatWormBodyPartSettings.*;

/**
 * Vista per i segmenti del corpo dell'Iscat Worm.
 * Implementa {@link Drawable} per uniformare il rendering a quello della testa e della coda.
 */
public class IscatWormBodyPartView extends AbstractEntityView implements Drawable<IscatWormBodyPartModel>, DrawableSpriteSheet {

    private static final String SPRITE_PATH = "/uni/gaben/iscat/sprites/iscat_worm_body_part.png";
    public static final double DRAW_SIZE = DIM_SPRITE * SCALE;

    private final SpriteSheetsParser spriteSheet;
    private final SpriteSheetsAnimator animator;

    public IscatWormBodyPartView() {
        // 1. Caricamento centralizzato dello spritesheet
        this.spriteSheet = SpritesLibrary.getInstance().getSprite(
                SPRITE_PATH,
                (int) DIM_SPRITE,
                (int) DIM_SPRITE
        );

        // 2. Configurazione animatore
        // Usiamo 0.045 come durata base per riflettere il divisore 0.45 originale
        this.animator = new SpriteSheetsAnimator(
                0.045,
                spriteSheet.getTotalFrames(),
                spriteSheet.getTotalStates()
        );
    }

    // --- Implementazione Getter Drawable ---

    @Override
    public SpriteSheetsParser getSpriteSheet() {
        return spriteSheet;
    }

    @Override
    public SpriteSheetsAnimator getAnimator() {
        return animator;
    }

    @Override
    public void draw(IscatWormBodyPartModel entity, GraphicsContext gc) {
        // Update del tempo di animazione (sincronizzato con il loop di gioco)
        animator.update(GamenexModel.TICKUNIT);

        // Calcolo posizione, angolo e dimensione
        setPos(entity);
        setAngle(entity);
        setSize(DRAW_SIZE);

        gc.save();

        // Traslazione al centro del segmento e rotazione correttiva
        gc.translate(cx, cy);
        gc.rotate(rotDeg + 180);

        // Il metodo drawSprite si occupa di:
        // - Chiedere all'animator il frame corretto
        // - Applicare il colore del tema attuale (Tint)
        // - Disegnare la porzione corretta dello spritesheet
        drawSprite(gc, 0, 0, w, h);

        gc.restore();

        // Overlay HP (solitamente disabilitato per i segmenti del corpo, ma disponibile)
        drawHpBar(entity, gc);
    }
}