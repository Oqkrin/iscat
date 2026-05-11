package uni.gaben.iscat.gamenex.universe.iscat_mob;

import uni.gaben.iscat.gamenex.lib.implementations.AiBehaviours;
import uni.gaben.iscat.gamenex.lib.implementations.behaviors.ChaseBehavior;
import uni.gaben.iscat.gamenex.lib.implementations.behaviors.LookAtBehavior;

/**
 * Controller per l'entità IscatMob.
 * Gestisce l'intelligenza artificiale del mob collegando diversi comportamenti (AI Behaviors).
 * 
 * I comportamenti vengono eseguiti sequenzialmente:
 * 1. LookAtBehavior: Orienta il mob verso il giocatore.
 * 2. ChaseBehavior: Calcola la forza di sterzata per mantenere la distanza ideale.
 */
public class IscatMobController extends AiBehaviours<IscatMobModel> {

    /**
     * Inizializza il controller per un mob specifico.
     * Recupera tutti i parametri di tuning dalla classe centralizzata IscatMobSettings.
     * @param iscat Il modello fisico del mob da controllare.
     */
    public IscatMobController(IscatMobModel iscat) {
        super(iscat);
        
        // Comportamento di rotazione: gestisce l'orientamento fluido verso il target
        addBehavior(new LookAtBehavior(
                IscatMobSettings.ROTATION_STIFFNESS,
                IscatMobSettings.ROTATION_DAMPING,
                IscatMobSettings.AI_ACCURACY
        ));
        
        // Comportamento di inseguimento: gestisce il movimento fisico (steering)
        // Include ora i parametri di scalabilità della velocità in base alla distanza
        addBehavior(new ChaseBehavior(
                IscatMobSettings.DISTANZA_IDEALE_PX,
                IscatMobSettings.MAX_FORCE,
                IscatMobSettings.RAMP_UP_PX,
                IscatMobSettings.MAX_VELOCITY_MS,
                IscatMobSettings.STEERING_GAIN,
                IscatMobSettings.MIN_DIST_MULT,
                IscatMobSettings.MAX_DIST_MULT
        ));
    }
}
