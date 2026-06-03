package uni.gaben.iscat.universe.interfaces;

public interface HasSprite {
    String getSpritePath();
    int getSpriteFrameWidth();
    double getFrameDuration();
    double getFrameDuration(int state, int frame);
    int getSpriteFrameHeight();
    double getVisualScale();
    double getVisualAngularOffsetDeg();
    default boolean canRotate() { return true; }
}
