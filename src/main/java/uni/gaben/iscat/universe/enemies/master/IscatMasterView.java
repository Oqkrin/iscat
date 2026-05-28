package uni.gaben.iscat.universe.enemies.master;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityView;
import uni.gaben.iscat.universe.lib.interfaces.view.Drawable;
import uni.gaben.iscat.universe.lib.interfaces.view.DrawableSpriteSheet;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.utils.AudioManager;
import uni.gaben.iscat.utils.sprite.SpriteSheetsAnimator;
import uni.gaben.iscat.utils.sprite.SpriteSheetsParser;
import uni.gaben.iscat.utils.sprite.SpritesLibrary;
import uni.gaben.iscat.utils.theme.ThemeManager;

import java.util.Objects;

import static uni.gaben.iscat.universe.enemies.master.IscatMasterSettings.*;
import static uni.gaben.iscat.universe.enemies.master.IscatMasterModel.AnimationState;

public class IscatMasterView extends AbstractEntityView<IscatMasterModel>
        implements Drawable<IscatMasterModel>, DrawableSpriteSheet {

    private static final String PATH_SINGLE_SHEET = "/uni/gaben/iscat/sprites/enemies/iscat_master.png";

    private final SpriteSheetsParser masterSheet;
    private final SpriteSheetsAnimator masterAnimator;

    // CACHE DEI FRAME: Matrice di singoli frame microscopici tagliati all'avvio
    private final Image[][] slicedFrames;

    private boolean deathSfxTriggered = false;
    private int lastRow = -1;

    public IscatMasterView() {
        spriteScale = IscatMasterSettings.ISCATMASTER.scale;

        masterSheet = SpritesLibrary.getInstance().getSprite(
                PATH_SINGLE_SHEET,
                (int) ISCATMASTER.dimSprite,
                (int) ISCATMASTER.dimSprite
        );

        masterAnimator = new SpriteSheetsAnimator(
                1.0 / 6.0,
                masterSheet != null ? masterSheet.getTotalFrames() : 1,
                masterSheet != null ? masterSheet.getTotalStates() : 1
        );

        int totalRows = 7;
        int dim = (int) ISCATMASTER.dimSprite;
        this.slicedFrames = new Image[totalRows][];

        // Carichiamo l'immagine sorgente nativa direttamente dal flusso risorse
        Image giantSheet = new Image(Objects.requireNonNull(getClass().getResourceAsStream(PATH_SINGLE_SHEET)));

        for (int row = 0; row < totalRows; row++) {
            int framesCount = getFramesCountForRow(row);
            this.slicedFrames[row] = new Image[framesCount];

            for (int col = 0; col < framesCount; col++) {
                // Estraiamo la sub-image del singolo frame per isolarla dal file gigante
                this.slicedFrames[row][col] = new WritableImage(
                        giantSheet.getPixelReader(),
                        col * dim,
                        row * dim,
                        dim,
                        dim
                );
            }
        }
    }

    @Override public SpriteSheetsParser getSpriteSheet() { return masterSheet; }
    @Override public SpriteSheetsAnimator getAnimator() { return masterAnimator; }

    @Override
    public void draw(IscatMasterModel entity, GraphicsContext gc) {
        if (entity == null) return;

        int targetRow;
        int maxFrames;

        if (!entity.isEntranceDone()) {
            targetRow = 0;
            maxFrames = getFramesCountForRow(targetRow);
            updateAnimatorState(targetRow);
            masterAnimator.update(UU.UNIVERSE_TICK);

            if (isRowCycleCompleted(maxFrames)) {
                entity.setEntranceDone(true);
                entity.setEnabled(true);
                entity.shockwave().trigger(2.0, 1500, 15);
                AudioManager.getInstance().playSFX("shockwave");
                AudioManager.getInstance().playSFX("laugh");
                updateAnimatorState(1);
            }
        } else {
            AnimationState modelState = entity.getAnimationState();
            targetRow = mapStateToRow(modelState);
            maxFrames = getFramesCountForRow(targetRow);

            updateAnimatorState(targetRow);
            masterAnimator.update(UU.UNIVERSE_TICK);

            if (modelState == AnimationState.DEATH && !deathSfxTriggered) {
                deathSfxTriggered = true;
                AudioManager.getInstance().playSFX("shockwave");
            }

            if (modelState != AnimationState.IDLE && modelState != AnimationState.DEATH && isRowCycleCompleted(maxFrames)) {
                updateAnimatorState(1);
                entity.setAnimationState(AnimationState.IDLE);
            }

            if (modelState == AnimationState.DEATH && isRowCycleCompleted(maxFrames)) {
                entity.completeKill();
            }
        }

        setupGraphicsContextAndDrawContent(entity, gc, 270.0);
    }

    @Override
    protected void drawContent(IscatMasterModel entity, GraphicsContext gc, double x, double y, double width, double height) {
        int currentRow = masterAnimator.getCurrentState();
        int maxFrames = getFramesCountForRow(currentRow);
        double defaultFrameDuration = 1.0 / 6.0;

        // Calcoliamo l'indice esatto del frame corrente
        int localFrame = (int) (masterAnimator.getTime() / defaultFrameDuration) % maxFrames;

        if (currentRow < slicedFrames.length && localFrame < slicedFrames[currentRow].length) {
            Image smallSingleFrame = slicedFrames[currentRow][localFrame];

            Image tintedFrame = ThemeManager.getInstance().getTintedImage(
                    smallSingleFrame,
                    ThemeManager.getInstance().globalTintProperty().get()
            );

            gc.drawImage(tintedFrame, x, y, width, height);
        }

        drawShockwave(gc, 0, 0, entity.shockwave());

        if (entity.getAnimationState() != AnimationState.DEATH) {
            drawHpBar(entity, gc);
        }
    }

    private void updateAnimatorState(int row) {
        masterAnimator.setState(row);
        if (row != lastRow) {
            masterAnimator.setTime(0);
            lastRow = row;
        }
    }

    private boolean isRowCycleCompleted(int maxFrames) {
        double defaultFrameDuration = 1.0 / 6.0;
        double totalRowDuration = maxFrames * defaultFrameDuration;
        return masterAnimator.getTime() >= totalRowDuration;
    }

    private int getFramesCountForRow(int row) {
        return switch (row) {
            case 0  -> 21;
            case 1  -> 4;
            case 2  -> 3;
            case 3  -> 7;
            case 4  -> 8;
            case 5  -> 5;
            case 6  -> 6;
            default -> 1;
        };
    }

    private int mapStateToRow(AnimationState state) {
        return switch (state) {
            case IDLE    -> 1;
            case ATTACK1 -> 2;
            case ATTACK2 -> 3;
            case ATTACK3 -> 4;
            case ATTACK4 -> 5;
            case DEATH   -> 6;
        };
    }
}