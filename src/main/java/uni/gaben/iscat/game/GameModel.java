package uni.gaben.iscat.game;

import uni.gaben.iscat.game.entities.EntityModel;
import uni.gaben.iscat.game.entities.LivingEntityModel;
import uni.gaben.iscat.game.enemies.EnemyModel;
import uni.gaben.iscat.game.enemies.iscat_bomber.IscatBomberModel;
import uni.gaben.iscat.game.interfaces.*;
import uni.gaben.iscat.game.physics.CollisionPhysics;
import uni.gaben.iscat.game.player.PlayerModel;
import uni.gaben.iscat.game.projectile.ProjectileModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    
    // Collezioni organizzate per tipo (evita instanceof ogni frame)
    private final List<EnemyModel>  enemies         = new ArrayList<>();
    private final List<EntityModel> allEntities     = new ArrayList<>();
    private final List<AI> aiEntities = new ArrayList<>();
    private final List<Gravitational> gravitationalBodies = new ArrayList<>();
    private final List<Physical> physicalBodies = new ArrayList<>();
    private final List<Updatable> updatableEntities = new ArrayList<>();
    private final List<Collidable> collidableEntities = new ArrayList<>();
    private final List<ProjectileModel> projectiles = new ArrayList<>();

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
        // Spawn un IscatBomberModel a destra del player
        IscatBomberModel bomber = new IscatBomberModel(300, 100);
        addEnemy(bomber);
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
    public void addEnemy(EnemyModel enemy) {
        enemies.add(enemy);
        addEntity(enemy);
    }

    /** Aggiunge un'entità generica al mondo e la registra nelle collezioni appropriate. */
    public void addEntity(EntityModel entity) {
        allEntities.add(entity);
        
        // Registra nelle collezioni tipizzate (fatto una volta, non ogni frame)
        if (entity instanceof AI ai) {
            aiEntities.add(ai);
        }
        if (entity instanceof Gravitational g) {
            gravitationalBodies.add(g);
        }
        if (entity instanceof Physical p) {
            physicalBodies.add(p);
        }
        if (entity instanceof Updatable u) {
            updatableEntities.add(u);
        }
        if (entity instanceof Collidable c) {
            collidableEntities.add(c);
        }
        if (entity instanceof ProjectileModel p) {
            projectiles.add(p);
        }
    }

    /** Rimuove un'entità dal mondo e da tutte le collezioni. */
    public void removeEntity(EntityModel entity) {
        allEntities.remove(entity);
        
        // Rimuovi da tutte le collezioni tipizzate
        if (entity instanceof EnemyModel e) {
            enemies.remove(e);
        }
        if (entity instanceof AI ai) {
            aiEntities.remove(ai);
        }
        if (entity instanceof Gravitational g) {
            gravitationalBodies.remove(g);
        }
        if (entity instanceof Physical p) {
            physicalBodies.remove(p);
        }
        if (entity instanceof Updatable u) {
            updatableEntities.remove(u);
        }
        if (entity instanceof Collidable c) {
            collidableEntities.remove(c);
        }
        if (entity instanceof ProjectileModel p) {
            projectiles.remove(p);
        }
    }

    // --- accessori ---

    public PlayerModel       getPlayer()   { return player; }
    public List<EnemyModel>  getEnemies()  { return Collections.unmodifiableList(enemies); }
    public List<EntityModel> getEntities() { return Collections.unmodifiableList(allEntities); }
}
