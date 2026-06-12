package uni.gaben.iscat.universe.entity.modules;

import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.entity.GameEntity;
import uni.gaben.iscat.universe.entity.interfaces.HasSprite;
import uni.gaben.iscat.universe.entity.Data.SpriteData;

public class SpriteModule implements EntityModule, HasSprite {

    private GameEntity entity;
    private SpriteData data;

    @Override
    public void attach(GameEntity entity) {
        this.entity = entity;
        this.data = entity.getRecord().sprite();
    }

    @Override
    public String getSpritePath() {
        return data.spritePath();
    }

    @Override
    public int getSpriteFrameWidth() {
        return data.frameW();
    }

    @Override
    public int getSpriteFrameHeight() {
        return data.frameH();
    }

    @Override
    public double getFrameDuration() {
        return UU.UNIVERSE_TICK * 3;
    }

    @Override
    public double getFrameDuration(int state, int frame) {
        return getFrameDuration();
    }

    @Override
    public double getVisualScale() {
        return data.scale();
    }

    @Override
    public double getVisualAngularOffsetDeg() {
        // We can handle rotations explicitly or read from data.
        // Usually it's 0 for enemies, maybe 180 for player (which can be overriden or added to SpriteData later).
        // For now returning 0, and player specific logic can be added if needed.
        return 0;
    }
}
