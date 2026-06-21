package uni.gaben.iscat.universe.entities.shooters;

import uni.gaben.iscat.universe.entities.projectiles.ProjectileModel;
import uni.gaben.iscat.universe.entities.projectiles.ProjectileType;
import uni.gaben.iscat.universe.entities.projectiles.Shooter;
import java.util.function.Consumer;

/**
 * Spara un singolo proiettile
 */
public class SingleShotPattern implements Pattern {

    /**
     * Innesca istantaneamente lo spawn balistico di un singolo colpo nel mondo virtuale.
     * Inoltra direttamente i parametri di tipo, traiettoria e i modificatori funzionali
     * al modulo {@link Shooter} delegato, senza introdurre logiche di sfasamento intermedie.
     */
    @Override
    public void execute(Shooter<?> shooter, ProjectileType type, double angle, Consumer<ProjectileModel> customizer) {
        // Delega immediata dell'operazione di sparo balistico lineare
        shooter.shoot(type, angle, customizer);
    }
}