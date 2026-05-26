package uni.gaben.iscat.universe.iscats.mob;

import javafx.scene.canvas.GraphicsContext;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityView;
import uni.gaben.iscat.universe.lib.interfaces.view.Drawable;
import uni.gaben.iscat.universe.lib.interfaces.view.DrawableSpriteSheet;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.utils.sprite.SpriteSheetsAnimator;
import uni.gaben.iscat.utils.sprite.SpriteSheetsParser;
import uni.gaben.iscat.utils.sprite.SpritesLibrary;

import static uni.gaben.iscat.universe.iscats.mob.IscatMobSettings.*;

/**
 * Gestisce il rendering grafico dell'IscatMob caricando lo spritesheet e la barra della vita.
 */
public class IscatMobView extends AbstractEntityView<IscatMobModel>
        implements Drawable<IscatMobModel>, DrawableSpriteSheet {

    private final SpriteSheetsParser spriteSheetsParser; // Parser dello spritesheet dell'entità
    private final SpriteSheetsAnimator animator;         // Gestore del frame rate dell'animazione

    private static final String SPRITE_SHEET_PATH = "/uni/gaben/iscat/sprites/enemies/iscat_mob.png";

    public IscatMobView() {
        // Imposta la scala di rendering dello sprite definita nei Settings
        spriteScale = ISCATMOB.scale;

        // Carica l'immagine tramite la libreria centrale dei file multimediali
        this.spriteSheetsParser = SpritesLibrary.getInstance()
                .getSprite(SPRITE_SHEET_PATH, (int) ISCATMOB.dimSprite, (int) ISCATMOB.dimSprite);

        // Configura la velocità di riproduzione dei fotogrammi (24 FPS standard)
        this.animator = new SpriteSheetsAnimator(
                1.0 / 24.0,
                spriteSheetsParser != null ? spriteSheetsParser.getTotalFrames() : 1,
                spriteSheetsParser != null ? spriteSheetsParser.getTotalStates() : 1
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
     * Metodo di disegno principale richiamato dal ciclo di rendering grafico di JavaFX.
     */
    @Override
    public void draw(IscatMobModel entity, GraphicsContext gc) {
        if (entity == null) return;

        // Avanza il frame dell'animazione dello sprite
        animator.update(UU.UNIVERSE_TICK);

        // Aggiorna le coordinate cx e cy della View allineandole al modello fisico globale
        setPos(entity);
        // Sincronizza l'angolo di disegno con l'angolo logico calcolato dall'IA
        setAngle(entity);

        double structuralOffset = 180.0;

        // Disegna lo sprite applicando traslazione e rotazione (Salva e ripristina la matrice internamente)
        setupGraphicsContextAndDrawContent(entity, gc, structuralOffset);

        drawHpBar(entity, gc);
    }

    /**
     * Disegna il contenuto specifico dell'entità (chiamato internamente da setupGraphicsContextAndDrawContent).
     * Qui dentro il canvas è centrato sull'entità e ruotato.
     */
    @Override
    protected void drawContent(IscatMobModel entity, GraphicsContext gc,
                               double x, double y, double width, double height) {

        // Disegna l'immagine dello sprite corrente estratto dall'animatore
        drawSprite(gc, x, y, width, height);
    }
}