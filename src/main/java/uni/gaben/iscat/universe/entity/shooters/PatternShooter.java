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
            case SINGLE_SHOT -> new SingleShotPatternShooter();
            case SPREAD -> new SpreadPatternShooter(pc.count(), pc.angleStepDeg());
            case MULTI_DIRECTION ->
                    new MultiDirectionPatternShooter(pc.count(), Math.toRadians(pc.angleStepDeg()), createPatternShooter(pc.innerPattern()));
            case RING -> new RingPatternShooter(pc.count());
            case REPEATER ->
                    new RepeaterPatternShooter(pc.repeats(), pc.intervalSec(), createPatternShooter(pc.innerPattern()));
            case PARALLEL_LINE ->
                    new ParallelLinePatternShooter(pc.count(), pc.angleStepDeg()); // angleStepDeg used as spacing
            case SUMMON -> new SummonPatternShooter(pc.count(), pc.summonedEntityKey(), pc.summonRadiusPx());
            case FIGURE -> new FigurePatternShooter(pc.count(), FigurePatternShooter.FigureType.valueOf(pc.figureType()));
        };
    }
}