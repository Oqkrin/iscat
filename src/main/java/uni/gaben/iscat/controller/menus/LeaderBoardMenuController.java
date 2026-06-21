package uni.gaben.iscat.controller.menus;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import uni.gaben.iscat.IscatSettings;
import uni.gaben.iscat.controller.interfaces.IscatMenuController;
import uni.gaben.iscat.database.IscatDB;
import uni.gaben.iscat.database.dao.ScoreDAO;
import uni.gaben.iscat.utils.ComponentsUtils;

import java.util.List;

/**
 * Controller per la schermata della classifica (Leaderboard).
 * Gestisce il caricamento asincrono e la visualizzazione dei migliori punteggi
 * globali di tutti gli utenti registrati, ordinati in modo decrescente.
 * <p>
 * Include logiche di aggiornamento automatico della UI guidate dalla visibilità della scena
 * e gestisce stili grafici differenziati per i podi (top 3) della classifica.
 */
public class LeaderBoardMenuController implements IscatMenuController {

    /** Pannello contenitore principale della vista basato su VBox. */
    @FXML private VBox rootPane;
    /** Contenitore verticale in cui vengono iniettate dinamicamente le righe dei punteggi degli utenti. */
    @FXML private VBox leaderboardContainer;
    /** Pulsante per l'uscita dalla schermata della classifica e il ritorno al menu precedente. */
    @FXML private Button exitBtn;

    /** Riferimento al contenitore a nodi sovrapposti della vista. */
    private StackPane contentRoot;
    /** Oggetto di accesso ai dati (DAO) dedicato alle operazioni di query sui punteggi degli utenti. */
    private ScoreDAO scoreDAO;

