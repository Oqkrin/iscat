package uni.gaben.iscat.universe.entity.brain.abilities;

public enum AbilityCategory {
    MOVEMENT,   // one dynamics goal at a time
    ATTACK,     // one attack pattern at a time (can fire while moving)
    SPECIAL     // e.g. summon, shield, teleport – not blocking dynamics/attack
}
