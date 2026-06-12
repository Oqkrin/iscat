package uni.gaben.iscat.universe.entity.modules;

import uni.gaben.iscat.universe.entity.GameEntity;
import uni.gaben.iscat.universe.entity.brain.RotationGoal;
import uni.gaben.iscat.universe.entity.brain.SteeringGoal;
import uni.gaben.iscat.universe.entity.brain.SteeringModifier;
import uni.gaben.iscat.universe.entity.brain.abilities.Ability;
import uni.gaben.iscat.universe.entity.Data.BrainData;
import uni.gaben.iscat.universe.entity.EntityBrain;

public class BrainModule implements EntityModule {

    private GameEntity entity;
    private BrainData data;
    private EntityBrain brain;

    @Override
    public void attach(GameEntity entity) {
        this.entity = entity;
        this.data = entity.getRecord().brain();
        
        this.brain = new EntityBrain(entity);

        if (data != null) {
            if (data.steering() != null) {
                brain.setSteeringGoal(SteeringGoal.createSteeringGoal(data.steering()));
            }
            if (data.rotation() != null) {
                brain.setRotationGoal(RotationGoal.createRotationGoal(data.rotation()));
            }
            if (data.abilities() != null) {
                for (BrainData.AbilityRecord ac : data.abilities()) {
                    Ability ability = Ability.createAbility(ac, entity);
                    if (ability != null) brain.addAction(ability);
                }
            }
            if (data.modifiers() != null) {
                for (BrainData.ModifierRecord mc : data.modifiers()) {
                    SteeringModifier mod = SteeringModifier.createModifier(mc, entity);
                    if (mod != null) brain.addModifier(mod);
                }
            }
        }
    }

    public EntityBrain getBrain() {
        return brain;
    }

    @Override
    public void update(double dt) {
        // Brain update is usually called from UniverseController where UniverseModel is available.
        // We might need to pass UniverseModel to this update, or keep updating it from the controller.
        // For now, Brain's update requires UniverseModel, so we'll leave it to GameController or UniverseController 
        // to iterate over brains and update them, or we can stash the universe in GameEntity.
        // Let's assume GameController updates EntityBrains explicitly as it does now.
    }
}
