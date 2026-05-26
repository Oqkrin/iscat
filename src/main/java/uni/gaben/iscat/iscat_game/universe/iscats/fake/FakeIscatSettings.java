package uni.gaben.iscat.iscat_game.universe.iscats.fake;

import uni.gaben.iscat.iscat_game.lib.abstracts.BaseEntitySettings;
import uni.gaben.iscat.iscat_game.universe.VelocitySettings;

public class FakeIscatSettings {
    public static final BaseEntitySettings FAKEISCAT = new BaseEntitySettings();
    
    static {
        FAKEISCAT.initLife = 30.0;
        FAKEISCAT.dimSprite = 32.0;
        FAKEISCAT.scale = 2.0;
        FAKEISCAT.dampingLineare = 3.0;
        FAKEISCAT.maxVelocity = VelocitySettings.FAKE_ISCAT_MAX_VELOCITY;
        FAKEISCAT.force = 15.0;
        FAKEISCAT.rotationSpeed = 0.0;
        FAKEISCAT.xpReward = 50;
        
        FAKEISCAT.detectionRange = 15.0;
        FAKEISCAT.combatRange = 10.0;
        FAKEISCAT.preferredRange = 7.0;
        FAKEISCAT.fireCooldownS = 3.5;
    }
}
