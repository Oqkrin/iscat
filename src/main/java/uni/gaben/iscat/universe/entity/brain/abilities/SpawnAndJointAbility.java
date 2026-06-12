package uni.gaben.iscat.universe.entity.brain.abilities;

import org.dyn4j.dynamics.joint.RevoluteJoint;
import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entity.EntityFactory;
import uni.gaben.iscat.universe.entity.GameEntity;
import uni.gaben.iscat.universe.entity.brain.Brain;

import java.util.Collections;

public class SpawnAndJointAbility extends Ability {

    private final String summonEntityKey;
    private final double jointOffsetPx;
    private boolean hasSpawned = false;

    public SpawnAndJointAbility(String summonEntityKey, double jointOffsetPx) {
        super("SpawnAndJoint", AbilityCategory.SPECIAL, java.util.Collections.emptySet());
        this.summonEntityKey = summonEntityKey;
        this.jointOffsetPx = jointOffsetPx;
    }

    @Override
    public boolean canActivate(GameEntity self, UniverseModel world, double dt) {
        return !hasSpawned;
    }

    @Override
    public void onActivate(Brain<?> brain, UniverseModel world) {
        if (hasSpawned) return;
        hasSpawned = true;

        GameEntity parent = brain.getEntity();
        
        // Calcola la posizione dietro al parent
        Vector2 parentPos = parent.getTransform().getTranslation();
        double parentAngle = parent.getTransform().getRotationAngle();
        
        // Posizioniamo il figlio dietro (180 gradi = PI radianti rispetto alla direzione del parent)
        Vector2 offset = new Vector2(-jointOffsetPx, 0).rotate(parentAngle);
        Vector2 childPos = parentPos.copy().add(offset);

        GameEntity child = EntityFactory.spawn(summonEntityKey, childPos.x, childPos.y, world, null);
        if (child != null) {
            // Allinea la rotazione iniziale
            child.getTransform().setRotation(parentAngle);

            // Crea il giunto (RevoluteJoint permette la rotazione ma fissa la distanza)
            // L'ancora è il punto medio tra parent e child
            Vector2 anchor = parentPos.copy().add(childPos).multiply(0.5);
            RevoluteJoint joint = new RevoluteJoint(parent, child, anchor);
            
            // Aggiungiamo limiti al giunto in modo che non si pieghi troppo
            joint.setLimitsEnabled(true);
            joint.setLimits(-Math.PI / 4, Math.PI / 4); // Limite di 45 gradi
            
            world.addJoint(joint);
        }
    }

    @Override
    public boolean update(Brain<?> brain, UniverseModel world, double dt) {
        return false; // Esegue solo un frame
    }
}
