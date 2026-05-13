package uni.gaben.iscat.gamenex.universe.iscat_worm;

import org.dyn4j.geometry.Vector2;

/**
 * Interfaccia comune per tutti i segmenti del verme (Head, BodyPart, Tail)
 */
public interface IscatWormSegment {

    Vector2 getPosition();           // posizione attuale
    void setRotation(double angle);
    double getRotation();
}