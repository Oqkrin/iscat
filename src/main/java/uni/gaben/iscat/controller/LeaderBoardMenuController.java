package uni.gaben.iscat.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import uni.gaben.iscat.IscatNavigator;
import uni.gaben.iscat.database.IscatDB;
import uni.gaben.iscat.database.dao.ScoreDAO;
import uni.gaben.iscat.model.IscatViews;
import uni.gaben.iscat.utils.ComponentsUtils;

import java.util.List;

/**
 * Controller per la schermata della classifica (Leaderboard).
 * Mostra i migliori punteggi di tutti gli utenti ordinati in modo decrescente.
 */
public class LeaderBoardMenuController implements IscatMenuController {

    @FXML private BorderPane rootPane;
    @FXML private VBox leaderboardContainer;
    @FXML private Button exitBtn;

    private StackPane contentRoot;
    private ScoreDAO scoreDAO;

    @FXML
    public void initialize() {
        registerEscHandler();

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
     * Carica i dati della classifica dal database in modo asincrono.
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
     * Popola il container con le righe della classifica.
     * @param scores Lista di punteggi da visualizzare
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
     * Costruisce una singola riga della classifica.
     * @param rank Posizione in classifica
     * @param entry Dati dell'utente (username e punteggio)
     * @return Griglia contenente la riga formattata
     */
    private GridPane buildRow(int rank, ScoreDAO.UserScoreEntry entry) {
        GridPane row = new GridPane();
        row.setHgap(20);
        row.getStyleClass().add("leaderboard-row");

        // Configurazione colonne
        javafx.scene.layout.ColumnConstraints rankCol = new javafx.scene.layout.ColumnConstraints();
        rankCol.setMinWidth(60);

        javafx.scene.layout.ColumnConstraints nameCol = new javafx.scene.layout.ColumnConstraints();
        nameCol.setHgrow(javafx.scene.layout.Priority.ALWAYS);

        javafx.scene.layout.ColumnConstraints scoreCol = new javafx.scene.layout.ColumnConstraints();
        scoreCol.setHalignment(javafx.geometry.HPos.RIGHT);

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
     * Restituisce il badge testuale per la posizione in classifica.
     * @param rank Posizione (1 = oro, 2 = argento, 3 = bronzo)
     * @return Stringa formattata con emoji e numero
     */
    private String rankBadge(int rank) {
        return switch (rank) {
            case 1 -> "🥇 1°";
            case 2 -> "🥈 2°";
            case 3 -> "🥉 3°";
            default -> rank + "°";
        };
    }

    @FXML
    private void handleBackAction(ActionEvent event) {
        handleBack();
    }

    @Override
    public void setContentRoot(StackPane contentRoot) {
        this.contentRoot = contentRoot;
    }

    @Override
    public Pane getRootPane() {
        return rootPane;
    }
}