package uni.gaben.iscat.controller.components;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import uni.gaben.iscat.database.IscatDB;
import uni.gaben.iscat.database.dao.ScoreDAO;

import java.util.List;

/**
 * Controller per il componente della classifica (Leaderboard).
 * Gestisce il caricamento asincrono e la visualizzazione fluida dei punteggi
 * degli utenti salvati nel database, limitando la vista alla Top 100.
 */
public class LeaderboardComponentController {

    /** Contenitore principale del componente. */
    @FXML private VBox rootPane;

    /** Contenitore verticale in cui vengono iniettate dinamicamente le righe dei punteggi. */
    @FXML private VBox leaderboardContainer;

    /** Data Access Object per la gestione delle query sui punteggi. */
    private ScoreDAO scoreDAO;

    /**
     * Inizializza il componente recuperando l'istanza del DAO dei punteggi
     * e avviando il caricamento iniziale della classifica.
     */
    @FXML
    public void initialize() {
        scoreDAO = IscatDB.getInstance().getScoreDAO();
        loadLeaderboard();
    }

    /**
     * Carica i dati della classifica dal database in modo asincrono.
     * Mostra un indicatore di caricamento e aggiorna l'interfaccia sul thread
     * JavaFX (Platform.runLater) in caso di successo o di errore.
     */
    public void loadLeaderboard() {
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
     * Popola il container verticale generando le righe della classifica.
     * Se la lista è vuota mostra un messaggio di notifica, altrimenti mostra i record
     * interrompendo il ciclo se si supera la centesima posizione.
     *
     * @param scores La lista contenente le voci di punteggio degli utenti recuperate dal DB.
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
     * Costruisce e configura un nodo {@link GridPane} flessibile per rappresentare
     * visivamente una singola riga della classifica (posizione, nome e punteggio).
     * Applica uno stile speciale (leaderboard-gold) se l'utente occupa la prima posizione.
     *
     * @param rank  La posizione in classifica della riga corrente.
     * @param entry Il record contenente i dati di punteggio dell'utente.
     * @return Un'istanza di {@link GridPane} formattata per la classifica.
     */
    private GridPane buildRow(int rank, ScoreDAO.UserScoreEntry entry) {
        GridPane row = new GridPane();
        row.getStyleClass().add("leaderboard-row");

        // Configurazione fluida delle colonne (Abbandonate le dimensioni fisse)
        ColumnConstraints rankCol = new ColumnConstraints();
        rankCol.setHgrow(Priority.SOMETIMES);

        ColumnConstraints nameCol = new ColumnConstraints();
        nameCol.setHgrow(Priority.ALWAYS);

        ColumnConstraints scoreCol = new ColumnConstraints();
        scoreCol.setHgrow(Priority.SOMETIMES);
        scoreCol.setHalignment(HPos.RIGHT);

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
     * Converte la posizione numerica in una stringa testuale formattata,
     * sostituendo i primi tre posizionamenti con i rispettivi emoji delle medaglie.
     *
     * @param rank La posizione numerica in classifica.
     * @return Una stringa rappresentativa del posizionamento (es. "🥇 1°" o "4°").
     */
    private String rankBadge(int rank) {
        return switch (rank) {
            case 1 -> "🥇 1°";
            case 2 -> "🥈 2°";
            case 3 -> "🥉 3°";
            default -> rank + "°";
        };
    }
}