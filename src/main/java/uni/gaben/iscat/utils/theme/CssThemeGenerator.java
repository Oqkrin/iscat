package uni.gaben.iscat.utils.theme;

import java.io.*;
import java.util.List;

/**
 * Generatore dinamico di fogli di stile CSS per la gestione dei temi dell'interfaccia utente.
 * Legge un file CSS di base dal classpath e genera un file temporaneo sovrascrivendo
 * al volo le variabili cromatiche (CSS Custom Properties) iniettando la palette dell'utente.
 */
public final class CssThemeGenerator {

    private CssThemeGenerator() {
        /* Questa classe di utilità non deve essere istanziata */
    }

    /**
     * Crea un file CSS temporaneo iniettando i colori della palette esadecimale fornita
     * all'interno del foglio di stile di base.
     *
     * @param baseResourcePath Il percorso della risorsa CSS base nel classpath (es. "/styles/base.css").
     * @param hexColors        Lista di stringhe contenenti i codici colore esadecimali (es. ["#FF0000", "#00FF00"]).
     * @return Un'istanza di {@link File} puntante al foglio di stile temporaneo generato, oppure {@code null} in caso di errore.
     */
    public static File createDynamicStylesheet(String baseResourcePath, List<String> hexColors) {
        // Estrazione sicura dei colori con valori di fallback standard (Default ISCAT Palette)
        String p  = (!hexColors.isEmpty()) ? hexColors.get(0) : "#ff0000";
        String s  = (hexColors.size() > 1) ? hexColors.get(1) : "#ff6200";
        String t  = (hexColors.size() > 2) ? hexColors.get(2) : "#ffa600";
        String bg = (hexColors.size() > 3) ? hexColors.get(3) : "#010203";

        try {
            InputStream is = CssThemeGenerator.class.getResourceAsStream(baseResourcePath);
            if (is == null) {
                throw new FileNotFoundException("Impossibile trovare il CSS di base nel classpath: " + baseResourcePath);
            }

            // Allocazione del file CSS temporaneo nel sistema operativo (verrà rimosso alla chiusura dell'applicazione)
            File tempCss = File.createTempFile("iscat-dynamic-theme-", ".css");
            tempCss.deleteOnExit();

            // Gestione robusta dei flussi di lettura e scrittura tramite try-with-resources
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                 BufferedWriter writer = new BufferedWriter(new FileWriter(tempCss))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();

                    if (trimmed.startsWith("-accent-primary:")) {
                        writer.write("    -accent-primary: " + p + ";\n");
                    } else if (trimmed.startsWith("-accent-secondary:")) {
                        writer.write("    -accent-secondary: " + s + ";\n");
                    } else if (trimmed.startsWith("-accent-tertiary:")) {
                        writer.write("    -accent-tertiary: " + t + ";\n");
                    } else if (trimmed.startsWith("-bg-primary:")) {
                        writer.write("    -bg-primary: " + bg + ";\n");
                    } else {
                        writer.write(line + "\n");
                    }
                }
            }

            return tempCss;

        } catch (IOException e) {
            System.err.println("[CssThemeGenerator] Errore critico durante l'iniezione dinamica del tema CSS: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}