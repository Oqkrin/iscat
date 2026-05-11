package uni.gaben.iscat.utils;

import javafx.scene.paint.Color;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility per estrarre variabili colore (CSS looked-up colors) dai fogli di stile
 * e convertirli in istanze Color di JavaFX a runtime.
 */
public class CssColorParser {

    /**
     * Analizza un file CSS e restituisce una mappa delle variabili colore trovate.
     * Supporta i formati standard es. -accent-primary: #cbcbcb; oppure rgba(0,0,0,0);
     * 
     * @param cssResourcePath il percorso del resource CSS (es. "/uni/gaben/iscat/styles/iscat-color-theme.css")
     * @return Mappa contenente i nomi delle variabili (senza il trattino iniziale) e i Color corrispondenti
     */
    public static Map<String, Color> parseColors(String cssResourcePath) {
        Map<String, Color> colors = new HashMap<>();
        try (InputStream is = CssColorParser.class.getResourceAsStream(cssResourcePath)) {
            if (is == null) {
                System.err.println("CssColorParser: Impossibile trovare il file " + cssResourcePath);
                return colors;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            // Match pattern per variabili CSS custom che contengono colori: -nome-var: #hex | rgba(...)
            Pattern pattern = Pattern.compile("-([a-zA-Z0-9-]+)\\s*:\\s*(#[0-9a-fA-F]{3,8}|rgba?\\([^)]+\\))");
            while ((line = reader.readLine()) != null) {
                Matcher m = pattern.matcher(line);
                if (m.find()) {
                    String varName = m.group(1);
                    String colorVal = m.group(2);
                    try {
                        colors.put(varName, Color.web(colorVal));
                    } catch (IllegalArgumentException e) {
                        System.err.println("CssColorParser: Impossibile fare il parse del colore " + colorVal + " per la var " + varName);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return colors;
    }
}
