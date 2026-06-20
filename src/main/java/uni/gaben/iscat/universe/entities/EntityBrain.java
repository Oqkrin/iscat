package uni.gaben.iscat.universe.entities;

import javafx.beans.property.SimpleDoubleProperty;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entities.brain.*;
import uni.gaben.iscat.universe.entities.brain.abilities.Ability;
import uni.gaben.iscat.universe.entities.brain.steering.SteeringModifier;

public class EntityBrain extends Brain<EntityModel> {

    public EntityBrain(EntityModel entity) {
        super(entity);
    }

    /**
     * Override del ciclo di aggiornamento del cervello.
     * Controlla se l'entità sta eseguendo l'animazione d'ingresso prima di procedere.
     */
    @Override
    public void update(UniverseModel universe, double dt) {
        if (entity == null) return;

        EntityState state = entity.getCurrentEntityState();
        if (state == EntityState.ENTRANCE
                || state == EntityState.SPAWN_ATTACK
                || state == EntityState.DEATH) {
            return;
        }

        super.update(universe, dt);
    }

    public static EntityBrain fromRecord(EntityModel entity) {
        EntityBrain brain = new EntityBrain(entity);
        EntityRecord entityRecord = entity.getEntityRecord();
        if (entityRecord.brain() == null) return brain;

        // Steering
        brain.setSteeringGoal(EntityRecordParser.createSteeringGoal(entityRecord.brain().steering()));

        // Rotation
        brain.setRotationGoal(EntityRecordParser.createRotationGoal(entityRecord.brain().rotation()));

        // Abilities
        for (EntityRecord.AbilityRecord ac : entityRecord.brain().abilities()) {
            Ability ability = EntityRecordParser.createAbility(ac, entity);
            if (ability != null) brain.addAction(ability);
        }

        // Modifiers
        for (EntityRecord.ModifierRecord mc : entityRecord.brain().modifiers()) {
            SteeringModifier mod = EntityRecordParser.createModifier(mc, entity);
            if (mod != null) brain.addModifier(mod);
        }



        return brain;
    }
}