package uni.gaben.iscat.game;

import uni.gaben.iscat.game.components.entities.EntityModel;
import uni.gaben.iscat.game.components.entities.npcs.NpcModel;
import uni.gaben.iscat.game.components.entities.npcs.iscat_bomber.IscatBomberController;
import uni.gaben.iscat.game.components.entities.npcs.iscat_bomber.IscatBomberModel;
import uni.gaben.iscat.game.components.entities.npcs.iscat_mother.IscatMotherController;
import uni.gaben.iscat.game.components.entities.npcs.iscat_mother.IscatMotherModel;
import uni.gaben.iscat.game.components.entities.player.PlayerModel;
import uni.gaben.iscat.game.components.entities.player.projectile.ProjectileModel;
import uni.gaben.iscat.game.utils.interfaces.*;
import uni.gaben.iscat.game.utils.physics.CollisionPhysics;
import uni.gaben.iscat.game.utils.physics.Vec2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Mondo di gioco e radice dello stato.
 * Ogni tick ({@link #update}):
 * <ol>
 *   <li>Le entità AI aggiornano il loro comportamento.</li>
 *   <li>I corpi {@link Gravitational} applicano forze a tutti i {@link Physical}.</li>
 *   <li>Tutti gli {@link Updatable} avanzano il proprio stato.</li>
 *   <li>Il sistema di collisioni risolve gli overlap tra {@link Collidable}.</li>
 * </ol>
 * 
 * Ottimizzazione: le entità sono organizzate in collezioni tipizzate per evitare
 * instanceof checks ripetuti ogni frame.
 */
public class GameModel {

    private final PlayerModel player;
    
    // Typed collections for fast per-frame iteration
    private final List<NpcModel>      enemies            = new ArrayList<>();
    private final List<EntityModel>   allEntities        = new ArrayList<>();
    private final List<AI>            aiEntities         = new ArrayList<>();
    private final List<Gravitational> gravitationalBodies = new ArrayList<>();
    private final List<Physical>      physicalBodies     = new ArrayList<>();
    private final List<Updatable>     updatableEntities  = new ArrayList<>();
    private final List<Collidable>    collidableEntities = new ArrayList<>();

    /**
     * Ordered map: entity → its renderer. Insertion order = draw order.
     * Populated via HasRenderer — no instanceof per entity type needed.
     */
    @SuppressWarnings("rawtypes")
    private final Map<EntityModel, EntityRenderer> renderers = new LinkedHashMap<>();

    public GameModel() {
        player = new PlayerModel(100, 100);
        addEntity(player);
        
        // TODO: rimuovere dopo test - spawn IscatBomberModel di test
        spawnTestEnemies();
    }
    
    /** Spawn nemici di test. Rimuovere quando ci sarà un sistema di spawn vero. */
    private void spawnTestEnemies() {
        IscatBomberModel bomberModel = new IscatBomberModel(300, 100);
        IscatBomberController bomberController = new IscatBomberController(bomberModel);
        IscatMotherModel iscatMother = new IscatMotherModel(300, 100);
        IscatMotherController motherController = new IscatMotherController(iscatMother);
        addEnemy(bomberModel, bomberController);
        addEnemy(iscatMother, motherController);
    }

    /** Avanza il mondo di un tick. */
    public void update(double dt) {
        updateAI(dt);
        applyGravity();
        updateAll(dt);
        resolveCollisions();
        cleanupDeadEntities();
    }

    // -- Dead / expired entities ---
    private void cleanupDeadEntities() {
        List<EntityModel> toRemove = new ArrayList<>();
        for (EntityModel e : allEntities) {
            if (e instanceof Mortal m    && m.isDead())    toRemove.add(e);
            if (e instanceof Expirable x && x.isExpired()) toRemove.add(e);
        }
        toRemove.forEach(this::removeEntity);
    }
    
    // --- AI ---
    
    /** Aggiorna l'AI di tutte le entità che implementano AI. */
    private void updateAI(double dt) {
        for (AI ai : aiEntities) {
            ai.updateAI(this, dt);
        }
    }

    // --- gravità ---

    private void applyGravity() {
        for (Gravitational g : gravitationalBodies) {
            for (Physical p : physicalBodies) {
                if (g != p) { // non applicare gravità a se stesso
                    g.applyGravityTo(p);
                }
            }
        }
    }

    // --- aggiornamento ---

    private void updateAll(double dt) {
        for (Updatable u : updatableEntities) {
            u.update(dt);
        }
    }

    // --- collisioni ---

    private void resolveCollisions() {
        int size = collidableEntities.size();
        for (int i = 0; i < size; i++) {
            Collidable a = collidableEntities.get(i);
            for (int j = i + 1; j < size; j++) {
                Collidable b = collidableEntities.get(j);
                if (!a.collidesWith(b)) continue;

                // Physics impulse only between two Physical non-projectile bodies.
                // Projectiles use LAYER_PROJECTILE which has no mask overlap with each other,
                // so they never reach here against other projectiles.
                // We still skip impulse if either is Expirable (projectile-like) to avoid
                // sending bullets flying off at physics speeds.
                boolean aExpirable = a instanceof Expirable;
                boolean bExpirable = b instanceof Expirable;
                if (!aExpirable && !bExpirable
                        && a instanceof Physical pa && b instanceof Physical pb) {
                    CollisionPhysics.resolveElasticCollision(pa, pb);
                }

                a.onCollision(b);
                b.onCollision(a);
            }
        }
    }

    // --- gestione entità ---

    /** Aggiunge un nemico al mondo. */
    public void addEnemy(NpcModel enemy) {
        enemies.add(enemy);
        addEntity(enemy);
    }

    /**
     * Aggiunge un nemico con il suo controller AI separato.
     * Il model viene registrato nelle collezioni fisiche/collision,
     * il controller viene registrato nella collezione AI.
     */
    public void addEnemy(NpcModel enemy, AI controller) {
        enemies.add(enemy);
        addEntity(enemy);
        // Register the controller for AI updates (model itself no longer implements AI)
        aiEntities.add(controller);
    }

    @SuppressWarnings("unchecked")
    public void addEntity(EntityModel entity) {
        allEntities.add(entity);

        if (entity instanceof AI ai)           aiEntities.add(ai);
        if (entity instanceof Gravitational g)  gravitationalBodies.add(g);
        if (entity instanceof Physical p)       physicalBodies.add(p);
        if (entity instanceof Updatable u)      updatableEntities.add(u);
        if (entity instanceof Collidable c)     collidableEntities.add(c);
        if (entity instanceof HasRenderer hr)   renderers.put(entity, hr.getRenderer());
        if (entity instanceof Spawnable s)      s.onSpawn();
    }

    public void removeEntity(EntityModel entity) {
        allEntities.remove(entity);
        renderers.remove(entity);

        if (entity instanceof NpcModel e)       enemies.remove(e);
        if (entity instanceof AI ai)            aiEntities.remove(ai);
        if (entity instanceof Gravitational g)  gravitationalBodies.remove(g);
        if (entity instanceof Physical p)       physicalBodies.remove(p);
        if (entity instanceof Updatable u)      updatableEntities.remove(u);
        if (entity instanceof Collidable c)     collidableEntities.remove(c);
        if (entity instanceof Spawnable s)      s.onDespawn();
    }

    // --- accessori ---

    public PlayerModel         getPlayer()     { return player; }
    public List<NpcModel>      getEnemies()    { return Collections.unmodifiableList(enemies); }
    public List<EntityModel>   getEntities()   { return Collections.unmodifiableList(allEntities); }

    /** Spawns a projectile into the world. Called by PlayerShootingController. */
    public void spawnProjectile(Vec2 pos, Vec2 vel) {
        addEntity(new ProjectileModel(pos, vel));
    }

    /**
     * Returns the ordered entity→renderer map for the draw pass.
     * GameCanvas iterates this and calls draw() — no instanceof needed.
     */
    @SuppressWarnings("rawtypes")
    public Map<EntityModel, EntityRenderer> getRenderables() {
        return Collections.unmodifiableMap(renderers);
    }
}
