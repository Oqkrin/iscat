package uni.gaben.iscat.gamenex.lib.implementations;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import uni.gaben.iscat.gamenex.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.gamenex.lib.interfaces.controller.AiBehavior;
import uni.gaben.iscat.gamenex.lib.interfaces.controller.AiController;
import uni.gaben.iscat.gamenex.universe.UniverseModel;
import uni.gaben.iscat.gamenex.universe.iscat_mob.IscatMobController;

/**
 * Controller che "possiede" un NPC ed esegue una lista di comportamenti in sequenza.
 * Implementa il pattern Strategy per permettere la composizione modulare dell'IA.
 * 
 * @param <T> Il tipo specifico di modello dell'entità controllata.
 */
public class AiBehaviours<T extends AbstractEntityModel> implements AiController {

    /** L'entità fisica controllata da questo sistema. */
    protected final T npc;
    /** Lista dei comportamenti atomici da eseguire ad ogni frame. */
    private final List<AiBehavior> behaviors = new ArrayList<>();
    private final PriorityQueue<Runnable> taskList = new PriorityQueue<>();

    /**
     * Crea un controller per un'entità specifica.
     * @param npc Il modello fisico dell'entità.
     */
    public AiBehaviours(T npc) {
        this.npc = npc;
    }

    /**
     * Aggiunge un nuovo comportamento alla sequenza di esecuzione.
     * @param behavior Il comportamento (es. ChaseBehavior) da aggiungere.
     */
    public void addBehavior(AiBehavior behavior) {
        this.behaviors.add(behavior);
    }

    /**
     * Aggiorna lo stato logico dell'entità eseguendo tutti i comportamenti registrati.
     * @param universeModel Il modello dell'universo per dati ambientali.
     * @param dt Il tempo trascorso dall'ultimo aggiornamento.
     */
    @Override
    public void aiUpdate(UniverseModel universeModel, double dt) {
        for (AiBehavior behavior : behaviors) {
            behavior.execute(npc, universeModel, dt);
        }
    }
}