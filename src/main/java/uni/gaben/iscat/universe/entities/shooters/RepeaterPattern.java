package uni.gaben.iscat.universe.entities.shooters;

import uni.gaben.iscat.universe.entities.projectiles.ProjectileModel;
import uni.gaben.iscat.universe.entities.projectiles.ProjectileType;
import uni.gaben.iscat.universe.entities.projectiles.Shooter;
import java.util.function.Consumer;

/**
 * Spara n volte un tipo di attacco a distanza di secondi
 */
public record RepeaterPattern(int times, double intervalSeconds, Pattern inner) implements Pattern {

    /**
     * Innesca la prima scarica dell'ondata immediatamente.
     * <p>
     * Le successive ripetizioni programmate verranno intercettate e processate dai sistemi di controllo
     * esterni leggendo i componenti esposti del record ({@link #times()} e {@link #inner()}).
     * </p>
     */
    @Override
    public void execute(Shooter<?> shooter, ProjectileType type, double angle, Consumer<ProjectileModel> customizer) {
        // Guardia di corto circuito: interrompe l'esecuzione in caso di pattern nullo o contatore azzerato
        if (inner == null || times <= 0) return;

        // Esegue istantaneamente la prima iterazione del pattern delegato
        inner.execute(shooter, type, angle, customizer);
    }
}