package uni.gaben.iscat.universe.brain;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class Target {
    // ── Internal storage ────────────────────────────────────────────────────
    private final AbstractEntityModel single;
    private final Vector2 point;
    private final Function<UniverseModel, Vector2> dynamic;
    private final List<AbstractEntityModel> multiple;

    // Private – use factories
    private Target(AbstractEntityModel single) {
        this.single = single;
        this.point = null;
        this.dynamic = null;
        this.multiple = null;
    }

    private Target(Vector2 point) {
        this.single = null;
        this.point = point;
        this.dynamic = null;
        this.multiple = null;
    }

    private Target(Function<UniverseModel, Vector2> supplier) {
        this.single = null;
        this.point = null;
        this.dynamic = supplier;
        this.multiple = null;
    }

    private Target(List<AbstractEntityModel> members) {
        this.single = null;
        this.point = null;
        this.dynamic = null;
        this.multiple = members;
    }

    // ── Factories ────────────────────────────────────────────────────────

    public static Target ofEntity(AbstractEntityModel entity) {
        return new Target(entity);
    }

    public static Target ofPoint(Vector2 point) {
        return new Target(point);
    }

    public static Target ofDynamic(Function<UniverseModel, Vector2> supplier) {
        return new Target(supplier);
    }

    public static Target ofGroup(List<AbstractEntityModel> members) {
        return new Target(members);
    }

    // ── Type checks (clean names) ────────────────────────────────────────

    public boolean isEntity()  { return single != null; }
    public boolean isPoint()   { return point != null; }
    public boolean isDynamic() { return dynamic != null; }
    public boolean isGroup()   { return multiple != null; }

    // ── Single‑position access (backward compatible) ─────────────────────

    /**
     * Returns a representative position:
     * <ul>
     *   <li>For an entity, its translation (or null if removed).</li>
     *   <li>For a fixed point, a copy of that point.</li>
     *   <li>For a dynamic target, the computed position.</li>
     *   <li>For a group, the center (average of all members).</li>
     * </ul>
     */
    public Vector2 getPosition(UniverseModel world) {
        if (single != null) {
            if (single.shouldRemove()) return null;
            return single.getTransform().getTranslation();
        }
        if (point != null) {
            return point.copy();
        }
        if (dynamic != null) {
            return dynamic.apply(world);
        }
        if (multiple != null && !multiple.isEmpty()) {
            Vector2 center = new Vector2();
            for (AbstractEntityModel m : multiple) center.add(m.getTransform().getTranslation());
            return center.multiply(1.0 / multiple.size());
        }
        return null;
    }

    // ── Collection‑based access (new) ────────────────────────────────────

    /**
     * Returns all entities associated with this target.
     * <ul>
     *   <li>For a single entity, a list containing that entity.</li>
     *   <li>For a group, the group list.</li>
     *   <li>For point/dynamic, an empty list.</li>
     * </ul>
     */
    public List<AbstractEntityModel> getEntities() {
        if (single != null) return Collections.singletonList(single);
        if (multiple != null) return multiple;
        return Collections.emptyList();
    }

    /**
     * Returns the positions of all relevant entities.
     * <ul>
     *   <li>For a single entity, its position (or empty if removed).</li>
     *   <li>For a group, the positions of all its members.</li>
     *   <li>For a point/dynamic, a single‑element list with that position.</li>
     * </ul>
     */
    public List<Vector2> getPositions(UniverseModel world) {
        if (single != null) {
            if (single.shouldRemove()) return Collections.emptyList();
            return Collections.singletonList(single.getTransform().getTranslation());
        }
        if (point != null) {
            return Collections.singletonList(point.copy());
        }
        if (dynamic != null) {
            Vector2 pos = dynamic.apply(world);
            return pos == null ? Collections.emptyList() : Collections.singletonList(pos);
        }
        if (multiple != null) {
            List<Vector2> positions = new ArrayList<>();
            for (AbstractEntityModel m : multiple) {
                if (!m.shouldRemove()) positions.add(m.getTransform().getTranslation());
            }
            return positions;
        }
        return Collections.emptyList();
    }

    // ── Convenience accessors (keep old names if needed) ──────────────────

    public AbstractEntityModel getEntity() { return single; }
    public List<AbstractEntityModel> getList() { return multiple; }
}