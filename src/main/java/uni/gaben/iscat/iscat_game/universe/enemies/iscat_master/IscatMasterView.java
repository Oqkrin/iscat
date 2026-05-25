package uni.gaben.iscat.iscat_game.universe.enemies.iscat_master;

import javafx.scene.canvas.GraphicsContext;
import uni.gaben.iscat.iscat_game.lib.abstracts.AbstractEntityView;
import uni.gaben.iscat.iscat_game.lib.interfaces.view.Drawable;
import uni.gaben.iscat.iscat_game.lib.interfaces.view.DrawableSpriteSheet;
import uni.gaben.iscat.iscat_game.utils.UU;
import uni.gaben.iscat.utils.sprite.SpriteSheetsAnimator;
import uni.gaben.iscat.utils.sprite.SpriteSheetsParser;
import uni.gaben.iscat.utils.sprite.SpritesLibrary;

import static uni.gaben.iscat.iscat_game.universe.enemies.iscat_master.IscatMasterSettings.*;
import static uni.gaben.iscat.iscat_game.universe.enemies.iscat_master.IscatMasterModel.AnimationState;

public class IscatMasterView extends AbstractEntityView<IscatMasterModel>
        implements Drawable<IscatMasterModel>, DrawableSpriteSheet {

    private static final String PATH_MAIN     = "/uni/gaben/iscat/sprites/enemies/iscat_master.png";
    private static final String PATH_ENTRANCE = "/uni/gaben/iscat/sprites/enemies/iscat_master_entrance.png";
    private static final String PATH_ATTACK1  = "/uni/gaben/iscat/sprites/enemies/iscat_master_attack1.png";
    private static final String PATH_ATTACK2  = "/uni/gaben/iscat/sprites/enemies/iscat_master_attack2.png";
    private static final String PATH_ATTACK3  = "/uni/gaben/iscat/sprites/enemies/iscat_master_attack3.png";
    private static final String PATH_ATTACK4  = "/uni/gaben/iscat/sprites/enemies/iscat_master_attack4.png";
    private static final String PATH_DEATH    = "/uni/gaben/iscat/sprites/enemies/iscat_master_death.png";

    private final SpriteSheetsParser mainSheet;
    private final SpriteSheetsParser entranceSheet;
    private final SpriteSheetsParser attack1Sheet;
    private final SpriteSheetsParser attack2Sheet;
    private final SpriteSheetsParser attack3Sheet;
    private final SpriteSheetsParser attack4Sheet;
    private final SpriteSheetsParser deathSheet;

    private final SpriteSheetsAnimator mainAnimator;
    private final SpriteSheetsAnimator entranceAnimator;
    private final SpriteSheetsAnimator attack1Animator;
    private final SpriteSheetsAnimator attack2Animator;
    private final SpriteSheetsAnimator attack3Animator;
    private final SpriteSheetsAnimator attack4Animator;
    private final SpriteSheetsAnimator deathAnimator;

    // Active pair — updated every draw() call based on model state
    private SpriteSheetsParser activeSheet;
    private SpriteSheetsAnimator activeAnimator;

    public IscatMasterView() {
        spriteScale = IscatMasterSettings.ISCATMASTER.scale;

        mainSheet     = load(PATH_MAIN);
        entranceSheet = load(PATH_ENTRANCE);
        attack1Sheet  = load(PATH_ATTACK1);
        attack2Sheet  = load(PATH_ATTACK2);
        attack3Sheet  = load(PATH_ATTACK3);
        attack4Sheet  = load(PATH_ATTACK4);
        deathSheet    = load(PATH_DEATH);

        mainAnimator     = animator(mainSheet);
        entranceAnimator = animator(entranceSheet);
        attack1Animator  = animator(attack1Sheet);
        attack2Animator  = animator(attack2Sheet);
        attack3Animator  = animator(attack3Sheet);
        attack4Animator  = animator(attack4Sheet);
        deathAnimator    = animator(deathSheet);

        // Always start from entrance
        activeSheet    = entranceSheet;
        activeAnimator = entranceAnimator;
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────

    private SpriteSheetsParser load(String path) {
        return SpritesLibrary.getInstance().getSprite(path, (int) ISCATMASTER.dimSprite, (int) ISCATMASTER.dimSprite);
    }

    private SpriteSheetsAnimator animator(SpriteSheetsParser sheet) {
        return new SpriteSheetsAnimator(
                1.0 / 6.0,
                sheet != null ? sheet.getTotalFrames() : 1,
                sheet != null ? sheet.getTotalStates() : 1
        );
    }

    // ── INTERFACE ─────────────────────────────────────────────────────────────

    @Override
    public SpriteSheetsParser getSpriteSheet() { return activeSheet; }

    @Override
    public SpriteSheetsAnimator getAnimator() { return activeAnimator; }

    // ── DRAW ──────────────────────────────────────────────────────────────────

    @Override
    public void draw(IscatMasterModel entity, GraphicsContext gc) {
        if (entity == null) return;

        if (!entity.isEntranceDone()) {
            activeSheet    = entranceSheet;
            activeAnimator = entranceAnimator;
            entranceAnimator.update(UU.UNIVERSE_TICK);

            if (entranceAnimator.hasCompletedCycle()) {
                entity.setEntranceDone(true);
                entity.setEnabled(true);
            }

        } else {

            // Select sheet and animator based on model state
            switch (entity.getAnimationState()) {
                case ATTACK1 -> { activeSheet = attack1Sheet; activeAnimator = attack1Animator; }
                case ATTACK2 -> { activeSheet = attack2Sheet; activeAnimator = attack2Animator; }
                case ATTACK3 -> { activeSheet = attack3Sheet; activeAnimator = attack3Animator; }
                case ATTACK4 -> { activeSheet = attack4Sheet; activeAnimator = attack4Animator; }
                case DEATH   -> { activeSheet = deathSheet;   activeAnimator = deathAnimator;   }
                default      -> { activeSheet = mainSheet;    activeAnimator = mainAnimator;     }
            }

            activeAnimator.update(UU.UNIVERSE_TICK);

            AnimationState state = entity.getAnimationState();

            // Attack animations: reset and return to IDLE when done
            if (state != AnimationState.IDLE && state != AnimationState.DEATH
                    && activeAnimator.hasCompletedCycle()) {
                activeAnimator.reset();
                entity.setAnimationState(AnimationState.IDLE);
            }

            // Death animation: mark for removal when done
            if (state == AnimationState.DEATH && deathAnimator.hasCompletedCycle()) {
                entity.completeKill();
            }
        }

        setPos(entity);
        setAngle(entity);
        setupGraphicsContextAndDrawContent(entity, gc, 270.0);

        if (entity.getAnimationState() != AnimationState.DEATH) {
            drawHpBar(entity, gc);
        }
    }

    @Override
    protected void drawContent(IscatMasterModel entity, GraphicsContext gc,
                               double x, double y, double width, double height) {
        drawSprite(gc, x, y, width, height);
    }
}