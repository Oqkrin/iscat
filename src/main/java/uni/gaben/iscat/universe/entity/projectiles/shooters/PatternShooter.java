package uni.gaben.iscat.universe.entity.projectiles.shooters;

import uni.gaben.iscat.universe.entity.EntitySettings;
import uni.gaben.iscat.universe.entity.projectiles.ProjectileProjectileModel;
import uni.gaben.iscat.universe.entity.projectiles.ProjectileType;
import uni.gaben.iscat.universe.entity.projectiles.Shooter;

import java.util.function.Consumer;

public interface PatternShooter {
    /**
     * Esegue l'attacco custom.
     */
    void execute(Shooter<?> shooter, ProjectileType type, double angle, Consumer<ProjectileProjectileModel> customizer);

    static PatternShooter createPatternShooter(EntitySettings.PatternSettings pc) {
        if (pc == null) return new SingleShotPatternShooter();
        return switch (pc.type) {
            case "singleShot" -> new SingleShotPatternShooter();
            case "spread" -> new SpreadPatternShooter(pc.count, pc.angleStepDeg);
            case "multiDirection" ->
                    new MultiDirectionPatternShooter(pc.count, Math.toRadians(pc.angleStepDeg), new SingleShotPatternShooter());
            case "ring" -> new RingPatternShooter(pc.count);
            case "repeater" ->
                    new RepeaterPatternShooter(pc.repeats, pc.intervalSec, createPatternShooter(pc)); // careful: recursion needs base pattern
            case "parallelLine" ->
                    new ParallelLinePatternShooter(pc.count, pc.angleStepDeg); // angleStepDeg used as spacing?
            case "summon" -> new SummonPatternShooter(pc.count, pc.summonedEntityKey, pc.summonRadiusPx);
            case "figure" -> new FigurePatternShooter(pc.count, FigurePatternShooter.FigureType.valueOf(pc.figureType));
            default -> new SingleShotPatternShooter();
        };
    }
}