package uni.gaben.iscat.universe.spawn.waves;

import org.json.JSONArray;
import org.json.JSONObject;
import uni.gaben.iscat.universe.entities.ThreatLevel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestore dedicato al caricamento, alla lettura e al parsing dei dati delle ondate da file JSON.
 */
public final class WaveConfigManager {

    /** Mappa la configurazione di una singola ondata caricata. */
    public record WaveConfig(ThreatLevel threatLevel, int totalEnemies) { }

    private final List<WaveConfig> loadedWaves = new ArrayList<>();

    public List<WaveConfig> getLoadedWaves() {
        return loadedWaves;
    }

    /**
     * Esegue il parsing manuale sequenziale del file risorsa JSON.
     */
    public void loadWavesFromResource(String resourcePath) {
        loadedWaves.clear();
        URL url = getClass().getResource(resourcePath);

        if (url == null) {
            System.err.println("[WAVE CONFIG] Risorsa non trovata: " + resourcePath + ". Fallback procedurale.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        try (InputStream is = url.openStream();
             BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            System.err.println("[WAVE CONFIG] Errore nella lettura del file: " + resourcePath);
            return;
        }

        try {
            JSONArray jsonArray = new JSONArray(sb.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                String threatStr = obj.getString("threat_level");
                int total = obj.getInt("total_enemies");

                ThreatLevel level = ThreatLevel.valueOf(threatStr.toUpperCase().trim());
                loadedWaves.add(new WaveConfig(level, total));
            }
            System.out.printf("[WAVE CONFIG] JSON caricato! Rilevate %d ondate custom.%n", loadedWaves.size());
        } catch (Exception e) {
            System.err.println("[WAVE CONFIG] Errore nel parsing del JSON delle ondate: " + e.getMessage());
        }
    }
}