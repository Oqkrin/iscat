package uni.gaben.iscat.iscat_game.universe.enemies.iscat_core;

import javafx.scene.canvas.GraphicsContext;
import uni.gaben.iscat.iscat_game.lib.abstracts.AbstractEntityView;
import uni.gaben.iscat.iscat_game.lib.interfaces.view.Drawable;
import uni.gaben.iscat.iscat_game.lib.interfaces.view.DrawableSpriteSheet;
import uni.gaben.iscat.iscat_game.utils.UU;
import uni.gaben.iscat.utils.sprite.SpriteSheetsAnimator;
import uni.gaben.iscat.utils.sprite.SpriteSheetsParser;
import uni.gaben.iscat.utils.sprite.SpritesLibrary;

import static uni.gaben.iscat.iscat_game.universe.enemies.iscat_core.IscatCoreSettings.*;
import static uni.gaben.iscat.iscat_game.universe.heart.HeartSettings.DIM_SPRITE;

/**
 * Gestisce il rendering grafico dell'IscatCore
 * caricando lo spritesheet e la barra della vita.
 */
public class IscatCoreView extends AbstractEntityView<IscatCoreModel>
        implements Drawable<IscatCoreModel>, DrawableSpriteSheet {

    private final SpriteSheetsParser spriteSheetsParser;
    private final SpriteSheetsAnimator animator;

    private static final String SPRITE_SHEET_PATH =
            "/uni/gaben/iscat/sprites/enemies/iscat_core.png";

    public IscatCoreView() {

        // Imposta la scala di rendering dello sprite definita nei Settings
        spriteScale = ISCATCORE.scale;

        // Carica l'immagine tramite la libreria centrale dei file multimediali
        this.spriteSheetsParser = SpritesLibrary.getInstance()
                .getSprite(
                        SPRITE_SHEET_PATH,
                        (int) ISCATCORE.dimSprite,
                        (int) ISCATCORE.dimSprite
                );

        // Configura la velocità di riproduzione dei fotogrammi
        this.animator = new SpriteSheetsAnimator(
                UU.UNIVERSE_TICK*10,
                spriteSheetsParser != null
                        ? spriteSheetsParser.getTotalFrames()
                        : 1,
                spriteSheetsParser != null
                        ? spriteSheetsParser.getTotalStates()
                        : 1
        );
    }

    @Override
    public SpriteSheetsParser getSpriteSheet() {
        return spriteSheetsParser;
    }

    @Override
    public SpriteSheetsAnimator getAnimator() {
        return animator;
    }

    /**
     * Metodo di disegno principale richiamato dal rendering JavaFX.
     */
    @Override
    public void draw(IscatCoreModel entity, GraphicsContext gc) {
        if (entity == null) return;

        // Avanza il frame dell'animazione dello sprite

        double healthRatio = entity.getLife() / entity.getMaxLife();
        double animationSpeedMultiplier = 1.0 / healthRatio;
        animator.setTime(entity.getLifetime() * animationSpeedMultiplier);


        // Aggiorna posizione e rotazione
        setPos(entity);
        setAngle(entity);

        double structuralOffset = 270.0;

        // Disegna sprite con trasformazioni
        setupGraphicsContextAndDrawContent(
                entity,
                gc,
                structuralOffset
        );

        drawHpBar(entity, gc);
    }

    /**
     * Disegna il contenuto specifico dell'entità.
     */
    @Override
    protected void drawContent(
            IscatCoreModel entity,
            GraphicsContext gc,
            double x,
            double y,
            double width,
            double height
    ) {

        drawSprite(gc, x, y, width, height);
    }
}