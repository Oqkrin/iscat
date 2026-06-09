package uni.gaben.iscat.utils;

import uni.gaben.iscat.universe.entity.GenericEntityModel;
import java.util.List;
import java.util.Random;

/**
 * Helper per mappare ed eseguire gli eventi audio specifici delle entità generiche
 * interfacciandosi con l'AudioManager centralizzato di ISCAT.
 */
public class EnemyAudioManager {

    private static final Random RANDOM = new Random();

    /**
     * Riproduce un effetto sonoro per l'entità in base all'evento richiesto.
     * Cerca i nomi nel JSON; se non trova nulla, applica un fallback di default.
     */
    public static void playEventAudio(GenericEntityModel model, String eventType) {
        if (model == null || model.getSettings() == null || model.getSettings().audio == null) return;

        // Recupera la lista di chiavi (nomi dei file) dal tipo di evento
        List<String> soundNames = switch (eventType.toLowerCase().trim()) {
            case "attack" -> model.getSettings().audio.attack;
            case "idle"   -> model.getSettings().audio.idle;
            case "hurt"   -> model.getSettings().audio.hurt;
            case "death"  -> model.getSettings().audio.death;
            case "spawn"  -> model.getSettings().audio.spawn;
            default       -> List.of();
        };

        String chosenSFXKey = null;

        // Se ci sono elementi nel JSON, ne estrae uno a caso
        if (soundNames != null && !soundNames.isEmpty()) {
            chosenSFXKey = soundNames.get(RANDOM.nextInt(soundNames.size())).trim();
        } else {
            // Fallback: Se l'array nel JSON è vuoto, usa un audio di default globale
            chosenSFXKey = getDefaultSFXKey(eventType);
        }

        // Se non c'è nessun suono configurato e nessun default, usciamo silenziosamente
        if (chosenSFXKey == null || chosenSFXKey.isEmpty()) return;

        if (chosenSFXKey.toLowerCase().endsWith(".wav")) {
            chosenSFXKey = chosenSFXKey.substring(0, chosenSFXKey.length() - 4);
        }

        // Invia la chiave pulita all'AudioManager esistente
        AudioManager.getInstance().playSFX(chosenSFXKey);
    }

    /**
     * Restituisce la chiave dell'SFX di default se il JSON non specifica nulla.
     * Ritorna null se per quell'evento non si desidera alcun suono automatico.
     */
    private static String getDefaultSFXKey(String eventType) {
        return switch (eventType.toLowerCase().trim()) {
            case "hurt"  -> "generic_hit";
            case "death" -> "generic_death";
            default      -> null;                };
    }
}