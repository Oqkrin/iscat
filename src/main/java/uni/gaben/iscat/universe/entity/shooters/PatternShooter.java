package uni.gaben.iscat.universe.entity.shooters;

import uni.gaben.iscat.universe.entity.EntityRecord;
import uni.gaben.iscat.universe.entity.hardcoded.projectiles.ProjectileModel;
import uni.gaben.iscat.universe.entity.hardcoded.projectiles.ProjectileType;

import java.util.function.Consumer;

public interface PatternShooter {
    void execute(Shooter<?> shooter, ProjectileType type, double angle, Consumer<ProjectileModel> customizer);
    static PatternShooter createPatternShooter(EntityRecord.PatternRecord pc) {
        if (pc == null) return new SingleShotPatternShooter();
        return switch (pc.type()) {
            case "singleShot" -> new SingleShotPatternShooter();
            case "spread" -> new SpreadPatternShooter(pc.count(), pc.angleStepDeg());
            case "multiDirection" ->
                    new MultiDirectionPatternShooter(pc.count(), Math.toRadians(pc.angleStepDeg()), createPatternShooter(pc.innerPattern()));
            case "ring" -> new RingPatternShooter(pc.count());
            case "repeater" ->
                    new RepeaterPatternShooter(pc.repeats(), pc.intervalSec(), createPatternShooter(pc.innerPattern()));
            case "parallelLine" ->
                    new ParallelLinePatternShooter(pc.count(), pc.angleStepDeg()); // angleStepDeg used as spacing?
            case "summon" -> new SummonPatternShooter(pc.count(), pc.summonedEntityKey(), pc.summonRadiusPx());
            case "figure" -> new FigurePatternShooter(pc.count(), FigurePatternShooter.FigureType.valueOf(pc.figureType()));
            default -> throw new IllegalStateException("Unexpected value: " + pc.type());
        };
    }
}