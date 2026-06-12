package uni.gaben.iscat.universe.entity.brain.abilities;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.UniverseSpawner;
import uni.gaben.iscat.universe.entity.EntityFactory;
import uni.gaben.iscat.universe.entity.GameEntity;
import uni.gaben.iscat.universe.entity.brain.Brain;

public class BreakupAbility extends Ability {

    private final String splitEntityKey;
    private final int numPieces;
    private final double splitVelocityPush;

    public BreakupAbility(String splitEntityKey, int numPieces, double splitVelocityPush) {
        super(AbilityCategory.DEFENSE, 0); // Triggered on death, not cooldown based
        this.splitEntityKey = splitEntityKey;
        this.numPieces = numPieces;
        this.splitVelocityPush = splitVelocityPush;
    }

    @Override
    public boolean canActivate(GameEntity self, UniverseModel world, double dt) {
        return self.shouldRemove(); // Only trigger when dying
    }

    @Override
    public void onActivate(Brain<?> brain, UniverseModel world) {
        GameEntity self = brain.getEntity();
        
        double xMeters = self.getTransform().getTranslationX();
        double yMeters = self.getTransform().getTranslationY();
        Vector2 currentVel = self.getLinearVelocity();
        
        // We calculate offsets based on collision size.
        // For simplicity we just spawn them slightly offset.
        double offsetMeters = 1.0; 

        double angleStep = (Math.PI * 2) / numPieces;
        for (int i = 0; i < numPieces; i++) {
            double angle = i * angleStep;
            double spawnX = UU.mToPx(xMeters + Math.cos(angle) * offsetMeters);
            double spawnY = UU.mToPx(yMeters + Math.sin(angle) * offsetMeters);
            
            GameEntity piece = EntityFactory.spawn(splitEntityKey, spawnX, spawnY, world, world.getController());
            if (piece != null) {
                Vector2 newVel = currentVel.copy().add(Math.cos(angle) * splitVelocityPush, Math.sin(angle) * splitVelocityPush);
                piece.setLinearVelocity(newVel);
            }
        }
    }

    @Override
    public boolean update(Brain<?> brain, UniverseModel world, double dt) {
        return false; // single shot action
    }
}
