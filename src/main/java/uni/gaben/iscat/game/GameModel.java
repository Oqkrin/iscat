package uni.gaben.iscat.game;

import javafx.scene.canvas.GraphicsContext;
import uni.gaben.iscat.game.components.entities.EntityModel;
import uni.gaben.iscat.game.components.entities.LivingEntityModel;
import uni.gaben.iscat.game.components.entities.npcs.NpcModel;
import uni.gaben.iscat.game.components.entities.npcs.iscat_bomber.IscatBomberController;
import uni.gaben.iscat.game.components.entities.npcs.iscat_bomber.IscatBomberModel;
import uni.gaben.iscat.game.components.entities.npcs.iscat_bomber.IscatBomberView;
import uni.gaben.iscat.game.components.entities.player.PlayerModel;
import uni.gaben.iscat.game.components.entities.player.PlayerView;
import uni.gaben.iscat.game.components.entities.player.projectile.ProjectileModel;
import uni.gaben.iscat.game.components.entities.player.projectile.ProjectileView;
import uni.gaben.iscat.game.utils.interfaces.*;
import uni.gaben.iscat.game.utils.physics.CollisionPhysics;

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
    
    // Typed collections for fast per-frame iteration (no instanceof each tick)
    private final List<NpcModel>       enemies            = new ArrayList<>();
    private final List<EntityModel>    allEntities        = new ArrayList<>();
    private final List<AI>             aiEntities         = new ArrayList<>();
    private final List<Gravitational>  gravitationalBodies = new ArrayList<>();
    private final List<Physical>       physicalBodies     = new ArrayList<>();
    private final List<Updatable>      updatableEntities  = new ArrayList<>();
    private final List<Collidable>     collidableEntities = new ArrayList<>();
    private final List<ProjectileModel> projectiles       = new ArrayList<>();

    /**
     * Ordered map: entity → its renderer.
     * Insertion order = draw order (back to front).
     * Renderer is typed as EntityRenderer<EntityModel> via unchecked cast at registration;
     * safe because each renderer only ever receives the entity it was registered for.
     */
    @SuppressWarnings("rawtypes")
    private final Map<EntityModel, EntityRenderer> renderers = new LinkedHashMap<>();

    // Pre-built renderer instances (stateless, reused for all entities of that type)
    private static final PlayerView      PLAYER_VIEW    = new PlayerView();
    private static final IscatBomberView BOMBER_VIEW    = new IscatBomberView();
    private static final ProjectileView  PROJECTILE_VIEW = new ProjectileView();

    public GameModel() {
        player = new PlayerModel(100, 100);
        addEntity(player);

        // colleghiamo lo sparo al player
        player.setOnSparo((pos, vel) -> {
            ProjectileModel p = new ProjectileModel(pos, vel);
            addEntity(p); // addEntity si occuperà di smistarlo in tutte le liste
        });
        
        // TODO: rimuovere dopo test - spawn IscatBomberModel di test
        spawnTestEnemies();
    }
    
    /** Spawn nemici di test. Rimuovere quando ci sarà un sistema di spawn vero. */
    private void spawnTestEnemies() {
        IscatBomberModel bomberModel = new IscatBomberModel(300, 100);
        IscatBomberController bomberController = new IscatBomberController(bomberModel);
        addEnemy(bomberModel, bomberController);
    }

    /** Avanza il mondo di un tick. */
    public void update(double dt) {
        updateAI(dt);
        applyGravity();
        updateAll(dt);
        resolveCollisions();
        cleanupDeadEntities();
    }

    // -- Dead entities ---
    private void cleanupDeadEntities() {
        // Raccogliamo chi deve essere rimosso
        List<EntityModel> toRemove = new ArrayList<>();

        for (EntityModel e : allEntities) {
            // Rimuovi nemici/player se morti
            if (e instanceof LivingEntityModel le && le.isDead()) {
                toRemove.add(e);
            }
            // Rimuovi proiettili se scaduti
            if (e instanceof ProjectileModel p && p.isExpired()) {
                toRemove.add(e);
            }
        }

        // Rimuoviamo effettivamente
        toRemove.forEach(this::removeEntity);
    }

    public List<ProjectileModel> getProjectiles() {
        return Collections.unmodifiableList(projectiles);
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
                if (a.collidesWith(b)) {
                    // 1. Risolvi la fisica (impulso + separazione) se entrambi sono Physical
                    if (a instanceof Physical pa && b instanceof Physical pb) {
                        // Non applicare la fisica d'urto se uno dei due è un proiettile
                        if (!(pa instanceof ProjectileModel || pb instanceof ProjectileModel)) {
                            CollisionPhysics.resolveElasticCollision(pa, pb);
                        }
                    }
                    // 2. Notifica le entità per la logica di gioco (stun, danno, ecc.)
                    a.onCollision(b);
                    b.onCollision(a);
                }
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

    /** Aggiunge un'entità generica al mondo e la registra nelle collezioni appropriate. */
    @SuppressWarnings("unchecked")
    public void addEntity(EntityModel entity) {
        allEntities.add(entity);
        
        if (entity instanceof AI ai)          aiEntities.add(ai);
        if (entity instanceof Gravitational g) gravitationalBodies.add(g);
        if (entity instanceof Physical p)      physicalBodies.add(p);
        if (entity instanceof Updatable u)     updatableEntities.add(u);
        if (entity instanceof Collidable c)    collidableEntities.add(c);
        if (entity instanceof ProjectileModel p) projectiles.add(p);

        // Register renderer — no instanceof needed at draw time
        if (entity instanceof PlayerModel)      renderers.put(entity, PLAYER_VIEW);
        else if (entity instanceof IscatBomberModel) renderers.put(entity, BOMBER_VIEW);
        else if (entity instanceof ProjectileModel)  renderers.put(entity, PROJECTILE_VIEW);
        // Entities with no renderer (e.g. BlackHole) are simply not drawn
    }

    /** Rimuove un'entità dal mondo e da tutte le collezioni. */
    public void removeEntity(EntityModel entity) {
        allEntities.remove(entity);
        renderers.remove(entity);

        if (entity instanceof NpcModel e)      enemies.remove(e);
        if (entity instanceof AI ai)           aiEntities.remove(ai);
        if (entity instanceof Gravitational g) gravitationalBodies.remove(g);
        if (entity instanceof Physical p)      physicalBodies.remove(p);
        if (entity instanceof Updatable u)     updatableEntities.remove(u);
        if (entity instanceof Collidable c)    collidableEntities.remove(c);
        if (entity instanceof ProjectileModel p) projectiles.remove(p);
    }

    // --- accessori ---

    public PlayerModel         getPlayer()     { return player; }
    public List<NpcModel>      getEnemies()    { return Collections.unmodifiableList(enemies); }
    public List<EntityModel>   getEntities()   { return Collections.unmodifiableList(allEntities); }

    /**
     * Returns the ordered entity→renderer map for the draw pass.
     * GameCanvas iterates this and calls draw() — no instanceof needed.
     */
    @SuppressWarnings("rawtypes")
    public Map<EntityModel, EntityRenderer> getRenderables() {
        return Collections.unmodifiableMap(renderers);
    }
}
