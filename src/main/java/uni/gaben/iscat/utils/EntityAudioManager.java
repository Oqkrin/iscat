package uni.gaben.iscat.utils;

import uni.gaben.iscat.universe.entities.AbstractLivingEntityModel;
import uni.gaben.iscat.utils.audio.AudioManager;

import java.util.List;
import java.util.Random;

/**
 * Helper per mappare ed eseguire gli eventi audio specifici delle entità viventi,
 * interfacciandosi con il sistema centralizzato di {@link AudioManager}.
 */
public class EntityAudioManager {

    private static final Random RANDOM = new Random();

    private EntityAudioManager() {
        /* Questa classe di utilità non deve essere istanziata */
    }

    /**
     * Riproduce un effetto sonoro associato a uno specifico evento di un'entità.
     * Cerca i file audio configurati nel record dell'entità; se la lista è vuota o assente,
     * applica un effetto sonoro di ripiego (fallback) predefinito.
     *
     * @param model     Il modello dell'entità vivente che ha scatenato l'evento.
     * @param eventType Il tipo di evento audio (es. "attack", "hurt", "death", "idle", "spawn").
     */
    public static void playEventAudio(AbstractLivingEntityModel model, String eventType) {
        if (model == null || model.getEntityRecord() == null || model.getEntityRecord().audio() == null) {
            return;
        }

        // Recupera la lista di chiavi (nomi dei file) in base al tipo di evento richiesto
        List<String> soundNames = switch (eventType.toLowerCase().trim()) {
            case "attack" -> model.getEntityRecord().audio().attack();
            case "idle"   -> model.getEntityRecord().audio().idle();
            case "hurt"   -> model.getEntityRecord().audio().hurt();
            case "death"  -> model.getEntityRecord().audio().death();
            case "spawn"  -> model.getEntityRecord().audio().spawn();
            default       -> List.of();
        };

        String chosenSFXKey;

        // Se l'array nel JSON contiene elementi, ne estrae uno casualmente (per variabilità)
        if (soundNames != null && !soundNames.isEmpty()) {
            chosenSFXKey = soundNames.get(RANDOM.nextInt(soundNames.size())).trim();
        } else {
            // Fallback: se non c'è una configurazione specifica nel JSON, usa il default globale
            chosenSFXKey = getDefaultSFXKey(eventType);
        }

        // Se non è stato trovato o configurato alcun suono, esce silenziosamente
        if (chosenSFXKey == null || chosenSFXKey.isEmpty()) {
            return;
        }

        // Pulisce l'estensione finale se presente, uniformandosi alle richieste dell'AudioManager
        if (chosenSFXKey.toLowerCase().endsWith(".wav")) {
            chosenSFXKey = chosenSFXKey.substring(0, chosenSFXKey.length() - 4);
        }

        // Invia la chiave pulita all'AudioManager per l'effettiva riproduzione
        AudioManager.getInstance().playSFX(chosenSFXKey);
    }

    /**
     * Restituisce la chiave dell'SFX di default del sistema qualora il JSON dell'entità sia sprovvisto.
     * * @param eventType Il tipo di evento audio normalizzato.
     * @return La stringa identificativa dell'SFX di fallback, oppure {@code null} se non è previsto alcun suono automatico.
     */
    private static String getDefaultSFXKey(String eventType) {
        return switch (eventType.toLowerCase().trim()) {
            case "hurt"   -> "generic_hurt";
            case "death"  -> "generic_death";
            case "attack" -> "shoot";
            default       -> null;
        };
    }
}