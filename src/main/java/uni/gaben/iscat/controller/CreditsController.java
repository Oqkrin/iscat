package uni.gaben.iscat.controller;

import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import uni.gaben.iscat.IscatNavigator;
import uni.gaben.iscat.model.IscatViews;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Controller per la gestione della schermata dei Crediti di gioco.
 * Gestisce lo scorrimento, il reset completo ad ogni ingresso/uscita e l'accelerazione con tasto premuto.
 */
public class CreditsController implements IscatFxmlController {

    // REGOLAZIONE VELOCITÀ: 1.0 = Default, 2.0 = Doppia velocità, 0.5 = Metà velocità
    private static final double SPEED_FACTOR = 2.0;

    // Moltiplicatore di accelerazione quando si tiene premuto il tasto (5.0 = 5 volte più veloce)
    private static final double FAST_FORWARD_MULTIPLIER = 5.0;

    // Pixel al secondo di base (moltiplicati poi per lo SPEED_FACTOR)
    private static final double BASE_PIXELS_PER_SECOND = 35.0;

    @FXML private StackPane rootPane;
    @FXML private Pane clippingPane;
    @FXML private VBox creditsContainer;
    @FXML private Button backButton;

    private TranslateTransition scrollAnimation;

    @FXML
    public void initialize() {
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(clippingPane.widthProperty());
        clip.heightProperty().bind(clippingPane.heightProperty());
        clippingPane.setClip(clip);

        // Ascoltatori per far partire l'animazione solo quando i componenti sono pronti in memoria
        clippingPane.heightProperty().addListener((obs, oldVal, newVal) -> tryStartAnimation());
        creditsContainer.heightProperty().addListener((obs, oldVal, newVal) -> tryStartAnimation());

        // Setup dei controlli da tastiera
        setupKeyListeners();
    }

