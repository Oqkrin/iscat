package uni.gaben.iscat.universe.entity.special.master;

import org.dyn4j.geometry.MassType;
import uni.gaben.iscat.universe.entity.GenericEntityModel;
import uni.gaben.iscat.universe.entity.GenericEntitySettings;
import uni.gaben.iscat.universe.entity.GenericEntityFactory;
import uni.gaben.iscat.utils.Updatable;
import uni.gaben.iscat.universe.UniverseWaveController;

public class IscatMasterModel extends GenericEntityModel implements Updatable {

    public static final String ENTITY_KEY = "iscat_master";

    // The ENTRANCE state replaces the boolean entranceDone flag
    public enum AnimationState { ENTRANCE, IDLE, ATTACK1, ATTACK2, ATTACK3, ATTACK4, DEATH }
    private AnimationState animationState = AnimationState.ENTRANCE;

    private boolean completeKillCalled = false;

    private final UniverseWaveController waveController;

    public IscatMasterModel(double x, double y, UniverseWaveController waveController) {
        this(x, y, waveController, loadSettings());
    }

    private IscatMasterModel(double x, double y, UniverseWaveController waveController,
                             GenericEntitySettings s) {
        super(x, y, s);
        this.waveController = waveController;

        setMass(MassType.FIXED_ANGULAR_VELOCITY);

        // Disabilitato di default fino al completamento dell'animazione di spawn intro
        setEnabled(false);
    }

    /**
     * Carica i parametri del boss direttamente dal file JSON.
     * Rimosso ogni riferimento a IscatDB ed EnemyDAO.
     */
    private static GenericEntitySettings loadSettings() {
        // Cerchiamo la chiave del boss direttamente nella cache del factory JSON
        GenericEntitySettings cached = GenericEntityFactory.getCache().get(ENTITY_KEY);

        if (cached != null) {
            return cached;
        }

        // Fallback di sicurezza se la cache JSON non contiene il boss
        System.err.println("[BOSS WARNING] Chiave '" + ENTITY_KEY + "' non trovata nel JSON. Uso i valori di fallback.");
        GenericEntitySettings s = new GenericEntitySettings();
        s.entityKey      = ENTITY_KEY;
        s.name           = "Iscat Master";
        s.initLife       = 5000;
        s.scale          = 1;
        s.linearDamping  = 1;
        s.maxVelocity    = 1;
        s.xpReward       = 1;
        s.spritePath     = "/uni/gaben/iscat/textures/enemies/iscat_master.png";
        return s;
    }

    public AnimationState getAnimationState() { return animationState; }

    @Override
    public int getState() {
        return animationState.ordinal();
    }

    // State changes automatically reset the animation timer so the View always starts at frame 0
    public void setAnimationState(AnimationState state) {
        if (this.animationState != state) {
            this.animationState = state;
            this.setStateTime(0.0);
        }
    }

    @Override
    public void update(double dt) {
        super.update(dt);

        if (animationState == AnimationState.ENTRANCE) {
            double duration = getFramesForState(AnimationState.ENTRANCE) * getFrameDuration();
            if (getStateTime() >= duration) {
                setEnabled(true);
                setAnimationState(AnimationState.IDLE);
                shockwave().trigger(2.0, 1500, 15);
                try {
                    uni.gaben.iscat.utils.AudioManager.getInstance().playSFX("shockwave");
                    uni.gaben.iscat.utils.AudioManager.getInstance().playSFX("laugh");
                } catch (Exception e) {
                    // Ignore if audio is unavailable
                }
            }
        } else if (animationState != AnimationState.IDLE && animationState != AnimationState.DEATH) {
            double duration = getFramesForState(animationState) * getFrameDuration();
            if (getStateTime() >= duration) {
                setAnimationState(AnimationState.IDLE);
            }
        } else if (animationState == AnimationState.DEATH) {
            double duration = getFramesForState(animationState) * getFrameDuration(); // Fix: usa l'argomento corrente
            if (getStateTime() >= duration && !completeKillCalled) {
                completeKill();
            }
        }
    }

    // Single source of truth for animation durations
    public int getFramesForState(AnimationState state) {
        return switch (state) {
            case ENTRANCE -> 21;
            case IDLE     -> 4;
            case ATTACK1  -> 3;
            case ATTACK2  -> 7;
            case ATTACK3  -> 8;
            case ATTACK4  -> 5;
            case DEATH    -> 6;
        };
    }

    @Override
    public void kill() { this.kill(true); }

    @Override
    public void kill(boolean silent) {
        if (animationState == AnimationState.DEATH) return;
        setAnimationState(AnimationState.DEATH);
    }

    @Override
    public boolean shouldRemove() {
        if (animationState == AnimationState.DEATH && !completeKillCalled) return false;
        return super.shouldRemove();
    }

    public void completeKill() {
        if (completeKillCalled) return;
        completeKillCalled = true;

        // DISABILITATO: Rimosso il tracciamento persistente delle kill del boss sul DB
        /*
        try {
            var user = uni.gaben.iscat.utils.SessionManager.getInstance().getCurrentUser();
            if (user != null) {
                IscatDB.getInstance().getEnemyDAO().incrementKill(user.id(), "iscat_master", 1);
            }
        } catch (Exception e) {
            System.err.println("[ERRORE REGISTRAZIONE BOSS] Impossibile aggiornare i record di morte su DB");
            e.printStackTrace();
        }
        */

        if (waveController != null) {
            waveController.notifyBossDead();
        }

        // Flag for removal so processEntityCleanup picks it up.
        setShouldRemove(true);
    }
}