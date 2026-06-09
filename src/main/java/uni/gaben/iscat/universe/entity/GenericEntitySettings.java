package uni.gaben.iscat.universe.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO specifico per il caricamento da JSON.
 * Estende PhysicalEntitySettings, ereditando automaticamente tutti i parametri fisici,
 * di movimento, di range IA e le ricompense (XP), evitando duplicazioni di memoria.
 */
public class GenericEntitySettings extends PhysicalEntitySettings {

    // 1. CAMPI IDENTITÀ (Specifici di questa sottoclasse)
    public String entityKey = "";
    public String name = "";
    public String description = "";

    // 2. PROPRIETÀ VISIVE / ANIMAZIONE (Specifici di questa sottoclasse)
    public String spritePath = ""; // Mappato dinamicamente da 'SpriteName' nel factory
    public int frameW = 32;
    public int frameH = 32;

    // 3. ADATTAMENTO PARAMETRI JSON VS CLASSE MADRE
    // Nota: 'shapeType', 'scale', 'initLife', 'mass', 'maxVelocity', 'detectionRange', ecc.
    // NON SONO SCRITTI QUI perché sono già ereditati da PhysicalEntitySettings.

    /** * Il JSON fornisce il cooldown in secondi (es. 800), mentre la classe madre
     * espone 'actionCooldownMS'. Teniamo questo campo per catturare il dato del JSON.
     */
    public double actionCooldownMS = 0.0;

    // 4. STRUTTURA AUDIO (Specifica di questa sottoclasse)
    public AudioProfile audio = new AudioProfile();

    // Boss logic
    public boolean isBoss;
    public boolean hasEntranceAnimation;
    public int[] animationFrames;

    /**
     * Sottoclasse interna per mappare l'oggetto 'audio' e le sue liste di file Wave/MP3.
     */
    public static class AudioProfile {
        public List<String> attack = new ArrayList<>();
        public List<String> idle = new ArrayList<>();
        public List<String> hurt = new ArrayList<>();
        public List<String> death = new ArrayList<>();
        public List<String> spawn = new ArrayList<>();
    }
}