package uni.gaben.iscat.universe.entities.shooters;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.entities.EntityState;
import uni.gaben.iscat.universe.entities.hardcoded.projectiles.Shooter;
import uni.gaben.iscat.universe.spawn.UniverseSpawner;
import uni.gaben.iscat.universe.entities.hardcoded.projectiles.ProjectileModel;
import uni.gaben.iscat.universe.entities.hardcoded.projectiles.ProjectileType;
import uni.gaben.iscat.universe.entities.EntityModel;
import uni.gaben.iscat.universe.UU;

import java.util.function.Consumer;

/** Evoca n entità nel raggio indicato usando l'ID del Database */
public class SummonPattern implements Pattern {

    private final int count;
    private final String enemyId;
    private final double spawnRadiusPx;

    public SummonPattern(int count, String enemyId, double spawnRadiusPx) {
        this.count = count;
        this.enemyId = enemyId;
        this.spawnRadiusPx = spawnRadiusPx;
    }

    @Override
    public void execute(Shooter<?> shooter, ProjectileType pType, double angle, Consumer<ProjectileModel> customizer) {
        var model = shooter.getModel();

        if (model instanceof EntityModel entityModel) {
            entityModel.setEntityState(EntityState.SPAWN_ATTACK);
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