package uni.gaben.iscat.iscat_game.universe.iscats.bomber;

import javafx.scene.canvas.GraphicsContext;
import uni.gaben.iscat.iscat_game.lib.abstracts.AbstractEntityView;
import uni.gaben.iscat.iscat_game.lib.interfaces.view.Drawable;
import uni.gaben.iscat.iscat_game.lib.interfaces.view.DrawableSpriteSheet;
import uni.gaben.iscat.iscat_game.utils.UU;
import uni.gaben.iscat.utils.sprite.SpriteSheetsAnimator;
import uni.gaben.iscat.utils.sprite.SpriteSheetsParser;
import uni.gaben.iscat.utils.sprite.SpritesLibrary;

import static uni.gaben.iscat.iscat_game.universe.iscats.bomber.IscatBomberSettings.*;

/**
 * Vista per l'IscatBomber.
 * Segue il pattern standard gamenex: {@link AbstractEntityView} per la pipeline
 * di rendering (translate + rotate) e {@link DrawableSpriteSheet} per il
 * campionamento dal foglio di sprite con tint globale automatico via ThemeManager.
 */
public class IscatBomberView extends AbstractEntityView<IscatBomberModel>
        implements Drawable<IscatBomberModel>, DrawableSpriteSheet {

    // Percorso assoluto classpath — il file è in resources/uni/gaben/iscat/sprites/enemies/
    private static final String SPRITE_PATH = "/uni/gaben/iscat/sprites/enemies/iscat_bomber.png";

    private final SpriteSheetsParser spriteSheet;
    private final SpriteSheetsAnimator animator;

    public IscatBomberView() {
        spriteScale = ISCATBOMBER.scale;

        // Carica lo spritesheet tramite la libreria centrale (cache automatica)
        this.spriteSheet = SpritesLibrary.getInstance()
                .getSprite(SPRITE_PATH, (int) ISCATBOMBER.dimSprite, (int) ISCATBOMBER.dimSprite);

        // Configura l'animatore con i frame del foglio
        this.animator = new SpriteSheetsAnimator(
                UU.UNIVERSE_TICK * 2,
                spriteSheet != null ? spriteSheet.getTotalFrames() : 1,
                spriteSheet != null ? spriteSheet.getTotalStates() : 1
        );
    }

    // --- DrawableSpriteSheet ---

    @Override
    public SpriteSheetsParser getSpriteSheet() { return spriteSheet; }

    @Override
    public SpriteSheetsAnimator getAnimator() { return animator; }

    // --- Drawable<IscatBomberModel> ---

    @Override
    public void draw(IscatBomberModel entity, GraphicsContext gc) {
        animator.update(UU.UNIVERSE_TICK);
        // +90° perché il bomber punta verso l'alto nel file PNG
        setupGraphicsContextAndDrawContent(entity, gc, 90.0);
    }

    // --- AbstractEntityView<IscatBomberModel> ---

    @Override
    protected void drawContent(IscatBomberModel entity, GraphicsContext gc,
                               double x, double y, double width, double height) {
        drawSprite(gc, x, y, width, height);
        drawHpBar(entity, gc);
    }
}
