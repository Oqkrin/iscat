package uni.gaben.iscat.utils.theme;

import javafx.scene.paint.Color;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Classe di utilità robusta per l'estrazione di variabili di colore custom da file CSS
 * (formato {@code -nome-variabile: valore;}) e la loro conversione diretta in istanze {@link Color} di JavaFX.
 */
public final class CssColorParser {

    private CssColorParser() {
        /* Questa classe di utilità non deve essere istanziata */
    }

    /**
     * Analizza un file CSS inserito come risorsa interna nel JAR dell'applicazione e mappa le sue variabili.
     *
     * @param cssResourcePath Il percorso assoluto della risorsa nel classpath (es. "/styles/theme.css").
     * @return Una {@link Map} che associa il nome della variabile (senza i trattini iniziali) al rispettivo {@link Color}.
     */
    public static Map<String, Color> parseColors(String cssResourcePath) {
        try (InputStream is = CssColorParser.class.getResourceAsStream(cssResourcePath)) {
            if (is == null) {
                System.err.println("[CssColorParser] Errore: File CSS interno non trovato: " + cssResourcePath);
                return new HashMap<>();
            }
            return parseFromStream(is);
        } catch (Exception e) {
            System.err.println("[CssColorParser] Eccezione critica durante l'analisi dello stylesheet interno: " + cssResourcePath);
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    /**
     * Analizza un file CSS fisico situato esternamente sul disco (es. in una cartella temporanea o di modding).
     *
     * @param externalCssFile L'oggetto {@link File} che punta allo stylesheet sul file system locale.
     * @return Una {@link Map} che associa il nome della variabile (senza i trattini iniziali) al rispettivo {@link Color}.
     */
    public static Map<String, Color> parseExternalColors(File externalCssFile) {
        if (externalCssFile == null || !externalCssFile.exists()) {
            System.err.println("[CssColorParser] Errore: File CSS esterno mancante o nullo.");
            return new HashMap<>();
        }

        try (FileInputStream fis = new FileInputStream(externalCssFile)) {
            return parseFromStream(fis);
        } catch (Exception e) {
            System.err.println("[CssColorParser] Eccezione critica durante l'analisi dello stylesheet esterno: " + externalCssFile.getAbsolutePath());
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    /**
     * Pipeline di elaborazione condivisa. Consuma un {@link InputStream} generico ed estrae le variabili tramite Regex.
     */
    private static Map<String, Color> parseFromStream(InputStream is) throws IOException {
        Map<String, Color> colors = new HashMap<>();
        String line;
        boolean inMultiLineComment = false;

        // Regex flessibile: intercetta spazi vuoti, punti e virgola opzionali, formati Hex (3-8 caratteri) e rgb/rgba
        Pattern pattern = Pattern.compile("-([a-zA-Z0-9-]+)\\s*:\\s*(#[0-9a-fA-F]{3,8}|rgba?\\([^)]+\\))\\s*;?");

        // Try-with-resources per garantire la chiusura dei lettori bufferizzati sul flusso
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // Gestione e filtraggio di sicurezza per i commenti multi-riga (/* ... */)
                if (line.contains("/*")) {
                    inMultiLineComment = true;
                }
                if (inMultiLineComment) {
                    if (line.contains("*/")) {
                        inMultiLineComment = false;
                        line = line.substring(line.indexOf("*/") + 2).trim();
                    } else {
                        continue;
                    }
                }

                // Salta righe vuote, commenti a riga singola o parentesi strutturali dei selettori
                if (line.isEmpty() || line.startsWith("*") || line.startsWith("//") || line.equals("}") || line.equals(".root {")) {
                    continue;
                }

                Matcher m = pattern.matcher(line);
                if (m.find()) {
                    String varName = m.group(1);
                    String colorVal = m.group(2);
                    try {
                        // Converte la stringa colore (Hex o web format) usando il parser nativo di JavaFX
                        colors.put(varName, Color.web(colorVal));
                    } catch (IllegalArgumentException e) {
                        System.err.println("[CssColorParser] Errore di formato web color '" + colorVal + "' nella variabile: " + varName);
                    }
                }
            }
        }
        return colors;
    }
}