package uni.gaben.iscat.universe.lib.implementations.attacks;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.lib.interfaces.model.AttackPattern;
import uni.gaben.iscat.universe.UniverseSpawnable;
import uni.gaben.iscat.universe.UniverseSpawner;
import uni.gaben.iscat.universe.enemies.master.IscatMasterModel;
import uni.gaben.iscat.universe.player.PlayerModel; // Importiamo il player
import uni.gaben.iscat.universe.projectiles.Projectile;
import uni.gaben.iscat.universe.projectiles.ProjectileType;
import uni.gaben.iscat.universe.projectiles.Shooter;
import uni.gaben.iscat.universe.UU;

import java.util.function.Consumer;

/** Evoca n entità nel raggio indicato */
public class SummonAttack implements AttackPattern {

    private final int count;
    private final UniverseSpawnable type;
    private final double spawnRadiusPx;

    public SummonAttack(int count, UniverseSpawnable type, double spawnRadiusPx) {
        this.count = count;
        this.type = type;
        this.spawnRadiusPx = spawnRadiusPx;
    }

    @Override
    public void execute(Shooter<?> shooter, ProjectileType pType, double angle, Consumer<Projectile> customizer) {
        var model = shooter.getModel();

        // GESTIONE ANIMAZIONI
        if (model instanceof IscatMasterModel master) {
            master.setAnimationState(IscatMasterModel.AnimationState.ATTACK3);
        } else if (model instanceof PlayerModel player) {
            // ...
        }

        Vector2 originPos = model.getTransform().getTranslation();
        double spawnRadiusM = UU.pxToM(spawnRadiusPx);
        double angleStep = (2.0 * Math.PI) / count;

        for (int i = 0; i < count; i++) {
            // Usiamo l'angolo di puntamento (angle) come offset iniziale,
            // così i minion spawnano orientati rispetto a dove guarda il player!
            double currentAngle = angle + (i * angleStep);

            double spawnX = UU.mToPx(originPos.x + Math.cos(currentAngle) * spawnRadiusM);
            double spawnY = UU.mToPx(originPos.y + Math.sin(currentAngle) * spawnRadiusM);

            UniverseSpawner.getInstance().spawn(type, spawnX, spawnY);
        }
    }
}