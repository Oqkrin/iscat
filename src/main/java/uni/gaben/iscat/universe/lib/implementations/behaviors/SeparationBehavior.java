package uni.gaben.iscat.universe.lib.implementations.behaviors;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.lib.interfaces.controller.AiBehavior;
import uni.gaben.iscat.universe.UniverseModel;

import java.util.List;

public class SeparationBehavior implements AiBehavior {

    private final double radius;
    private final double maxForce;

    public SeparationBehavior(double radius, double maxForce) {
        this.radius = radius;
        this.maxForce = maxForce;
    }

    @Override
    public double getPriority(AbstractEntityModel npc, UniverseModel universe) {
        // Ritorna -1.0 per indicare che è un comportamento parallelo additivo eseguito ad ogni frame
        return -1.0;
    }

    @Override
    public void execute(AbstractEntityModel npc, UniverseModel universe, double dt) {
        List<? extends AbstractEntityModel> sameTypeEntities = universe.getEntitiesOfType(npc.getClass());
        if (sameTypeEntities.size() <= 1) return;

        Vector2 npcPos = npc.getTransform().getTranslation();
        Vector2 steering = new Vector2();
        int count = 0;

        for (AbstractEntityModel other : sameTypeEntities) {
            if (other == npc) continue;

            Vector2 otherPos = other.getTransform().getTranslation();
            double dist = npcPos.distance(otherPos);

            // Se l'altra entità dello stesso tipo è troppo vicina, calcola la forza repulsiva
            if (dist > 0 && dist < radius) {
                Vector2 diff = npcPos.copy().subtract(otherPos);
                diff.normalize();
                diff.divide(dist); // Più vicino significa spinta repulsiva maggiore
                steering.add(diff);
                count++;
            }
        }

        if (count > 0) {
            steering.divide(count);
            if (steering.getMagnitudeSquared() > 0) {
                steering.normalize();
                steering.multiply(maxForce);
                
                // Applica la forza di separazione per evitare clumping
                npc.applyForce(steering);
            }
        }
    }
}