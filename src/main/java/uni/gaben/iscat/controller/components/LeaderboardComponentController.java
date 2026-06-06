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
 * Può essere iniettato in qualsiasi altra schermata o layout flessibile.
 */
public class LeaderboardComponentController {

    @FXML private VBox rootPane;
    @FXML private VBox leaderboardContainer;

    private ScoreDAO scoreDAO;

    @FXML
    public void initialize() {
        scoreDAO = IscatDB.getInstance().getScoreDAO();
        loadLeaderboard();
    }

    /**
     * Carica i dati della classifica dal database in modo asincrono.
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
     * Popola il container con le righe della classifica.
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
     * Costruisce una singola riga della classifica fluida.
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

    private String rankBadge(int rank) {
        return switch (rank) {
            case 1 -> "🥇 1°";
            case 2 -> "🥈 2°";
            case 3 -> "🥉 3°";
            default -> rank + "°";
        };
    }
}