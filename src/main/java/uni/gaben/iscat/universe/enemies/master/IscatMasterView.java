package uni.gaben.iscat.universe.enemies.master;

import javafx.scene.canvas.GraphicsContext;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityView;
import uni.gaben.iscat.universe.lib.interfaces.view.Drawable;
import uni.gaben.iscat.universe.lib.interfaces.view.DrawableSpriteSheet;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.utils.AudioManager;
import uni.gaben.iscat.utils.sprite.SpriteSheetsAnimator;
import uni.gaben.iscat.utils.sprite.SpriteSheetsParser;
import uni.gaben.iscat.utils.sprite.SpritesLibrary;

import static uni.gaben.iscat.universe.enemies.master.IscatMasterSettings.*;
import static uni.gaben.iscat.universe.enemies.master.IscatMasterModel.AnimationState;

public class IscatMasterView extends AbstractEntityView<IscatMasterModel>
        implements Drawable<IscatMasterModel>, DrawableSpriteSheet {

    private static final String PATH_SINGLE_SHEET = "/uni/gaben/iscat/sprites/enemies/iscat_master.png";

    private final SpriteSheetsParser masterSheet;
    private final SpriteSheetsAnimator masterAnimator;

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
            maxFrames = 22;

            updateAnimatorState(targetRow);
            masterAnimator.update(UU.UNIVERSE_TICK);

            if (isRowCycleCompleted(maxFrames)) {
                entity.setEntranceDone(true);
                entity.setEnabled(true);
                entity.shockwave().trigger(.8, 15, 7);

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

        gc.save();

        setPos(entity);
        setAngle(entity);
        setupGraphicsContextAndDrawContent(entity, gc, 270.0);

        drawShockwave(gc, 0, 0, entity.shockwave());

        if (entity.getAnimationState() != AnimationState.DEATH) {
            drawHpBar(entity, gc);
        }

        gc.restore();
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

    @Override
    protected void drawContent(IscatMasterModel entity, GraphicsContext gc, double x, double y, double width, double height) {
        int currentRow = masterAnimator.getCurrentState();
        int maxFrames = getFramesCountForRow(currentRow);

        int localFrame = (int) (masterAnimator.getTime() / (1.0 / 6.0)) % maxFrames;

        double frameW = masterSheet.frameWidth;
        double frameH = masterSheet.frameHeight;
        double sx = localFrame * frameW;
        double sy = currentRow * frameH;

        gc.drawImage(masterSheet.getSheet(), sx, sy, frameW, frameH, x, y, width, height);
    }
}