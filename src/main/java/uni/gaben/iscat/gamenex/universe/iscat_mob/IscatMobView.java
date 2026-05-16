package uni.gaben.iscat.gamenex.universe.iscat_mob;

import javafx.scene.canvas.GraphicsContext;
import uni.gaben.iscat.gamenex.lib.abstracts.AbstractEntityView;
import uni.gaben.iscat.gamenex.lib.interfaces.view.Drawable;
import uni.gaben.iscat.gamenex.lib.interfaces.view.DrawableSpriteSheet;
import uni.gaben.iscat.gamenex.lib.utils.UU;
import uni.gaben.iscat.gamenex.model.GamenexModel;
import uni.gaben.iscat.utils.sprite.SpriteSheetsAnimator;
import uni.gaben.iscat.utils.sprite.SpriteSheetsParser;
import uni.gaben.iscat.utils.sprite.SpritesLibrary;

import static uni.gaben.iscat.gamenex.universe.iscat_mob.IscatMobSettings.*;

/**
 * Vista per i Mob Iscat.
 * Implementa {@link Drawable} per sfruttare il sistema di animazione centralizzato.
 */
public class IscatMobView extends AbstractEntityView implements Drawable<IscatMobModel> , DrawableSpriteSheet {

    // Costanti di rendering
    public static final double DRAW_SIZE = DIM_SPRITE * SCALE;
    private static final String SPRITE_PATH = "/uni/gaben/iscat/sprites/iscat.png";

    private final SpriteSheetsParser spriteSheet; //detiene lo sprite sheet
    private final SpriteSheetsAnimator animator; // anima lo sprite sheet

    public IscatMobView() {
        // 1. Carichiamo lo spritesheet tramite la libreria (per il caching dell'Image)
        this.spriteSheet = SpritesLibrary.getInstance().getSprite(
                SPRITE_PATH,
                DIM_SPRITE,
                DIM_SPRITE
        );

        // 2. Inizializziamo l'animatore con le dimensioni corrette
        this.animator = new SpriteSheetsAnimator(
                0.1, // Durata frame default (100ms)
                spriteSheet.getTotalFrames(),
                spriteSheet.getTotalStates()
        );
    }

    @Override
    public SpriteSheetsParser getSpriteSheet() {
        return spriteSheet;
    }

    @Override
    public SpriteSheetsAnimator getAnimator() {
        return animator;
    }

    @Override
    public void draw(IscatMobModel entity, GraphicsContext gc) {
        // Aggiornamento logica animazione
        animator.update(UU.UNIVERSE_TICK);

        // Impostiamo lo stato dell'animatore (es. se ha stati diversi per morte/attacco)
        // animator.setState(entity.getCurrentStateIndex());

        gc.save();

        // Setup trasformazioni
        setPos(entity);
        gc.translate(cx, cy);

        setAngle(entity);
        if(ROTATION_TOWARDS_PLAYER)
            gc.rotate(rotDeg + 180);

        setSize(DRAW_SIZE);

        // Il metodo drawSprite dell'interfaccia gestisce:
        // - Recupero frame corretto dall'animator
        // - Tinting dinamico dal ThemeManager
        // - Ritaglio dello spritesheet (Viewport)
        drawSprite(gc, 0, 0, w, h);

        gc.restore();

        // Overlay della barra HP
        drawHpBar(entity, gc);
    }
}