    /**
     * Inizializza i componenti grafici della vista FXML.
     * Recupera l'istanza del DAO dal database, applica i glifi grafici al pulsante di uscita
     * e registra i listener reattivi per attivare il ricaricamento dei record ogni volta
     * che la vista torna ad essere visibile o associata a una scena attiva.
     */
    @FXML
    public void initialize() {

        scoreDAO = IscatDB.getInstance().getScoreDAO();

        ComponentsUtils.applyIconButton(exitBtn, "fas-sign-out-alt");

        // Ricarica la classifica quando il menu diventa visibile
        rootPane.visibleProperty().addListener((obs, wasVisible, isNowVisible) -> {
            if (isNowVisible) {
                loadLeaderboard();
            }
        });

        // Ricarica la classifica quando viene associata una scena
        rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                loadLeaderboard();
            }
        });

        loadLeaderboard();
    }

    /**
     * Interroga in via asincrona il database SQLITE tramite un thread dedicato (Worker Thread)
     * per estrarre la lista di tutti i record di punteggio.
     * <p>
     * Inietta una label di caricamento temporanea nella UI e ne gestisce asincronamente sia il successo
     * di popolamento (re-instradando l'esecuzione sul thread JavaFX primario tramite {@link Platform#runLater}),
     * sia l'eventuale fallimento o eccezione del canale di IO.
     */
    private void loadLeaderboard() {
        leaderboardContainer.getChildren().clear();
        Label loadingLabel = new Label("Caricamento classifica...");
        loadingLabel.getStyleClass().add("score-stat");
        leaderboardContainer.getChildren().add(loadingLabel);

        IscatDB.getInstance().queryAsync(() -> scoreDAO.getAllScores())
                .thenAccept(scores -> {
                    Platform.runLater(() -> populateRows(scores));
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        leaderboardContainer.getChildren().clear();
                        Label errorLabel = new Label("Errore nel caricamento dei dati.");
                        errorLabel.getStyleClass().add("score-stat");
                        leaderboardContainer.getChildren().add(errorLabel);
                    });
                    return null;
                });
    }

    /**
     * Svuota il contenitore e popola la lista delle righe grafiche della classifica basandosi sui record estratti.
     * Applica un vincolo algoritmico di interruzione (cap a 100 iterazioni) per limitare il rendering alla sola Top 100.
     *
     * @param scores Lista ordinata di oggetti {@link uni.gaben.iscat.database.dao.ScoreDAO.UserScoreEntry} da visualizzare.
     */
    private void populateRows(List<ScoreDAO.UserScoreEntry> scores) {
        leaderboardContainer.getChildren().clear();

        if (scores.isEmpty()) {
            Label empty = new Label("Nessun punteggio registrato");
            empty.getStyleClass().add("score-stat");
            leaderboardContainer.getChildren().add(empty);
            return;
        }

        int rank = 1;
        for (ScoreDAO.UserScoreEntry entry : scores) {
            leaderboardContainer.getChildren().add(buildRow(rank, entry));
            if (++rank > 100) break; // Limita a top 100
        }
    }

    /**
     * Fabbrica e struttura un nodo {@link GridPane} pre-configurato per ospitare le informazioni di un utente a tabellone.
     * Calcola i vincoli di colonna ed allineamento orizzontale e assegna classi CSS speciali
     * (es. {@code leaderboard-gold}) se la posizione analizzata corrisponde al gradino più alto del podio.
     *
     * @param rank  Posizione numerica ordinale occupata dall'utente all'interno della classifica.
     * @param entry Il record dati contenente il nome utente e il punteggio associato.
     * @return Un'istanza preconfigurata di {@link GridPane} rappresentante la riga formattata.
     */
    private GridPane buildRow(int rank, ScoreDAO.UserScoreEntry entry) {
        GridPane row = new GridPane();
        row.getStyleClass().add("leaderboard-row");
        row.setHgap(IscatSettings.STANDARD_UNIT);

        // Configurazione colonne
        ColumnConstraints rankCol = new ColumnConstraints();

        ColumnConstraints nameCol = new ColumnConstraints();

        ColumnConstraints scoreCol = new ColumnConstraints();
        scoreCol.setHalignment(HPos.RIGHT);

        nameCol.setHgrow(Priority.ALWAYS);
        row.getColumnConstraints().addAll(rankCol, nameCol, scoreCol);

        // Etichette della riga
        Label rankLabel = new Label(rankBadge(rank));
        rankLabel.getStyleClass().add("score-stat");

        Label nameLabel = new Label(entry.username());
        nameLabel.getStyleClass().add("score-stat");


        Label scoreLabel = new Label(String.format("%,d", entry.score()));
        scoreLabel.getStyleClass().add("score-stat");

        // Stile speciale per il primo classificato
        if (rank == 1) {
            nameLabel.getStyleClass().add("leaderboard-gold");
            scoreLabel.getStyleClass().add("leaderboard-gold");
        }

        row.add(rankLabel, 0, 0);
        row.add(nameLabel, 1, 0);
        row.add(scoreLabel, 2, 0);

        return row;
    }

    /**
     * Mappa ed associa un costrutto testuale contenente un badge emoji specifico alle prime tre posizioni
     * di testa della classifica, applicando una formattazione testuale standard per le restanti.
     *
     * @param rank La posizione ordinale intera da analizzare.
     * @return Una stringa formattata (es: "🥇 1°" oppure "4°").
     */
    private String rankBadge(int rank) {
        return switch (rank) {
            case 1 -> "🥇 1°";
            case 2 -> "🥈 2°";
            case 3 -> "🥉 3°";
            default -> rank + "°";
        };
    }

    /**
     * Gestisce l'evento di azione scatenato dal click sul pulsante di uscita.
     *
     * @param event L'evento di tipo {@link ActionEvent} catturato dalla UI.
     */
    @FXML
    private void handleBackAction(ActionEvent event) {
        handleBack();
    }

    /**
     * Interfaccia di aggancio del navigatore e del controller.
     *
     * @param pointer Il pannello contenitore di destinazione passato dal sistema di navigazione.
     */
    @Override
    public void setPointerToView(StackPane pointer) {
        this.contentRoot = pointer;
    }

    /**
     * Ritorna il pannello radice associato a questo specifico controller grafico.
     *
     * @return L'istanza di tipo {@link Pane} del pannello radice.
     */
    @Override
    public Pane getRootPane() {
        return rootPane;
    }
}