package uni.gaben.iscat.controller;

import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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
 * Implementa l'interfaccia IscatFxmlController richiesta dal sistema di navigazione personalizzato.
 */
public class CreditsController implements IscatFxmlController {

    @FXML private StackPane rootPane;
    @FXML private Pane clippingPane;          // Il riquadro visibile entro cui scorreranno i crediti
    @FXML private VBox creditsContainer;      // Il contenitore verticale dentro cui iniettiamo i testi
    @FXML private Button backButton;

    // Gestore dell'animazione di scorrimento verticale
    private TranslateTransition scrollAnimation;

    /**
     * Metodo di ciclo di vita di JavaFX. Viene eseguito automaticamente
     * non appena il file FXML viene caricato in memoria.
     */
    @FXML
    public void initialize() {
        // Creiamo un rettangolo geometrico per nascondere i testi quando escono dal riquadro visibile
        Rectangle clip = new Rectangle();
        // Leghiamo (bind) le dimensioni del rettangolo a quelle del pannello contenitore
        clip.widthProperty().bind(clippingPane.widthProperty());
        clip.heightProperty().bind(clippingPane.heightProperty());
        // Applichiamo il rettangolo come maschera di ritaglio sul pannello
        clippingPane.setClip(clip);

        // Nascondiamo inizialmente il VBox per evitare sfarfallii visivi prima del calcolo del layout
        creditsContainer.setOpacity(0);

        // Leggiamo i crediti dal file .txt esterno e popoliamo il VBox con dei nodi Label
        loadCreditsFromFile();

        // JavaFX calcola le altezze dei componenti in modo asincrono.
        // Monitoriamo le altezze del pannello e del testo: l'animazione partirà SOLO quando entrambe saranno > 0.
        clippingPane.heightProperty().addListener((obs, oldVal, newVal) -> tryStartAnimation());
        creditsContainer.heightProperty().addListener((obs, oldVal, newVal) -> tryStartAnimation());
    }

