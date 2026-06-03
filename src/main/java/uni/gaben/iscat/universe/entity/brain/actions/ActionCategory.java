package uni.gaben.iscat.universe.entity.brain.actions;

public enum ActionCategory {
    MOVEMENT,   // one movement goal at a time
    ATTACK,     // one attack pattern at a time (can fire while moving)
    SPECIAL     // e.g. summon, shield, teleport – not blocking movement/attack
}