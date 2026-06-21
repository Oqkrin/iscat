package uni.gaben.iscat.universe.entities.parsed;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Polygon;
import uni.gaben.iscat.universe.effects.Shockwave;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseCollisionLayers;
import uni.gaben.iscat.universe.entities.AbstractLivingEntityModel;
import uni.gaben.iscat.universe.entities.EntityState;
import uni.gaben.iscat.universe.spawn.waves.UniverseWaveController;
import uni.gaben.iscat.universe.entities.asteroids.AsteroidShapeFactory;
import uni.gaben.iscat.universe.entities.interfaces.HasShockwave;
import uni.gaben.iscat.universe.entities.interfaces.HasSprite;
import uni.gaben.iscat.utils.EntityAudioManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Rappresenta un'istanza concreta di un'entità di gioco (nemico o boss) nel mondo fisico.
 * Gestisce la sincronizzazione tra gli stati logici, le collisioni dyn4j e i set di animazioni del foglio sprite.
 */
public class EntityModel extends AbstractLivingEntityModel implements HasSprite, HasShockwave {
    private final Shockwave shockwave = new Shockwave();
    private static final Random RNG = new Random();

    private EntityState currentEntityState = EntityState.IDLE;
    private boolean completeKillCalled = false;
    private UniverseWaveController waveController;
    private double idleAudioTimer = 5.0 + Math.random() * 10.0;
    private int currentAnimationRow;

    /**
     * Costruisce il modello dell'entità impostando la geometria di collisione, i filtri di livello,
     * la tipologia di massa e l'eventuale stato di animazione d'ingresso.
     */
    public EntityModel(double x, double y, EntityRecord entity) {
        super(x, y, entity);
        setXpReward(entity.xpReward());

        if (entity.hasEntranceAnimation()) {
            this.currentEntityState = EntityState.ENTRANCE;
            this.currentAnimationRow = findRowByType("ENTRANCE", EntityState.ENTRANCE.ordinal());
            setEnabled(false);
        } else {
            this.currentAnimationRow = findRowByType("IDLE", EntityState.IDLE.ordinal());
            setState(this.currentAnimationRow);
        }

        double collisionSize = UU.pxToM(entity.frameW() * entity.scale() * 0.9);
        BodyFixture fixture = switch (entity.shapeType()) {
            case CIRCLE -> addFixture(Geometry.createCircle(collisionSize / 2.0));
            case SQUARE -> addFixture(Geometry.createSquare(collisionSize));
            case POLYGON -> addFixture(new Polygon(AsteroidShapeFactory.getScaledShape(collisionSize)));
        };

        if (entity.isBoss() || "iscat-master".equals(entity.entityKey())) {
            fixture.setFilter(UniverseCollisionLayers.MASTER_FILTER);
        } else {
            fixture.setFilter(UniverseCollisionLayers.ENEMY_FILTER);
        }
        setMass(entity.maxAngularVelocity() > 0 ? MassType.NORMAL : MassType.FIXED_LINEAR_VELOCITY);
        setLinearDamping(entity.linearDamping());

        if (!entity.hasEntranceAnimation()) {
            EntityAudioManager.playEventAudio(this, "spawn");
        }
    }

    public void setWaveController(UniverseWaveController waveController) {
        this.waveController = waveController;
    }

    @Override
    public int getState() {
        return currentAnimationRow;
    }

    /**
     * Modifica lo stato logico dell'entità calcolando automaticamente la riga di animazione corretta.
     */
    public void setEntityState(EntityState state) {
        setEntityState(state, findRowByType(state.name(), state.ordinal()));
    }

    /**
     * Aggiorna i filtri di categoria su tutte le fixture fisiche collegate all'entità.
     */
    public void setCollisionFilter(org.dyn4j.collision.CategoryFilter filter) {
        for (int i = 0; i < getFixtureCount(); i++) {
            getFixture(i).setFilter(filter);
        }
    }

    /**
     * Forza lo stato logico dell'entità e imposta una riga di animazione esplicita resettandone il timer.
     */
    public void setEntityState(EntityState state, int animationRow) {
        if (this.currentEntityState != state || this.currentAnimationRow != animationRow) {
            this.currentEntityState = state;
            this.currentAnimationRow = animationRow;
            setState(animationRow);
            setStateTime(0);
        }
    }

    /**
     * Analizza la configurazione per estrarre i dati di riproduzione della riga d'animazione corrente.
     */
    private EntityRecord.AnimationRecord getCurrentAnimationRecord() {
        if (entity.animations() == null) return null;
        for (EntityRecord.AnimationRecord anim : entity.animations()) {
            if (anim.row() == currentAnimationRow) {
                return anim;
            }
        }
        return null;
    }

