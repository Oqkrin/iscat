package uni.gaben.iscat.gamenex.universe.eater;

import javafx.scene.canvas.GraphicsContext;
import uni.gaben.iscat.gamenex.lib.abstracts.AbstractEntityView;
import uni.gaben.iscat.gamenex.lib.interfaces.view.Drawable;
import uni.gaben.iscat.gamenex.lib.interfaces.view.DrawableSpriteSheet;
import uni.gaben.iscat.gamenex.model.GamenexModel;
import uni.gaben.iscat.utils.sprite.SpriteSheetsAnimator;
import uni.gaben.iscat.utils.sprite.SpriteSheetsParser;
import uni.gaben.iscat.utils.sprite.SpritesLibrary;

import static uni.gaben.iscat.gamenex.universe.eater.EaterSettings.DIM_SPRITE;
import static uni.gaben.iscat.gamenex.universe.eater.EaterSettings.SCALE;

public class EaterView extends AbstractEntityView implements Drawable<EaterModel>, DrawableSpriteSheet {

    private final SpriteSheetsParser spriteSheetsParser;
    private final SpriteSheetsAnimator animator;

    // Path corretto (corretto anche il typo "Pah")
    private static final String SPRITE_SHEET_PATH = "/uni/gaben/iscat/sprites/eater.png";
    public static final double DRAW_SIZE = DIM_SPRITE * SCALE;

    public EaterView() {
        // 1. Inizializzazione Parser
        this.spriteSheetsParser = SpritesLibrary.getInstance().getSprite(SPRITE_SHEET_PATH, DIM_SPRITE, DIM_SPRITE);

        // 2. Inizializzazione Animatore con i dati reali del parser
        // Usiamo ad esempio 24 FPS di default (1.0 / 24.0 ≈ 0.041 secondi per frame)
        this.animator = new SpriteSheetsAnimator(
                1.0 / 24.0,
                spriteSheetsParser.getTotalFrames(),
                spriteSheetsParser.getTotalStates()
        );
    }

    // --- Implementazione obbligatoria dei Getter per Drawable ---

    @Override
    public SpriteSheetsParser getSpriteSheet() {
        return spriteSheetsParser;
    }

    @Override
    public SpriteSheetsAnimator getAnimator() {
        return animator;
    }

    @Override
    public void draw(EaterModel entity, GraphicsContext gc) {
        // 1. Update temporale (sincronizzato con il gioco)
        animator.update(GamenexModel.TICKUNIT);

        // 2. Logica di stato (Esempio: se l'Eater sta mangiando, cambia riga dello spritesheet)
        // if (entity.isEating()) animator.setState(1); else animator.setState(0);

        // 3. Setup trasformazioni
        setAngle(entity);
        setPos(entity);
        setSize(DRAW_SIZE);

        gc.save();
        gc.translate(cx, cy);

        // Se l'Eater deve guardare verso la direzione di movimento,
        // aggiungi eventuali rotazioni extra qui (es. gc.rotate(rotDeg))
        gc.rotate(rotDeg);

        // 4. Delega il disegno all'interfaccia Drawable.
        // Nota: x e y sono 0 perché usiamo il translate del GraphicsContext
        drawSprite(gc, 0, 0, w, h);

        gc.restore();

        // 5. Overlay HP
        drawHpBar(entity, gc);
    }
}