package uni.gaben.iscat.controller.game;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import uni.gaben.iscat.IscatNavigator;
import uni.gaben.iscat.controller.interfaces.IscatFxmlController;
import uni.gaben.iscat.model.IscatViews;
import uni.gaben.iscat.model.game.GameState;
import uni.gaben.iscat.utils.ComponentsUtils;
import uni.gaben.iscat.utils.SessionScoreTracker;
import uni.gaben.iscat.view.game.GameView;

/**
 * Controller FXML delegato alla gestione delle schermate di fine partita, operando sia come
 * menu di Game Over (sconfitta del giocatore) sia come schermata di Vittoria (Win).
 * Controlla in modo reattivo lo stato della sessione per rimodulare la visibilità dei componenti grafici,
 * formattare il punteggio finale tramite il {@link SessionScoreTracker} e coordinare i flussi di navigazione
 * (riprova, ritorno al menu o prosecuzione verso i titoli di coda).
 */
public class GameOverMenuController implements IscatFxmlController {

    /** Etichetta di testo principale utilizzata per mostrare l'esito della partita (es. "YOU DIED" o "HAI VINTO"). */
    @FXML private Label titleLabel;

    /** Etichetta di testo formattata per la visualizzazione del punteggio (Score) totalizzato nella sessione. */
    @FXML private Label sessionScoreLabel;

    /** Pulsante interattivo visibile solo in caso di vittoria per proseguire nei flussi applicativi. */
    @FXML private Button continueBtn;

    /** Pulsanti di controllo standard per il riavvio della partita, il rientro al menu principale o la chiusura del software. */
    @FXML private Button retryBtn, menuBtn, quitBtn;

    /** Riferimento al controller principale del ciclo di gioco per l'invocazione dei comandi di reset o disallocazione. */
    private GameController gameController;

    /** Riferimento alla vista di gioco principale per l'iniezione delle animazioni e delle transizioni di stato visive. */
    private GameView gameView;

    /**
     * Inizializza i componenti grafici iniettati tramite FXML.
     * Configura le icone grafiche vettoriali sui pulsanti e applica le interpolazioni di animazione (tweening)
     * per gestire gli effetti visivi al passaggio del cursore del mouse (hover).
     */
    @FXML
    public void initialize() {
        ComponentsUtils.applyIconButton(continueBtn, "fas-arrow-right");
        ComponentsUtils.applyIconButton(retryBtn,    "fas-redo");
        ComponentsUtils.applyIconButton(menuBtn,     "fas-home");
        ComponentsUtils.applyIconButton(quitBtn,     "fas-power-off");

        ComponentsUtils.setupButtonHoverTween(continueBtn);
        ComponentsUtils.setupButtonHoverTween(retryBtn);
        ComponentsUtils.setupButtonHoverTween(menuBtn);
        ComponentsUtils.setupButtonHoverTween(quitBtn);
    }

    /**
     * Inietta i puntatori ai moduli core di gioco e registra un listener reattivo sulla proprietà
     * dello stato della partita. Intercetta i passaggi a {@link GameState#GAME_OVER} o {@link GameState#WIN}
     * per alterare dinamicamente i testi e la disposizione dei pulsanti a schermo.
     *
     * @param controller Il controller logico della sessione di gioco.
     * @param view       La vista grafica associata al gameplay.
     */
    public void initData(GameController controller, GameView view) {
        this.gameController = controller;
        this.gameView       = view;

        controller.getGameModel().gameStateProperty().addListener((obs, oldState, newState) -> {
            if (newState == GameState.GAME_OVER) {
                titleLabel.setText("YOU DIED");
                sessionScoreLabel.setText(buildScoreText());
                setWinMode(false);
            }
            if (newState == GameState.WIN) {
                titleLabel.setText("HAI VINTO");
                sessionScoreLabel.setText(buildScoreText());
                setWinMode(true);
            }
        });
    }

    /**
     * Modula selettivamente la visibilità e la gestione del layout (managed property) dei pulsanti.
     * Se la modalità vittoria è attiva mostra unicamente il controllo di continuazione, altrimenti
     * abilita la tripletta standard di fallimento (Riprova, Menu, Esci).
     *
     * @param win {@code true} per configurare l'interfaccia in assetto Vittoria, {@code false} per l'assetto Sconfitta.
     */
    private void setWinMode(boolean win) {
        continueBtn.setVisible(win);
        continueBtn.setManaged(win);

        retryBtn.setVisible(!win);
        retryBtn.setManaged(!win);
        menuBtn.setVisible(!win);
        menuBtn.setManaged(!win);
        quitBtn.setVisible(!win);
        quitBtn.setManaged(!win);
    }

    /**
     * Estrae il punteggio accumulato dal tracciatore di sessione singleton e lo formatta in una stringa localizzata.
     *
     * @return Stringa testuale formattata nel pattern "SCORE: X,XXX".
     */
    private String buildScoreText() {
        int score = SessionScoreTracker.getInstance().getScore();
        return String.format("SCORE: %,d", score);
    }

    /**
     * Gestisce la transizione alla pressione del tasto continua. Disalloca la partita corrente
     * e reindirizza l'utente verso la schermata dei riconoscimenti (Credits).
     */
    @FXML
    private void handleContinue() {
        if (gameController != null) gameController.quitToMainMenu();
        IscatNavigator.getInstance().navigateWithFade(IscatViews.CREDITS);
    }

    /**
     * Gestisce il comando di riavvio istantaneo della partita. Ripristina lo stato visivo
     * su PLAYING e ordina al controller di azzerare e rigenerare l'universo fisico.
     */
    @FXML
    private void handleRetry() {
        if (gameView != null) gameView.transitionTo(GameState.PLAYING);
        if (gameController != null) gameController.retryGame();
    }

    /**
     * Interrompe la sessione corrente ed effettua il rientro controllato verso il menu principale.
     */
    @FXML
    private void handleQuitToMenu() {
        if (gameController != null) gameController.quitToMainMenu();
    }

    /**
     * Forza la chiusura immediata dell'istanza dell'applicazione.
     */
    @FXML
    private void handleQuitGame() {
        if (gameController != null) gameController.quitGame();
    }

    /**
     * Metodo di interfaccia implementato da {@link IscatFxmlController}. In questo specifico
     * sotto-componente di overlay non richiede l'ancoraggio di nodi radice esterni.
     */
    @Override
    public void setPointerToView(StackPane pointer) {}
}