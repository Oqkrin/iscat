package uni.gaben.iscat.controller.game;

import javafx.animation.AnimationTimer;
import uni.gaben.iscat.model.game.GameModel;

import java.util.function.DoubleConsumer;

/**
 * Timer e coordinatore del ciclo principale di gioco (Game Loop) basato su {@link AnimationTimer}.
 * Gestisce il calcolo del tempo delta (delta time) agganciato al refresh-rate dello schermo,
 * orchestrando in sequenza la fase di aggiornamento logico (update) e la fase di rendering (draw).
 * Provvede a incrementare il cronometro della sessione attiva escludendo i periodi di pausa e
 * applica un campionamento (clamping) sul delta time per prevenire instabilità fisiche (spiral of death).
 */
public class GameLoopTimer extends AnimationTimer {

    /** Il modello globale contenente i binding temporali, lo stato di pausa e la scala del tempo. */
    private final GameModel gameModel;

    /** Callback funzionale adibita all'aggiornamento logico delle entità, esposta come consumer del delta time. */
    private final DoubleConsumer updateCall;

    /** Callback opzionale contenente le istruzioni di rendering grafico su canvas. */
    private Runnable drawCall;

    /** Accumulatore interno del tempo effettivo di gioco trascorso, espresso in secondi. */
    private double totalElapsedSeconds = 0.0;

    /**
     * Costruisce il timer del loop legandolo al modello di gioco e alla routine di aggiornamento.
     *
     * @param gameModel  Il modello di gioco di riferimento.
     * @param updateCall Il consumatore deputato a ricevere il passo temporale per la logica.
     */
    public GameLoopTimer(GameModel gameModel, DoubleConsumer updateCall) {
        this.gameModel = gameModel;
        this.updateCall = updateCall;
    }

    /**
     * Assegna o aggiorna la routine delegata al disegno e al rendering della scena.
     *
     * @param drawCall Interfaccia funzionale contenente le istruzioni grafiche.
     */
    public void setDrawCall(Runnable drawCall) {
        this.drawCall = drawCall;
    }

    /**
     * Resetta integralmente gli accumulatori temporali interni e le proprietà reattive del modello,
     * garantendo che una nuova sessione di gioco parta esattamente dal secondo zero.
     */
    public void resetTimer() {
        totalElapsedSeconds = 0.0;
        gameModel.setTotalElapsedSeconds(0.0);
        gameModel.timerProperty().set(0);
        gameModel.setLastUpdate(0);
        gameModel.startProperty().set(-1);
    }

    /**
     * Callback interna invocata automaticamente da JavaFX a ogni fotogramma utile.
     * Calcola il delta time effettivo, aggiorna il cronometro formattato (HHMMSS) nel modello
     * in assenza di pausa, e invoca in successione sincrona la logica di update (scalata e clampata)
     * e la logica di disegno.
     *
     * @param now Il timestamp del frame corrente espresso in nanosecondi.
     */
    @Override
    public void handle(long now) {
        if (gameModel.getStart() == -1) gameModel.startProperty().set(now);
        if (gameModel.getLastUpdate() == 0) {
            gameModel.setLastUpdate(now);
            return;
        }

        // Aggiorna il timestamp corrente nel modello (aggiorna implicitamente il calcolo del dt)
        gameModel.setNow(now);

        double dt = gameModel.getDt();

        // Incrementa i contatori temporali solo se il gioco non è in pausa
        if (!gameModel.getGameState().isPaused()) {
            totalElapsedSeconds += dt;
            gameModel.setTotalElapsedSeconds(totalElapsedSeconds);

            int h = (int) (totalElapsedSeconds / 3600);
            int m = (int) ((totalElapsedSeconds % 3600) / 60);
            int s = (int) (totalElapsedSeconds % 60);
            // Codifica il tempo nel formato intero HHMMSS per scopi di binding testuale
            gameModel.timerProperty().set(h * 10000 + m * 100 + s);
        }

        // Previene salti fisici macroscopici limitando superiormente il delta time
        double clampedDt = Math.min(dt, GameModel.ACCUMULATORUNIT);

        // Fase di Update: elaborazione logica applicando l'eventuale moltiplicatore (Time Scale)
        if (updateCall != null) {
            updateCall.accept(clampedDt * gameModel.getTimeScale());
        }

        // Fase di Draw: rendering visivo su schermo
        if (drawCall != null) {
            drawCall.run();
        }

        gameModel.setLastUpdate(now);
    }
}