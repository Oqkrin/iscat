package uni.gaben.iscat.iscat_game.universe.enemies.iscat_worm;

import uni.gaben.iscat.iscat_game.utils.UU;
import java.util.ArrayList;
import java.util.List;

public class IscatWormModel {  // NON estende LivingEntityModel

    private final List<IscatWormSegment> segments = new ArrayList<>();

    public IscatWormModel(double x, double y) {
        createFullWorm(x, y);
    }

    private void createFullWorm(double x, double y) {
        double spacing = UU.pxToM(IscatWormSettings.SEGMENT_SPACING_PX);

        IscatWormSegment head = new IscatWormSegment(IscatWormSegment.Type.HEAD, x, y);
        segments.add(head);
        IscatWormSegment previous = head;

        for (int i = 0; i < IscatWormSettings.INITIAL_SEGMENTS - 2; i++) {
            IscatWormSegment body = new IscatWormSegment(
                    IscatWormSegment.Type.BODY, x - (i + 1) * spacing, y);
            body.setPreviousSegment(previous); // ← era mancante
            segments.add(body);
            previous = body;
        }

        IscatWormSegment tail = new IscatWormSegment(
                IscatWormSegment.Type.TAIL, x - (IscatWormSettings.INITIAL_SEGMENTS - 1) * spacing, y);
        tail.setPreviousSegment(previous); // ← era mancante
        segments.add(tail);
    }

    public List<IscatWormSegment> getSegments() { return segments; }

    public IscatWormSegment getHead() {
        return segments.stream().filter(s -> !s.isConsumed()).findFirst().orElse(null);
    }
}