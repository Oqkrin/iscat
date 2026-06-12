package uni.gaben.iscat.universe.entity.Data;

import java.util.List;

public record PlayerData(
        double dashImpulse,
        double dashDurationSec,
        double dashCooldownSec,
        double stunDurationSec,
        double baseCooldownSec,
        List<LevelAbility> levelAbilities
) {
    public record LevelAbility(
            int minLevel,
            BrainData.PatternRecord pattern,
            double cooldownSec
    ) {}
}
