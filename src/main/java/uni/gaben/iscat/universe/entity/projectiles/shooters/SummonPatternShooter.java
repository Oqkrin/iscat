package uni.gaben.iscat.universe.entity.projectiles.shooters;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UniverseSpawner;
import uni.gaben.iscat.universe.entity.GameEntity;

import uni.gaben.iscat.universe.entity.GameEntity;
import uni.gaben.iscat.universe.UU;

import java.util.function.Consumer;

/** Evoca n entità nel raggio indicato usando l'ID del Database */
public class SummonPatternShooter implements PatternShooter {

    private final int count;
    private final String enemyId;
    private final double spawnRadiusPx;
    private final int attackStateIndex;

    public SummonPatternShooter(int count, String enemyId, double spawnRadiusPx, int attackStateIndex) {
        this.count = count;
        this.enemyId = enemyId;
        this.spawnRadiusPx = spawnRadiusPx;
        this.attackStateIndex = attackStateIndex;
    }

    public SummonPatternShooter(int count, String enemyId, double spawnRadiusPx) {
        this(count, enemyId, spawnRadiusPx, 4); // 4 = ATTACK3
    }

    @Override
    public void execute(Shooter<?> shooter, String pType, double angle, Consumer<GameEntity> customizer) {
        var model = shooter.getModel();

        if (model instanceof GameEntity genericEntity) {
            if (genericEntity.getStateModule() != null) {
                // genericEntity.getStateModule().setCurrentState(attackStateIndex);
            }
        }

        Vector2 originPos = model.getTransform().getTranslation();
        double spawnRadiusM = UU.pxToM(spawnRadiusPx);
        double angleStep = (2.0 * Math.PI) / count;

        for (int i = 0; i < count; i++) {
            double currentAngle = angle + (i * angleStep);

            double spawnX = UU.mToPx(originPos.x + Math.cos(currentAngle) * spawnRadiusM);
            double spawnY = UU.mToPx(originPos.y + Math.sin(currentAngle) * spawnRadiusM);

            UniverseSpawner.getInstance().spawn(enemyId, spawnX, spawnY);
        }
    }
}
