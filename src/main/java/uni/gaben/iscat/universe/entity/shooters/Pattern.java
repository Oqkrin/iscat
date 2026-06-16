package uni.gaben.iscat.universe.entity.shooters;

import uni.gaben.iscat.universe.entity.EntityRecord;
import uni.gaben.iscat.universe.entity.hardcoded.projectiles.ProjectileModel;
import uni.gaben.iscat.universe.entity.hardcoded.projectiles.ProjectileType;

import java.util.function.Consumer;

public interface Pattern {
    void execute(Shooter<?> shooter, ProjectileType type, double angle, Consumer<ProjectileModel> customizer);
    static Pattern createPattern(EntityRecord.PatternRecord pc) {
        if (pc == null) return new SingleShotPattern();
        return switch (pc.type()) {
            case SINGLE_SHOT -> new SingleShotPattern();
            case SPREAD -> new SpreadPattern(pc.count(), pc.angleStepDeg());
            case MULTI_DIRECTION ->
                    new MultiDirectionPattern(pc.count(), Math.toRadians(pc.angleStepDeg()), createPattern(pc.innerPattern()));
            case RING -> new RingPattern(pc.count());
            case REPEATER ->
                    new RepeaterPattern(pc.repeats(), pc.intervalSec(), createPattern(pc.innerPattern()));
            case PARALLEL_LINE ->
                    new ParallelLinePattern(pc.count(), pc.angleStepDeg()); // angleStepDeg used as spacing
            case SUMMON -> new SummonPattern(pc.count(), pc.summonedEntityKey(), pc.summonRadiusPx());
            case FIGURE -> new FigurePattern(pc.count(), FigurePattern.FigureType.valueOf(pc.figureType()));
        };
    }
}
