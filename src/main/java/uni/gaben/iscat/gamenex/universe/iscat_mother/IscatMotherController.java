package uni.gaben.iscat.gamenex.universe.iscat_mother;

import uni.gaben.iscat.game.utils.physics.Vec2;
import uni.gaben.iscat.gamenex.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.gamenex.lib.implementations.AiBehaviours;
import uni.gaben.iscat.gamenex.lib.interfaces.controller.AiBehavior;
import uni.gaben.iscat.gamenex.universe.UniverseModel;
/*
public class IscatMotherController extends AiBehaviours<IscatMotherModel> implements AiBehavior {


    /**
     * Crea un controller per un'entità specifica.
     *
     * @param AiEntity Il modello fisico dell'entità.
     *
    public IscatMotherController(IscatMotherModel AiEntity) {
        super(AiEntity);
    }

    @Override
    public void execute(AbstractEntityModel npc, UniverseModel universe, double dt) {
        this.aiEntity.checkMinionSpawn(universe);

        if (isDead()) return;

        Vec2 playerPos = world.getPlayer().getColliderCenter();
        Vec2 myPos = this.getColliderCenter();
        double dist = Math.hypot(playerPos.x - myPos.x, playerPos.y - myPos.y);

        maintainDistance(playerPos, myPos, dist);

        if (dist >= 90 && dist <= 420 && cooldownFuoco.isReady()) {
            shoot(world, myPos);
        }
    }
}
*/