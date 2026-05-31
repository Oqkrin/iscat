package uni.gaben.iscat.universe.enemies.healer;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import uni.gaben.iscat.database.sqlite.EnemyDAO;
import uni.gaben.iscat.universe.enemies.generic.GenericEntitySettings;
import uni.gaben.iscat.universe.lib.implementations.LivingEntityModel;
import uni.gaben.iscat.utils.Updatable;
import uni.gaben.iscat.universe.lib.interfaces.model.HasShockwave;
import uni.gaben.iscat.universe.rendering.vfx.ShockwaveModel;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseCollisionLayers;

public class IscatHealerModel extends LivingEntityModel implements HasShockwave, Updatable {

    private static final String ENTITY_KEY = "iscat_healer";

    private static final double FB_INIT_LIFE  = 80.0;
    private static final double FB_DIM_SPRITE = 32.0;
    private static final double FB_SCALE      = 2.0;
    private static final double FB_DAMPING    = 3.0;
    private static final int    FB_XP_REWARD  = 100;

    private final ShockwaveModel healingWave = new ShockwaveModel();
    private final GenericEntitySettings settings;

    public IscatHealerModel(double x, double y) {
        this(x, y, loadSettings());
    }

    private IscatHealerModel(double x, double y, GenericEntitySettings s) {
        super(x, y, s.initLife, s.initLife);
        this.settings = s;

        setXpReward(s.xpReward);

        BodyFixture fixture = addFixture(
                Geometry.createCircle(
                        UU.pxToM(s.dimSprite * s.scale / 2.0 * 0.9)));

        fixture.setFilter(UniverseCollisionLayers.ENEMY_FILTER);
        setMass(MassType.NORMAL);
        setLinearDamping(s.dampingLineare);
    }

    private static GenericEntitySettings loadSettings() {
        return EnemyDAO.findByKey(ENTITY_KEY).orElseGet(() -> {
            System.err.println("[IscatHealerModel] DB row not found, using fallback values.");
            GenericEntitySettings s = new GenericEntitySettings();
            s.initLife       = FB_INIT_LIFE;
            s.dimSprite      = FB_DIM_SPRITE;
            s.scale          = FB_SCALE;
            s.dampingLineare = FB_DAMPING;
            s.xpReward       = FB_XP_REWARD;
            return s;
        });
    }

    public GenericEntitySettings getSettings() { return settings; }

    @Override public ShockwaveModel shockwave() { return healingWave; }

    @Override
    public void update(double dt) { healingWave.update(dt); }

    @Override
    public double getTerminalVelocity() { return settings.maxVelocity; }
}