    /**
     * Configura l'ascolto dei tasti per gestire l'effetto "Fast Forward"
     */
    private void setupKeyListeners() {
        // Permette al rootPane di intercettare gli eventi della tastiera
        rootPane.setFocusTraversable(true);

        rootPane.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.SPACE && scrollAnimation != null) {
                scrollAnimation.setRate(FAST_FORWARD_MULTIPLIER);
            }
        });

        rootPane.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (event.getCode() == KeyCode.SPACE && scrollAnimation != null) {
                scrollAnimation.setRate(1.0); // Ritorna alla velocità normale impostata dallo SPEED_FACTOR
            }
        });
    }

    /**
     * Quando si entra nella vista per resettare lo stato e ricaricare i testi da zero.
     */
    public void prepareView() {
        // Ferma l'animazione precedente se ancora attiva
        if (scrollAnimation != null) {
            scrollAnimation.stop();
            scrollAnimation = null;
        }

        // Pulisce i vecchi nodi per evitare duplicazioni al rientro
        creditsContainer.getChildren().clear();
        creditsContainer.setOpacity(0);

        // Ricarica il file .txt aggiornato
        loadCreditsFromFile();

        // Forza il focus sul rootPane in modo che riceva immediatamente i tasti premuti
        Platform.runLater(() -> rootPane.requestFocus());
    }

    private void loadCreditsFromFile() {
        String filePath = "/uni/gaben/iscat/credits.txt";

        try (InputStream is = getClass().getResourceAsStream(filePath)) {
            if (is == null) {
                System.err.println("Errore: impossibile trovare il file dei crediti in: " + filePath);
                creditsContainer.getChildren().add(new Label("Impossibile caricare i crediti."));
                return;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();

                    if (line.isEmpty()) continue;

                    if (line.equalsIgnoreCase("[IMAGES_HBOX]")) {
                        creditsContainer.getChildren().add(createCreditsImagesBox());
                        continue;
                    }

                    Label label = new Label();

                    if (line.startsWith("#")) {
                        String titleText = line.substring(1).trim();
                        label.setText(titleText.toUpperCase());
                        label.getStyleClass().add("credit-role");

                        if (titleText.equalsIgnoreCase("ISCAT")) {
                            label.setStyle("-fx-font-size: 56px; -fx-font-weight: bold; -fx-text-fill: #ffaa00; -fx-padding: 40 0 20 0;");
                        } else if (titleText.equalsIgnoreCase("ISCAT WILL RETURN?")) {
                            label.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #ff3333; -fx-padding: 30 0 30 0;");
                        } else {
                            label.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #ffaa00; -fx-padding: 20 0 5 0;");
                        }
                    } else {
                        label.setText(line);
                        label.getStyleClass().add("credit-name");
                        label.setStyle("-fx-font-size: 16px; -fx-text-fill: #ffffff;");
                    }

                    creditsContainer.getChildren().add(label);
                }
            }
        } catch (Exception e) {
            System.err.println("Errore durante la lettura del file dei crediti.");
            e.printStackTrace();
        }
    }

    private HBox createCreditsImagesBox() {
        HBox hbox = new HBox(24);
        hbox.setAlignment(Pos.CENTER);
        hbox.setStyle("-fx-padding: 20 0 20 0;");

        String[] imagePaths = {
                "/uni/gaben/iscat/sprites/credits/img_credit1.png",
                "/uni/gaben/iscat/sprites/credits/img_credit2.png",
                "/uni/gaben/iscat/sprites/credits/img_credit3.png"
        };

        for (int i = 0; i < imagePaths.length; i++) {
            try {
                InputStream imgStream = getClass().getResourceAsStream(imagePaths[i]);
                if (imgStream != null) {
                    Image image = new Image(imgStream);
                    ImageView imageView = new ImageView(image);
                    imageView.setFitWidth(160);
                    imageView.setFitHeight(160);
                    imageView.setPreserveRatio(true);
                    imageView.setSmooth(false);

                    imageView.getStyleClass().add("credit-image");
                    hbox.getChildren().add(imageView);
                }
            } catch (Exception ex) {
                System.err.println("Errore nel caricamento dell'immagine: " + imagePaths[i]);
            }
        }
        return hbox;
    }

    private void tryStartAnimation() {
        if (scrollAnimation == null && clippingPane.getHeight() > 0 && creditsContainer.getHeight() > 0) {
            Platform.runLater(this::startCreditsAnimation);
        }
    }

    private void startCreditsAnimation() {
        creditsContainer.setOpacity(1);

        double startY = clippingPane.getHeight();
        double endY = -creditsContainer.getHeight();

        // Distanza totale da percorrere (dallo scompari sotto allo scompari sopra)
        double totalDistance = startY - endY;

        // CALCOLO VELOCITÀ REALE FORMULA fisica: Tempo = Spazio / Velocità
        // Moltiplichiamo i pixel al secondo per il nostro SPEED_FACTOR lineare richiesto
        double currentSpeed = BASE_PIXELS_PER_SECOND * SPEED_FACTOR;
        double durationSeconds = totalDistance / currentSpeed;

        scrollAnimation = new TranslateTransition(Duration.seconds(durationSeconds), creditsContainer);
        scrollAnimation.setFromY(startY);
        scrollAnimation.setToY(endY);
        scrollAnimation.setInterpolator(Interpolator.LINEAR);
        scrollAnimation.setCycleCount(1);

        scrollAnimation.setOnFinished(e -> handleBack());
        scrollAnimation.play();
    }

    @FXML
    public void handleBack() {
        if (scrollAnimation != null) {
            scrollAnimation.stop();
            scrollAnimation.setOnFinished(null);
        }

        if (clippingPane != null && creditsContainer != null) {
            creditsContainer.setTranslateY(clippingPane.getHeight());
            creditsContainer.setOpacity(0);
        }

        scrollAnimation = null;
        IscatNavigator.getInstance().navigateWithFade(IscatViews.MAIN_MENU);
    }

    @Override
    public void setContentRoot(StackPane contentRoot) {
        prepareView();
    }
}