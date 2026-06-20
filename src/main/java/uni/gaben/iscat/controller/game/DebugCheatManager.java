package uni.gaben.iscat.controller.game;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.dyn4j.collision.CategoryFilter;
import uni.gaben.iscat.universe.UniverseCollisionLayers;
import uni.gaben.iscat.universe.camera.CameraModel;
import uni.gaben.iscat.universe.entities.player.PlayerModel;
import uni.gaben.iscat.universe.spawn.UniverseSpawner;

/**
 * Gestore dedicato alla logica dei trucchi e delle alterazioni di stato
 * utilizzate esclusivamente durante le sessioni di debug.
 */
public class DebugCheatManager {

    private final GameController mainController;
    private final BooleanProperty godMode = new SimpleBooleanProperty(false);

    /**
     * Inizializza il gestore dei cheat collegandolo al controller di gioco principale.
     *
     * @param mainController Il controller di gioco di riferimento
     */
    public DebugCheatManager(GameController mainController) {
        this.mainController = mainController;
    }

    public void debugHeal(double amount) {
        PlayerModel player = mainController.getPlayer();
        if (player != null) {
            player.alter(amount);
            System.out.println("[DEBUG CHEAT] Curato di: " + amount);
        }
    }

    public void debugDamage(double amount) {
        if (godMode.get()) return;
        PlayerModel player = mainController.getPlayer();
        if (player != null) {
            player.alter(-amount);
            System.out.println("[DEBUG CHEAT] Danno autoinflitto: " + amount);
        }
    }

    public void debugToggleGodMode() {
        this.godMode.set(!this.godMode.get());
        System.out.println("[DEBUG CHEAT] Godmode impostato a: " + godMode.get());

        PlayerModel player = mainController.getPlayer();
        if (player != null) {
            CategoryFilter filter = godMode.get()
                    ? new CategoryFilter(UniverseCollisionLayers.PLAYER, 0)
                    : UniverseCollisionLayers.PLAYER_FILTER;

            player.getFixtures().forEach(fixture -> fixture.setFilter(filter));
            System.out.println("[DEBUG CHEAT] Collisioni del giocatore aggiornate per rispecchiare lo stato Godmode.");
        }
    }

    public void debugLevelUp() {
        PlayerModel player = mainController.getPlayer();
        if (player != null) {
            player.levelUp();
            System.out.println("[DEBUG CHEAT] Livello aumentato! Livello attuale: " + player.getLevel());
        }
    }

    public void debugLevelDown() {
        PlayerModel player = mainController.getPlayer();
        if (player != null && player.getLevel() > 1) {
            player.levelProperty().set(player.getLevel() - 1);
            System.out.println("[DEBUG CHEAT] Livello diminuito! Livello attuale: " + player.getLevel());
        }
    }

    public void debugSpawn(String id) {
        CameraModel camera = mainController.getCameraModel();
        double x = camera.getX() + (Math.random() - 0.5) * 400;
        double y = camera.getY() + (Math.random() - 0.5) * 400;
        UniverseSpawner.getInstance().spawn(id, x, y);
    }

    public BooleanProperty godModeProperty() { return godMode; }
    public boolean isGodModeOn() { return godMode.get(); }
    public void reset() { this.godMode.set(false); }
}