    /**
     * Legge dinamicamente il file di testo dei crediti inserito nelle risorse del progetto,
     * formattando le righe a seconda che siano titoli (#) o nomi/link standard.
     */
    private void loadCreditsFromFile() {
        String filePath = "/uni/gaben/iscat/credits.txt";

        // Apriamo il file come flusso di input dalle risorse (funziona anche dentro i file .jar compilati)
        try (InputStream is = getClass().getResourceAsStream(filePath)) {
            if (is == null) {
                System.err.println("Errore: impossibile trovare il file dei crediti in: " + filePath);
                // Fallback di emergenza: mostriamo un errore a schermo per non lasciare il gioco vuoto
                creditsContainer.getChildren().add(new Label("Impossibile caricare i crediti."));
                return;
            }

            // Usiamo BufferedReader per leggere il file riga per riga con codifica UTF-8 (supporta accenti e simboli)
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim(); // Rimuoviamo spazi vuoti all'inizio e alla fine della riga

                    if (line.isEmpty()) {
                        continue; // Salta le righe totalmente vuote nel file .txt
                    }

                    Label label = new Label(); // Creiamo un componente di testo per la riga corrente

                    // SEZIONE DI PARSING DEL FILE:
                    if (line.startsWith("#")) {
                        // Se la riga inizia con '#' è un titolo. Rimuoviamo il '#' e convertiamo in maiuscolo
                        String titleText = line.substring(1).trim();
                        label.setText(titleText.toUpperCase());
                        label.getStyleClass().add("credit-role"); // Stile CSS per i titoli

                        // CASO SPECIALE: Se il titolo è proprio il nome del gioco, lo facciamo gigante
                        if (titleText.equalsIgnoreCase("ISCAT")) {
                            label.setStyle("-fx-font-size: 56px; -fx-font-weight: bold; -fx-text-fill: #ffaa00; -fx-padding: 40 0 20 0;");
                        } else {
                            // Sottotitoli dei ruoli e delle canzoni (grandi e color oro)
                            label.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #ffaa00; -fx-padding: 20 0 5 0;");
                        }
                    } else {
                        // Se non inizia con '#' è un nome o un link di dettaglio (testo standard bianco)
                        label.setText(line);
                        label.getStyleClass().add("credit-name"); // Stile CSS per i nomi
                        label.setStyle("-fx-font-size: 16px; -fx-text-fill: #ffffff;");
                    }

                    // Iniettiamo il Label appena creato e formattato dentro il contenitore verticale grafico
                    creditsContainer.getChildren().add(label);
                }
            }
        } catch (Exception e) {
            System.err.println("Errore durante la lettura del file dei crediti.");
            e.printStackTrace();
        }
    }

    /**
     * Verifica centralizzata per l'avvio dell'animazione. Evita calcoli errati a schermo.
     */
    private void tryStartAnimation() {
        // Se l'animazione non è ancora stata creata, ed entrambi i componenti sono stati renderizzati (altezza > 0)
        if (scrollAnimation == null && clippingPane.getHeight() > 0 && creditsContainer.getHeight() > 0) {
            // Deleghiamo l'esecuzione al thread grafico di JavaFX per garantire la stabilità del layout
            Platform.runLater(this::startCreditsAnimation);
        }
    }

    /**
     * Configura fisicamente e avvia la transizione di movimento dei crediti dal basso verso l'alto.
     */
    private void startCreditsAnimation() {
        creditsContainer.setOpacity(1); // Rendiamo visibile il testo ora che le coordinate sono pronte

        // CALCOLO DELLE COORDINATE DI VIAGGIO:
        // Punto di partenza (startY): il testo si posiziona subito sotto il bordo inferiore del riquadro visibile
        double startY = clippingPane.getHeight();
        // Punto di arrivo (endY): il testo sale verso l'alto fino a quando la sua altezza non è completamente uscita dal bordo superiore
        double endY = -creditsContainer.getHeight();

        // REGOLAZIONE VELOCITÀ DINAMICA:
        // Moltiplichiamo i pixel totali del testo per un fattore (0.03).
        // Più il testo è lungo, più tempo impiegherà, mantenendo una velocità di scorrimento fluida e costante.
        double speedFactor = 0.03;
        double durationSeconds = Math.max(10, creditsContainer.getHeight() * speedFactor);

        // CONFIGURAZIONE DELLA TRANSIZIONE:
        scrollAnimation = new TranslateTransition(Duration.seconds(durationSeconds), creditsContainer);
        scrollAnimation.setFromY(startY); // Coordinata d'inizio Y
        scrollAnimation.setToY(endY);     // Coordinata di fine Y
        scrollAnimation.setInterpolator(Interpolator.LINEAR); // Movimento fluido e costante (senza accelerazioni/decelerazioni)
        scrollAnimation.setCycleCount(1); // L'animazione viene eseguita una sola volta

        // Quando il testo finisce di scorrere tutto ed esce dallo schermo, viene invocato automaticamente il reset di uscita
        scrollAnimation.setOnFinished(e -> handleBack());

        scrollAnimation.play(); // Avvia l'animazione cinematografica
    }

    /**
     * Gestisce la chiusura in sicurezza della schermata, pulendo la memoria e i nodi grafici.
     * Viene chiamato sia premendo il pulsante "MAIN MENU" sia al termine naturale dei crediti.
     */
    @FXML
    public void handleBack() {
        // 1. Se l'animazione è attiva, la stoppiamo subito per liberare la CPU
        if (scrollAnimation != null) {
            scrollAnimation.stop();
            scrollAnimation.setOnFinished(null); // Sganciamo il listener per evitare loop ricorsivi
        }

        // 2. RESET GEOMETRICO DELLO STATO:
        // Riportiamo forzatamente il VBox del testo nella sua posizione iniziale nascosta.
        // Se l'utente rientrerà nei crediti, l'animazione partirà da zero in modo pulito.
        if (clippingPane != null && creditsContainer != null) {
            creditsContainer.setTranslateY(clippingPane.getHeight());
            creditsContainer.setOpacity(0);
        }

        // 3. Azzeriamo il riferimento dell'oggetto per consentire un nuovo ciclo al prossimo initialize()
        scrollAnimation = null;

        // 4. Navigazione sfumata verso il Menu Principale tramite il Navigator del gioco
        IscatNavigator.getInstance().navigateWithFade(IscatViews.MAIN_MENU);
    }

    @Override
    public void setContentRoot(StackPane contentRoot) {
        // Metodo richiesto dall'interfaccia IscatFxmlController per la gestione del root nel Navigator
    }
}