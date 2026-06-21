package uni.gaben.iscat.universe.entities.shooters;

import uni.gaben.iscat.universe.entities.projectiles.ProjectileModel;
import uni.gaben.iscat.universe.entities.projectiles.ProjectileType;
import uni.gaben.iscat.universe.entities.projectiles.Shooter;
import java.util.function.Consumer;

/**
 * Astrazione strategica per la generazione di raffiche di proiettili (Bullet Hell Pattern Interface).
 * <p>
 * Rappresenta l'interfaccia funzionale alla base del sistema di sbarramento del gioco. Implementa il pattern
 * <b>Strategy</b>, consentendo alle entità dotate di un modulo {@link Shooter} di variare dinamicamente
 * la geometria o la logica di sparo (es. sventagliate a cono, geometrie polari, linee parallele) senza dover
 * ricompilare o alterare il codice dell'emettitore stesso.
 * </p>
 */
@FunctionalInterface
public interface Pattern {

    /**
     * Esegue l'algoritmo di calcolo spaziale e innesca l'emissione dei proiettili nel mondo virtuale.
     *
     * @param shooter    L'istanza del modulo {@link Shooter} associata all'entità che sta eseguendo l'attacco.
     * @param type       Il tipo di munizione (identificatore strutturale del proiettile) da istanziare.
     * @param angle      L'angolo di puntamento nominale (asse di mira in radianti) impostato dall'emettitore.
     * @param customizer Un callback funzionale {@link Consumer} opzionale, utilizzato per iniettare mutazioni esterne
     * ai singoli parametri dei proiettili (es. variazioni di colore, modificatori di danno o tag)
     * all'atto dello spawn.
     */
    void execute(Shooter<?> shooter, ProjectileType type, double angle, Consumer<ProjectileModel> customizer);
}