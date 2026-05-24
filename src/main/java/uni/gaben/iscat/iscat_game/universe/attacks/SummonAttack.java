package uni.gaben.iscat.iscat_game.universe.attacks;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.iscat_game.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.iscat_game.lib.interfaces.model.HasProjectile;
import uni.gaben.iscat.iscat_game.utils.UU;
import uni.gaben.iscat.iscat_game.universe.UniverseSpawnable;
import uni.gaben.iscat.iscat_game.universe.UniverseSpawner;
import uni.gaben.iscat.iscat_game.universe.enemies.iscat_master.IscatMasterModel;
import uni.gaben.iscat.iscat_game.universe.projectiles.Projectile;
import uni.gaben.iscat.iscat_game.universe.projectiles.Shooter;

//----------------------------------------------------------------------
//  Questo attacco evoca n nemici attorno al nemico in modo radiale.
//----------------------------------------------------------------------
public class SummonAttack<T extends AbstractEntityModel & HasProjectile<?>> implements AttackPattern<T> {

    private final int count;
    private final UniverseSpawnable type;
    private final double spawnRadiusPx;

    /**
     * @param count         numero di nemici da evocare
     * @param type          tipo di nemico (es. UniverseSpawnable.WORM)
     * @param spawnRadiusPx raggio di spawn attorno al boss in pixel
     */
    public SummonAttack(int count, UniverseSpawnable type, double spawnRadiusPx) {
        this.count          = count;
        this.type           = type;
        this.spawnRadiusPx  = spawnRadiusPx;
    }

    @Override
    public boolean updateAndExecute(T entity, Shooter<T> shooter, Projectile template,
                                    double targetAngle, double dt) {
        // Notifica il model per triggerare l'animazione ATTACK3
        if (entity instanceof IscatMasterModel m) {
            m.setAnimationState(IscatMasterModel.AnimationState.ATTACK3);
        }

        Vector2 bossPos = entity.getTransform().getTranslation();
        double spawnRadiusM = UU.pxToM(spawnRadiusPx);
        double angleStep = (2.0 * Math.PI) / count;

        for (int i = 0; i < count; i++) {
            double angle  = i * angleStep;
            double spawnX = UU.mToPx(bossPos.x + Math.cos(angle) * spawnRadiusM);
            double spawnY = UU.mToPx(bossPos.y + Math.sin(angle) * spawnRadiusM);
            UniverseSpawner.getInstance().spawn(type, spawnX, spawnY);
        }

        return true; // Istantaneo
    }

    @Override
    public void reset() {} // Stateless

    @Override
    public void onStart(AbstractEntityModel entity) {
        if (entity instanceof IscatMasterModel m) {
            m.setAnimationState(IscatMasterModel.AnimationState.ATTACK3);
        }
    }
}