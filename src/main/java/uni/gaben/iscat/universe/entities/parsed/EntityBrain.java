package uni.gaben.iscat.universe.entities.parsed;

import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entities.EntityState;
import uni.gaben.iscat.universe.entities.brain.*;
import uni.gaben.iscat.universe.entities.brain.abilities.Ability;
import uni.gaben.iscat.universe.entities.brain.steering.SteeringModifier;

/**
 * Gestisce l'intelligenza artificiale (IA) e il comportamento delle entità nemiche.
 * Controlla l'attivazione di abilità, modificatori di movimento e obiettivi di rotazione.
 */
public class EntityBrain extends Brain<EntityModel> {

    public EntityBrain(EntityModel entity) {
        super(entity);
    }

    /**
     * Aggiorna la logica del cervello.
     * Blocca le decisioni dell'IA se l'entità è in stato di ingresso, evocazione o morte.
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

    /**
     * Genera e configura un nuovo cervello partendo dai dati statici salvati nel relativo EntityRecord.
     */
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