package uni.gaben.iscat.game.model.entities;

import uni.gaben.iscat.game.model.interfaces.Collidable;
import uni.gaben.iscat.game.model.interfaces.Gravitational;
import uni.gaben.iscat.game.model.interfaces.Physical;
import uni.gaben.iscat.game.model.interfaces.Updatable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Mondo di gioco e radice dello stato.
 * Ogni tick ({@link #update}):
 * <ol>
 *   <li>I corpi {@link Gravitational} applicano forze a tutti i {@link Physical}.</li>
 *   <li>Tutti gli {@link Updatable} avanzano il proprio stato.</li>
 *   <li>Il sistema di collisioni risolve gli overlap tra {@link Collidable}.</li>
 * </ol>
 * Nessuna dipendenza da JavaFX o UI.
 */
public class GameModel {

    private final Player       player;
    private final List<Enemy>  enemies    = new ArrayList<>();
    private final List<Entity> allEntities = new ArrayList<>();

    public GameModel() {
        player = new Player(100, 100);
        allEntities.add(player);
    }

    /** Avanza il mondo di un tick. */
    public void update(double dt) {
        applyGravity();
        updateAll(dt);
        resolveCollisions();
    }

    // --- gravità ---

    private void applyGravity() {
        for (Entity e : allEntities) {
            if (!(e instanceof Gravitational g)) continue;
            for (Entity target : allEntities) {
                if (target != e && target instanceof Physical p) g.applyGravityTo(p);
            }
        }
    }

    // --- aggiornamento ---

    private void updateAll(double dt) {
        for (Entity e : allEntities) {
            if (e instanceof Updatable u) u.update(dt);
        }
    }

    // --- collisioni ---

    private void resolveCollisions() {
        List<Collidable> collidables = new ArrayList<>();
        for (Entity e : allEntities) {
            if (e instanceof Collidable c) collidables.add(c);
        }
        for (int i = 0; i < collidables.size(); i++) {
            for (int j = i + 1; j < collidables.size(); j++) {
                Collidable a = collidables.get(i), b = collidables.get(j);
                if (a.collidesWith(b)) { a.onCollision(b); b.onCollision(a); }
            }
        }
    }

    // --- gestione entità ---

    /** Aggiunge un nemico al mondo. */
    public void addEnemy(Enemy enemy) {
        enemies.add(enemy);
        allEntities.add(enemy);
    }

    /** Aggiunge un'entità generica al mondo. */
    public void addEntity(Entity entity) {
        allEntities.add(entity);
    }

    /** Rimuove un'entità dal mondo. */
    public void removeEntity(Entity entity) {
        allEntities.remove(entity);
        if (entity instanceof Enemy e) enemies.remove(e);
    }

    // --- accessori ---

    public Player       getPlayer()   { return player; }
    public List<Enemy>  getEnemies()  { return Collections.unmodifiableList(enemies); }
    public List<Entity> getEntities() { return Collections.unmodifiableList(allEntities); }
}
