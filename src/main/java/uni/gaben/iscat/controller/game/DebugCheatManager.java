package uni.gaben.iscat.controller.game;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.dyn4j.collision.CategoryFilter;
import uni.gaben.iscat.universe.UniverseCollisionLayers;
import uni.gaben.iscat.universe.camera.CameraModel;
import uni.gaben.iscat.universe.entities.player.PlayerModel;
import uni.gaben.iscat.universe.spawn.UniverseSpawner;

/**
 * Gestore dedicato alla logica dei trucchi (cheats) e delle alterazioni di stato.
 * Fornisce un set di strumenti diagnostici utilizzabili esclusivamente durante le sessioni
 * di debug, come l'immunità ai danni (God Mode), la cura istantanea, la modifica del livello
 * del giocatore e lo spawn arbitrario di entità nel mondo di gioco.
 */
public class DebugCheatManager {

    /** Riferimento al controller di gioco principale per l'accesso ai modelli attivi di sessione. */
    private final GameController mainController;

    /** Proprietà booleana che traccia e notifica lo stato di attivazione della modalità invulnerabilità. */
    private final BooleanProperty godMode = new SimpleBooleanProperty(false);

    /**
     * Inizializza il gestore dei cheat collegandolo al controller di gioco principale.
     *
     * @param mainController Il controller di gioco di riferimento.
     */
    public DebugCheatManager(GameController mainController) {
        this.mainController = mainController;
    }

    /**
     * Ripristina istantaneamente una quantità definita di punti vita (integrità dello scafo)
     * al modello del giocatore, se esistente.
     *
     * @param amount La quantità di punti vita da rigenerare.
     */
    public void debugHeal(double amount) {
        PlayerModel player = mainController.getPlayer();
        if (player != null) {
            player.alter(amount);
            System.out.println("[DEBUG CHEAT] Curato di: " + amount);
        }
    }

    /**
     * Applica una quantità definita di danno autoinflitto al giocatore.
     * L'operazione viene ignorata se la modalità God Mode risulta attiva.
     *
     * @param amount La quantità di danno da infliggere.
     */
    public void debugDamage(double amount) {
        if (godMode.get()) return;
        PlayerModel player = mainController.getPlayer();
        if (player != null) {
            player.alter(-amount);
            System.out.println("[DEBUG CHEAT] Danno autoinflitto: " + amount);
        }
    }

    /**
     * Alterna lo stato di attivazione della God Mode (invulnerabilità).
     * Modifica dinamicamente le maschere di collisione di dyn4j sulle fixture del giocatore
     * per disabilitare i riscontri con i livelli di minaccia ostili o ripristinare i filtri di default.
     */
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

    /**
     * Incrementa istantaneamente di un'unità il livello del giocatore, innescando
     * le relative logiche interne di evoluzione e aggiornamento delle statistiche della nave.
     */
    public void debugLevelUp() {
        PlayerModel player = mainController.getPlayer();
        if (player != null) {
            player.levelUp();
            System.out.println("[DEBUG CHEAT] Livello aumentato! Livello attuale: " + player.getLevel());
        }
    }

    /**
     * Decrementa di un'unità il livello attuale del giocatore, purché il livello corrente sia superiore a 1.
     */
    public void debugLevelDown() {
        PlayerModel player = mainController.getPlayer();
        if (player != null && player.getLevel() > 1) {
            player.levelProperty().set(player.getLevel() - 1);
            System.out.println("[DEBUG CHEAT] Livello diminuito! Livello attuale: " + player.getLevel());
        }
    }

    /**
     * Esegue lo spawn forzato di un'entità nell'universo di gioco calcolandone la posizione
     * in un raggio casuale attorno alle coordinate correnti della telecamera (inquadratura visiva).
     *
     * @param id L'identificativo testuale (chiave interna) dell'entità da generare tramite l'{@link UniverseSpawner}.
     */
    public void debugSpawn(String id) {
        CameraModel camera = mainController.getCameraModel();
        double x = camera.getX() + (Math.random() - 0.5) * 400;
        double y = camera.getY() + (Math.random() - 0.5) * 400;
        UniverseSpawner.getInstance().spawn(id, x, y);
    }

    /**
     * Restituisce la proprietà booleana associata allo stato della God Mode per scopi di binding.
     *
     * @return La {@link BooleanProperty} dell'invulnerabilità.
     */
    public BooleanProperty godModeProperty() { return godMode; }

    /**
     * Verifica se la modalità God Mode è correntemente abilitata.
     *
     * @return {@code true} se attiva, {@code false} altrimenti.
     */
    public boolean isGodModeOn() { return godMode.get(); }

    /**
     * Ripristina lo stato del gestore disattivando forzatamente la modalità God Mode.
     */
    public void reset() { this.godMode.set(false); }
}