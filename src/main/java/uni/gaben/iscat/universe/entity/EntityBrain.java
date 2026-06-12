package uni.gaben.iscat.universe.entity;

import uni.gaben.iscat.universe.entity.Data.BrainData;

import uni.gaben.iscat.universe.entity.brain.*;
import uni.gaben.iscat.universe.entity.brain.abilities.Ability;


public class EntityBrain extends Brain<GameEntity> {

    public EntityBrain(GameEntity entity) {
        super(entity);

    }

    public static EntityBrain fromRecord(GameEntity entity) {
        EntityBrain brain = new EntityBrain(entity);
        EntityRecord s = entity.getRecord();
        if (s.brain() == null) return brain;

        // Steering
        brain.setSteeringGoal(SteeringGoal.createSteeringGoal(s.brain().steering()));

        // Rotation
        brain.setRotationGoal(RotationGoal.createRotationGoal(s.brain().rotation()));

        // Abilities
        for (BrainData.AbilityRecord ac : s.brain().abilities()) {
            Ability ability = Ability.createAbility(ac, entity);
            if (ability != null) brain.addAction(ability);
        }

        // Modifiers
        for (BrainData.ModifierRecord mc : s.brain().modifiers()) {
            SteeringModifier mod = SteeringModifier.createModifier(mc, entity);
            if (mod != null) brain.addModifier(mod);
        }
        return brain;
    }


}
