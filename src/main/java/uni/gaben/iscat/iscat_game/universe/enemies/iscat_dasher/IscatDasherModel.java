package uni.gaben.iscat.iscat_game.universe.enemies.iscat_dasher;

import uni.gaben.iscat.iscat_game.lib.implementations.LivingEntityModel;
import uni.gaben.iscat.iscat_game.universe.enemies.fake_iscat.FakeIscatSettings;
public class IscatDasherModel extends LivingEntityModel {
    public IscatDasherModel(double x, double y) {
        this(x, y, FakeIscatSettings.HP_INIZIALI, FakeIscatSettings.HP_INIZIALI);
    }
    public IscatDasherModel(double x, double y, double life, double maxLife) {
        super(x, y, life, maxLife);
    }
}