    /**
     * Cerca le righe d'animazione corrispondenti a una categoria. Se multipli, estrae un indice casuale.
     */
    private int findRowByType(String typeName, int fallbackRow) {
        if (entity.animations() == null || entity.animations().isEmpty()) {
            return typeName.equalsIgnoreCase("IDLE") ? 0 : fallbackRow;
        }

        List<Integer> candidates = new ArrayList<>();
        for (EntityRecord.AnimationRecord anim : entity.animations()) {
            if (anim.type().equalsIgnoreCase(typeName)) {
                candidates.add(anim.row());
            }
        }

        if (candidates.isEmpty()) {
            return typeName.equalsIgnoreCase("IDLE") ? 0 : fallbackRow;
        }
        return candidates.get(RNG.nextInt(candidates.size()));
    }

    /**
     * Ritorna il numero totale di frame presenti nella riga specificata dell'animazione.
     */
    public int getFramesForState(int rowIndex) {
        if (entity.animations() == null || entity.animations().isEmpty()) {
            return 1;
        }
        for (EntityRecord.AnimationRecord anim : entity.animations()) {
            if (anim.row() == rowIndex) return anim.frames();
        }
        return 1;
    }

    /**
     * Aggiorna lo stato temporale, gli effetti d'onda d'urto, gli audio di idle e la transizione tra gli stati.
     */
    @Override
    public void update(double dt) {
        updateStateTime(dt);
        shockwave.update(dt);

        if (currentEntityState == EntityState.IDLE) {
            idleAudioTimer -= dt;
            if (idleAudioTimer <= 0) {
                EntityAudioManager.playEventAudio(this, "idle");
                idleAudioTimer = 8.0 + Math.random() * 14.0;
            }
        }

        double duration = 0;
        EntityRecord.AnimationRecord anim = getCurrentAnimationRecord();
        if (anim != null) {
            duration = anim.durationSec() > 0 ? anim.durationSec() : anim.frames() * getFrameDuration();
        }

        if (currentEntityState == EntityState.ENTRANCE) {
            if (getFramesForState(currentAnimationRow) <= 0 || getStateTime() >= duration) {
                setEnabled(true);
                setEntityState(EntityState.IDLE);
                if (entity.isBoss()) {
                    shockwave.trigger(2.0, 1500, 15);
                }
                EntityAudioManager.playEventAudio(this, "spawn");
            }
        }
        else if (currentEntityState != EntityState.IDLE && currentEntityState != EntityState.DEATH) {
            if (getStateTime() >= duration) {
                setEntityState(EntityState.IDLE);
            }
        }
        else if (currentEntityState == EntityState.DEATH) {
            if (getStateTime() >= duration && !completeKillCalled) {
                completeKill();
            }
        }
    }

    /**
     * Avvia la sequenza di rimozione. Se presente un'animazione di morte la riproduce prima di eliminare il corpo.
     */
    @Override
    public void extinguish(boolean silent) {
        boolean hasDeathAnimation = false;
        if (entity.animations() != null) {
            for (EntityRecord.AnimationRecord anim : entity.animations()) {
                if (anim.type().equalsIgnoreCase("DEATH")) {
                    hasDeathAnimation = true;
                    break;
                }
            }
        }

        if (hasDeathAnimation) {
            if (currentEntityState == EntityState.DEATH) return;
            setEntityState(EntityState.DEATH, findRowByType("DEATH", EntityState.DEATH.ordinal()));
        } else {
            super.extinguish(silent);
            completeKill();
        }
    }

    @Override
    public boolean shouldRemove() {
        if (currentEntityState == EntityState.DEATH && !completeKillCalled) return false;
        return super.shouldRemove();
    }

    /**
     * Esegue la rimozione definitiva dell'entità dal loop logico e notifica l'eventuale morte del boss al wave controller.
     */
    public void completeKill() {
        if (completeKillCalled) return;
        completeKillCalled = true;

        if (entity.isBoss() && waveController != null) {
            waveController.notifyBossDead();
        }

        setShouldRemove(true);
    }

    @Override public Shockwave shockwave() { return shockwave; }
    @Override public String getSpritePath() { return entity.spritePath(); }
    @Override public int getSpriteFrameWidth() { return entity.frameW(); }
    @Override public int getSpriteFrameHeight() { return entity.frameH(); }
    @Override public double getVisualAngularOffsetDeg() { return entity.angularOffsetDeg(); }

    /**
     * Restituisce la durata temporale di un singolo fotogramma dell'animazione corrente.
     */
    @Override
    public double getFrameDuration() {
        EntityRecord.AnimationRecord anim = getCurrentAnimationRecord();
        if (anim != null && anim.durationSec() > 0) {
            return anim.durationSec() / (double) anim.frames();
        }
        return UU.UNIVERSE_TICK * 6;
    }

    @Override
    public boolean isInalterable() {
        return false;
    }

    public EntityState getCurrentEntityState() {
        return currentEntityState;
    }
}