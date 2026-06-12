package uni.gaben.iscat.universe.entity.projectiles.shooters;

import uni.gaben.iscat.universe.entity.Data.BrainData;
import uni.gaben.iscat.universe.entity.GameEntity;


import java.util.function.Consumer;

public interface PatternShooter {
    /**
     * Esegue l'attacco custom.
     */
    void execute(Shooter<?> shooter, String type, double angle, Consumer<GameEntity> customizer);

    static PatternShooter createPatternShooter(BrainData.PatternRecord pc) {
        if (pc == null) return new SingleShotPatternShooter();
        return switch (pc.type()) {
            case "singleShot" -> new SingleShotPatternShooter();
            case "spread" -> new SpreadPatternShooter(pc.count(), pc.angleStepDeg());
            case "multiDirection" ->
                    new MultiDirectionPatternShooter(pc.count(), Math.toRadians(pc.angleStepDeg()), createPatternShooter(pc)); //#TODO fix recursion definition
            case "ring" -> new RingPatternShooter(pc.count());
            case "repeater" ->
                    new RepeaterPatternShooter(pc.repeats(), pc.intervalSec(), createPatternShooter(pc)); //#TODO fix recursion definition
            case "parallelLine" ->
                    new ParallelLinePatternShooter(pc.count(), pc.angleStepDeg()); // angleStepDeg used as spacing?
            case "summon" -> new SummonPatternShooter(pc.count(), pc.summonedEntityKey(), pc.summonRadiusPx());
            case "figure" -> new FigurePatternShooter(pc.count(), FigurePatternShooter.FigureType.valueOf(pc.figureType()));
            default -> throw new IllegalStateException("Unexpected value: " + pc.type());
        };
    }
}
