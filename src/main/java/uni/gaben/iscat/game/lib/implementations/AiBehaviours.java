package uni.gaben.iscat.game.lib.implementations;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import uni.gaben.iscat.game.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.game.lib.interfaces.controller.AiBehavior;
import uni.gaben.iscat.game.lib.interfaces.controller.AiController;
import uni.gaben.iscat.game.universe.UniverseModel;

/**
 * Controller che "possiede" un NPC ed esegue una lista di comportamenti in sequenza.
 * Implementa il pattern Strategy per permettere la composizione modulare dell'IA.
 * 
 * @param <T> Il tipo specifico di modello dell'entità controllata.
 */
public class AiBehaviours<T extends AbstractEntityModel> implements AiController {

    /** L'entità fisica controllata da questo sistema. */
    protected final T aiEntity;
    /** Lista dei comportamenti atomici da eseguire ad ogni frame. */
    private final List<AiBehavior> behaviors = new ArrayList<>();
    private final PriorityQueue<Runnable> taskList = new PriorityQueue<>();

    /**
     * Crea un controller per un'entità specifica.
     * @param aiEntity Il modello fisico dell'entità.
     */
    public AiBehaviours(T aiEntity) {
        this.aiEntity = aiEntity;
    }

    /**
     * Aggiunge un nuovo comportamento alla sequenza di esecuzione.
     * @param behavior Il comportamento (es. ChaseBehavior) da aggiungere.
     */
    public void addBehavior(AiBehavior behavior) {
        this.behaviors.add(behavior);
    }

    /**
     * Aggiorna lo stato logico dell'entità eseguendo il comportamento con priorità più alta.
     * @param universeModel Il modello dell'universo per dati ambientali.
     * @param dt Il tempo trascorso dall'ultimo aggiornamento.
     */
    @Override
    public void aiUpdate(UniverseModel universeModel, double dt) {
        if (behaviors.isEmpty()) return;

        // Tick globale: avanza timer/cooldown di tutti i behavior ogni frame
        for (AiBehavior behavior : behaviors) {
            behavior.tick(aiEntity, universeModel, dt);
        }

        // Selezione e esecuzione del behavior con priorità più alta
        PriorityQueue<AiBehavior> queue = new PriorityQueue<>((a, b) ->
                Double.compare(b.getPriority(aiEntity, universeModel), a.getPriority(aiEntity, universeModel))
        );
        queue.addAll(behaviors);

        AiBehavior currentTask = queue.peek();
        if (currentTask != null && currentTask.getPriority(aiEntity, universeModel) > 0) {
            currentTask.execute(aiEntity, universeModel, dt);
        }

        // Behavior paralleli (priorità -1.0)
        for (AiBehavior behavior : behaviors) {
            if (behavior.getPriority(aiEntity, universeModel) == -1.0) {
                behavior.execute(aiEntity, universeModel, dt);
            }
        }
    }
}