package uni.gaben.iscat.controller.menus;

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
import uni.gaben.iscat.controller.interfaces.IscatFxmlController;
import uni.gaben.iscat.model.IscatViews;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Controller per la gestione della schermata dei Crediti di gioco.
 * Gestisce l'animazione di scorrimento verticale (rolling credits), il reset completo
 * dello stato grafico ad ogni ingresso/uscita della vista e l'accelerazione temporanea
 * dell'animazione tramite la pressione prolungata della tastiera.
 */
public class CreditsController implements IscatFxmlController {

    /** Fattore moltiplicativo lineare applicato alla velocità di base dello scorrimento (1.0 = default, 2.0 = doppia velocità). */
    private static final double SPEED_FACTOR = 2.0;

    /** Moltiplicatore di accelerazione (Fast Forward) applicato all'animazione quando viene premuto il tasto Spazio. */
    private static final double FAST_FORWARD_MULTIPLIER = 5.0;

    /** Numero di pixel al secondo calcolati come base per lo scorrimento prima dell'applicazione dei fattori di scala. */
    private static final double BASE_PIXELS_PER_SECOND = 35.0;

    /** Pannello contenitore principale della vista. */
    @FXML private StackPane rootPane;
    /** Pannello di mascheramento geometrico utilizzato per ritagliare i testi fuori dall'area visibile. */
    @FXML private Pane clippingPane;
    /** Contenitore verticale in cui vengono iniettate dinamicamente le righe di testo dei crediti. */
    @FXML private VBox creditsContainer;
    /** Pulsante per l'interruzione anticipata dei crediti e il ritorno al menu principale. */
    @FXML private Button backButton;

    /** Transizione JavaFX dedicata allo scorrimento verticale traslatorio dell'interfaccia dei crediti. */
    private TranslateTransition scrollAnimation;

    /**
     * Inizializza i componenti grafici della vista FXML.
     * Configura la maschera rettangolare di clipping legandola reattivamente alle dimensioni del pannello,
     * registra i listener per l'avvio sicuro dell'animazione e delega il setup degli eventi da tastiera.
     */
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
     * Configura i gestori degli eventi (EventHandler) della tastiera sul pannello principale.
     * Intercetta la pressione del tasto {@link KeyCode#SPACE} per attivare l'accelerazione
     * temporanea dei crediti e il suo rilascio per ripristinare il flusso a velocità normale.
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
     * Predispone l'architettura grafica della vista azzerando lo stato delle animazioni attive,
     * svuotando il contenitore dei testi e richiedendo il focus asincrono per l'intercettazione dei comandi.
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

    /**
     * Effettua la lettura e il parsing sequenziale del file di testo dei crediti memorizzato nelle risorse del pacchetto.
     * Riconosce i tag strutturali (come i titoli o i marcatori di immagini) applicando dinamicamente
     * gli stili CSS e le spaziature corrette all'interno del contenitore.
     */
    private void loadCreditsFromFile() {
        String filePath = "/uni/gaben/iscat/credits/credits.txt";

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

    /**
     * Genera un contenitore orizzontale {@link HBox} per ospitare i file sprite promozionali o grafici
     * specificati all'interno della sequenza dei crediti.
     *
     * @return Un'istanza preconfigurata di {@link HBox} popolata con le relative immagini caricate in memoria.
     */
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

    /**
     * Verifica le condizioni geometriche minime necessarie all'avvio dell'animazione.
     * Se i calcoli di layout sui pannelli sono pronti, delega l'avvio effettivo sul thread JavaFX.
     */
    private void tryStartAnimation() {
        if (scrollAnimation == null && clippingPane.getHeight() > 0 && creditsContainer.getHeight() > 0) {
            Platform.runLater(this::startCreditsAnimation);
        }
    }

    /**
     * Formula l'animazione cinematografica di scorrimento applicando i princìpi fisici dello spazio e del tempo.
     * <p>
     * Calcola la durata esatta in secondi dividendo la distanza totale geometrica per la velocità
     * normalizzata (moltiplicata per il rispettivo parametro lineare {@link #SPEED_FACTOR}). Al termine,
     * programma il reindirizzamento automatico verso il menu di gioco principale.
     */
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

    /**
     * Gestisce l'evento di interruzione dello scorrimento o di ritorno indietro.
     * Arresta in modo sicuro la transizione di traslazione, azzera i listener di fine ciclo
     * e richiama il navigatore dell'applicazione per caricare la scena del menu principale tramite dissolvenza.
     */
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

    /**
     * Interfaccia di aggancio del navigatore e del controller.
     *
     * @param pointer Il pannello contenitore di destinazione passato dal sistema di navigazione.
     */
    @Override
    public void setPointerToView(StackPane pointer) {
        prepareView();
    }
}