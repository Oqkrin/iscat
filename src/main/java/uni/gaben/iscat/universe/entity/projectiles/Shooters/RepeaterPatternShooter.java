package uni.gaben.iscat.universe.entity.projectiles.Shooters;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import uni.gaben.iscat.universe.entity.projectiles.Projectile;
import uni.gaben.iscat.universe.entity.projectiles.ProjectileType;
import uni.gaben.iscat.universe.entity.projectiles.Shooter;
import java.util.function.Consumer;

/**
 * Attacco che definisce quanti colpi di fila deve eseguire un determinato attacco,
 * distanziandoli nel tempo in modo autonomo.
 */
public class RepeaterPatternShooter implements PatternShooter {

    private final int times;
    private final PatternShooter inner;
    private final double intervalSeconds; // Intervallo tra i colpi

    public RepeaterPatternShooter(int times, double intervalSeconds, PatternShooter inner) {
        this.times = times;
        this.intervalSeconds = intervalSeconds;
        this.inner = inner;
    }

    public int getTimes() { return times; }
    public PatternShooter getInner() { return inner; }

    @Override
    public void execute(Shooter<?> shooter, ProjectileType type, double angle, Consumer<Projectile> customizer) {
        if (inner == null || times <= 0) return;

        // Creiamo una Timeline di JavaFX per gestire i colpi successivi nel tempo
        Timeline timeline = new Timeline();
        timeline.setCycleCount(times);

        KeyFrame keyFrame = new KeyFrame(Duration.seconds(intervalSeconds), event -> {
            inner.execute(shooter, type, angle, customizer);
        });

        timeline.getKeyFrames().add(keyFrame);

        // Eseguiamo il primo colpo immediatamente, gli altri seguiranno a intervalli
        inner.execute(shooter, type, angle, customizer);

        // La timeline gestirà i restanti (times - 1) colpi
        if (times > 1) {
            timeline.setCycleCount(times - 1);
            timeline.play();
        }
    }
}