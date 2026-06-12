package uni.gaben.iscat.universe.entity;

import uni.gaben.iscat.universe.entity.Data.*;

/**
 * Immutable definition of an entity type.
 * Composed of modular Data records.
 * If a Data record is present, the entity supports that behavior.
 */
public record EntityRecord(
        IdentityData identity,
        SpriteData sprite,
        PhysicsData physics,
        DynamicsData dynamics,
        EnduranceData endurance,
        StateData state,
        XpData xp,
        AudioData audio,
        BrainData brain,
        PlayerData player
) {}
