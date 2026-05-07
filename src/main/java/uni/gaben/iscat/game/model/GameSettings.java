package uni.gaben.iscat.game.model;

import uni.gaben.iscat.utils.Interpolator;

/**
 * Configurazione completa del gioco.
 * Tutti i valori numerici, costanti e impostazioni sono qui.
 * 
 * <p>Organizzato in classi nidificate per categoria.
 * Nessun "magic number" nel codice - tutto qui!
 */
public final class GameSettings {
    
    private GameSettings() {} // Classe utility
    
    // ============================================
    // GIOCATORE
    // ============================================
    
    public static final class Giocatore {
        private Giocatore() {}
        
        // === Fisica ===
        /** Massa del giocatore (kg unità di gioco) */
        public static final double MASSA = 1.0;
        
        /** Attrito normale (0-1, più basso = più attrito) */
        public static final double ATTRITO = 0.92;
        
        /** Velocità massima normale (px/tick) */
        public static final double VELOCITA_MAX = 9.0;
        
        /** Soglia dead-zone per fermare micro-movimenti */
        public static final double ZONA_MORTA = 0.01;
        
        // === Movimento ===
        /** Forza di spinta per movimento */
        public static final double FORZA_SPINTA = 4.0;
        
        // === Dash ===
        /** Impulso istantaneo del dash */
        public static final double IMPULSO_SCATTO = 15.0;
        
        /** Attrito ridotto durante dash */
        public static final double ATTRITO_SCATTO = 0.98;
        
        /** Velocità massima durante dash */
        public static final double VELOCITA_MAX_SCATTO = 20.0;
        
        /** Durata fase dash (tick) */
        public static final int DURATA_SCATTO_TICK = 15;
        
        /** Cooldown dash (tick) */
        public static final int COOLDOWN_SCATTO_TICK = 60;
        
        // === Combattimento ===
        /** HP massimo */
        public static final int HP_MASSIMO = 100;
        
        /** HP iniziale */
        public static final int HP_INIZIALE = 100;
        
        /** Durata invulnerabilità dopo danno (tick) */
        public static final int DURATA_INVULNERABILITA = 60;
        
        // === Collisione ===
        /** Raggio collisione (px) */
        public static final double RAGGIO_COLLISIONE = 24.0;
        
        /** Dimensione sprite (px) */
        public static final double DIMENSIONE_SPRITE = 64.0;
    }
    
    // ============================================
    // NEMICI
    // ============================================
    
    public static final class Bomber {
        private Bomber() {}
        
        // === Fisica ===
        /** Fattore massa rispetto al giocatore */
        public static final double FATTORE_MASSA = 8.0;
        
        /** Attrito */
        public static final double ATTRITO = 0.95;
        
        /** Fattore velocità massima rispetto al giocatore */
        public static final double FATTORE_VELOCITA_MAX = 0.8;
        
        // === AI ===
        /** Lunghezza trail posizioni giocatore */
        public static final int LUNGHEZZA_TRAIL = 120;
        
        /** Ritardo trail (tick indietro da seguire) */
        public static final int RITARDO_TRAIL = 40;
        
        /** Velocità inseguimento */
        public static final double VELOCITA_INSEGUIMENTO = 80.0;
        
        /** Distanza minima per inseguire */
        public static final double DISTANZA_MIN_INSEGUIMENTO = 10.0;
        
        /** Fattore smoothing rotazione (0-1) */
        public static final double SMOOTHING_ROTAZIONE = 0.12;
        
        // === Collisione ===
        /** Forza rimbalzo collisione */
        public static final double FORZA_RIMBALZO = 300.0;
        
        /** Durata stordimento dopo collisione (tick) */
        public static final int DURATA_STORDIMENTO = 30;
        
        /** Cooldown collisione per evitare ripetizioni (tick) */
        public static final int COOLDOWN_COLLISIONE = 10;
        
        /** Raggio collisione (px) */
        public static final double RAGGIO_COLLISIONE = 24.0;
        
        /** Dimensione sprite (px) */
        public static final double DIMENSIONE_SPRITE = 64.0;
        
        // === Combattimento ===
        /** HP */
        public static final int HP = 100;
        
        /** Valore in punti quando ucciso */
        public static final int VALORE_PUNTI = 100;
    }
    
    // ============================================
    // MONDO
    // ============================================
    
    public static final class Mondo {
        private Mondo() {}
        
        /** Costante gravitazionale */
        public static final double COSTANTE_GRAVITAZIONALE = 100.0;
        
        /** Larghezza mondo (px) - 0 = infinito */
        public static final double LARGHEZZA = 0;
        
        /** Altezza mondo (px) - 0 = infinito */
        public static final double ALTEZZA = 0;

        /**
         * Coefficiente di restituzione per collisioni (0=anelastica, 1=elastica perfetta).
         * Usato da CollisionPhysics.resolveElasticCollision().
         */
        public static final double COEFFICIENTE_RESTITUZIONE = 0.7;
    }
    
    // ============================================
    // LOOP E FISICA
    // ============================================

    public static final class Loop {
        private Loop() {}

        /** Delta-time fisso passato al mondo fisico ogni tick. */
        public static final double DT = 1.0;

        /** Fattore lerp per l'accelerazione della spinta. Più basso = più immediato. */
        public static final double LERP_SPINTA = 0.18;

        /** Curva usata per la spinta del giocatore. */
        public static final Interpolator.Preset EASING_SPINTA = Interpolator.Preset.EASE_OUT;

        /** Impulso visivo alle stelle al dodge (frazione di IMPULSO_SCATTO). */
        public static final double FATTORE_IMPULSO_STELLE = 0.4;
    }

    // ============================================
    // VISUALE
    // ============================================
    
    public static final class Visuale {
        private Visuale() {}
        
        // === Tile ===
        /** Dimensione tile base (px) */
        public static final double DIMENSIONE_TILE = 64.0;
        
        /** Offset rotazione sprite: 90° se punta a nord, 0° se punta a est. */
        public static final double OFFSET_NORD_SPRITE = 90.0;

        // === Stelle ===
        /** Numero stelle sfondo */
        public static final int NUMERO_STELLE = 200;
        
        /** Dimensione minima stella (px) */
        public static final double DIMENSIONE_STELLA_MIN = 1.0;
        
        /** Dimensione massima stella (px) */
        public static final double DIMENSIONE_STELLA_MAX = 3.0;
        
        /** Potenza distribuzione dimensioni (bias verso piccole) */
        public static final double POTENZA_DIMENSIONE_STELLA = 2.5;
        
        // === Parallasse ===
        /** Fattore lerp parallasse (0-1, più basso = più lento) */
        public static final double LERP_STELLE = 0.05;
        
        /** Preset easing parallasse */
        public static Interpolator.Preset EASING_STELLE = Interpolator.Preset.LINEAR;
        
        // === FPS ===
        /** Mostra contatore FPS */
        public static boolean MOSTRA_FPS = false;
    }
    
    // ============================================
    // AUDIO
    // ============================================
    
    public static final class Audio {
        private Audio() {}
        
        /** Volume musica di sottofondo (0.0 - 1.0) */
        public static double VOLUME_BGM = 0.5;
        
        /** Volume effetti sonori (0.0 - 1.0) */
        public static double VOLUME_SFX = 0.7;
    }
    
    // ============================================
    // INPUT
    // ============================================
    
    public static final class Input {
        private Input() {}
        
        /** Sensibilità mouse */
        public static double SENSIBILITA_MOUSE = 1.0;
        
        /** Dead-zone controller analogico */
        public static double DEAD_ZONE_CONTROLLER = 0.15;
    }
    
    // ============================================
    // EFFETTI
    // ============================================
    
    public static final class Effetti {
        private Effetti() {}
        
        /** Abilita screenshake */
        public static boolean SCREENSHAKE_ABILITATO = true;
        
        /** Intensità screenshake (px) */
        public static double INTENSITA_SCREENSHAKE = 5.0;
        
        /** Durata screenshake (tick) */
        public static int DURATA_SCREENSHAKE = 10;
        
        /** Abilita particelle */
        public static boolean PARTICELLE_ABILITATE = true;
        
        /** Numero massimo particelle simultanee */
        public static int MAX_PARTICELLE = 500;
    }
    
    // ============================================
    // DEBUG
    // ============================================
    
    public static final class Debug {
        private Debug() {}
        
        /** Modalità debug attiva */
        public static boolean MODALITA_DEBUG = false;
        
        /** Mostra collision boxes */
        public static boolean MOSTRA_COLLISION_BOXES = false;
        
        /** Mostra vettori velocità */
        public static boolean MOSTRA_VETTORI_VELOCITA = false;
        
        /** Mostra trail AI */
        public static boolean MOSTRA_TRAIL_AI = false;
        
        /** Invulnerabilità giocatore */
        public static boolean GIOCATORE_INVULNERABILE = false;
    }
    
    // ============================================
    // PERFORMANCE
    // ============================================
    
    public static final class Performance {
        private Performance() {}
        
        /** Target FPS */
        public static final int TARGET_FPS = 60;
        
        /** Tick rate fisica (Hz) */
        public static final int TICK_RATE_FISICA = 60;
        
        /** Distanza massima rendering entità (px, 0 = infinito) */
        public static double DISTANZA_MAX_RENDERING = 2000.0;
        
        /** Distanza massima aggiornamento AI (px, 0 = infinito) */
        public static double DISTANZA_MAX_AI = 1500.0;
    }
    
    // ============================================
    // COMPATIBILITÀ CON VECCHIO CODICE
    // ============================================
    
    /** @deprecated Usa {@link Giocatore#MASSA} */
    @Deprecated public static final double MASSA_GIOCATORE = Giocatore.MASSA;
    
    /** @deprecated Usa {@link Giocatore#ATTRITO} */
    @Deprecated public static final double ATTRITO = Giocatore.ATTRITO;
    
    /** @deprecated Usa {@link Giocatore#VELOCITA_MAX} */
    @Deprecated public static final double VELOCITA_MAX = Giocatore.VELOCITA_MAX;
    
    /** @deprecated Usa {@link Giocatore#ATTRITO_SCATTO} */
    @Deprecated public static final double ATTRITO_SCATTO = Giocatore.ATTRITO_SCATTO;
    
    /** @deprecated Usa {@link Giocatore#VELOCITA_MAX_SCATTO} */
    @Deprecated public static final double VELOCITA_MAX_SCATTO = Giocatore.VELOCITA_MAX_SCATTO;
    
    /** @deprecated Usa {@link Giocatore#FORZA_SPINTA} */
    @Deprecated public static final double FORZA_SPINTA = Giocatore.FORZA_SPINTA;
    
    /** @deprecated Usa {@link Giocatore#IMPULSO_SCATTO} */
    @Deprecated public static final double IMPULSO_SCATTO = Giocatore.IMPULSO_SCATTO;
    
    /** @deprecated Usa {@link Giocatore#DURATA_SCATTO_TICK} */
    @Deprecated public static final int DURATA_SCATTO_TICK = Giocatore.DURATA_SCATTO_TICK;
    
    /** @deprecated Usa {@link Giocatore#COOLDOWN_SCATTO_TICK} */
    @Deprecated public static final int COOLDOWN_SCATTO_TICK = Giocatore.COOLDOWN_SCATTO_TICK;
    
    /** @deprecated Usa {@link Giocatore#RAGGIO_COLLISIONE} */
    @Deprecated public static final double RAGGIO_COLLISIONE = Giocatore.RAGGIO_COLLISIONE;
    
    /** @deprecated Usa {@link Visuale#DIMENSIONE_TILE} */
    @Deprecated public static final double DIMENSIONE_TILE = Visuale.DIMENSIONE_TILE;
    
    /** @deprecated Usa {@link Visuale#NUMERO_STELLE} */
    @Deprecated public static final int NUMERO_STELLE = Visuale.NUMERO_STELLE;
    
    /** @deprecated Usa {@link Visuale#DIMENSIONE_STELLA_MIN} */
    @Deprecated public static final double DIMENSIONE_STELLA_MIN = Visuale.DIMENSIONE_STELLA_MIN;
    
    /** @deprecated Usa {@link Visuale#DIMENSIONE_STELLA_MAX} */
    @Deprecated public static final double DIMENSIONE_STELLA_MAX = Visuale.DIMENSIONE_STELLA_MAX;
    
    /** @deprecated Usa {@link Visuale#POTENZA_DIMENSIONE_STELLA} */
    @Deprecated public static final double STELLA_SIZE_POWER = Visuale.POTENZA_DIMENSIONE_STELLA;
    
    /** @deprecated Usa {@link Visuale#LERP_STELLE} */
    @Deprecated public static final double LERP_STELLE = Visuale.LERP_STELLE;
    
    /** @deprecated Usa {@link Visuale#EASING_STELLE} */
    @Deprecated public static Interpolator.Preset EASING_STELLE = Visuale.EASING_STELLE;
    
    /** @deprecated Usa {@link Visuale#MOSTRA_FPS} */
    @Deprecated public static boolean SHOW_FPS = Visuale.MOSTRA_FPS;
    
    /** @deprecated Usa {@link Audio#VOLUME_BGM} */
    @Deprecated public static double BGM_VOLUME = Audio.VOLUME_BGM;
    
    /** @deprecated Usa {@link Audio#VOLUME_SFX} */
    @Deprecated public static double SFX_VOLUME = Audio.VOLUME_SFX;
    
    /** @deprecated Usa {@link Input#SENSIBILITA_MOUSE} */
    @Deprecated public static double SENSITIVITY = Input.SENSIBILITA_MOUSE;
    
    /** @deprecated Usa {@link Effetti#SCREENSHAKE_ABILITATO} */
    @Deprecated public static boolean SCREENSHAKE_ENABLED = Effetti.SCREENSHAKE_ABILITATO;
    
    /** @deprecated Usa {@link Bomber#FATTORE_MASSA} */
    @Deprecated public static final double BOMBER_MASSA_FACTOR = Bomber.FATTORE_MASSA;
    
    /** @deprecated Usa {@link Bomber#ATTRITO} */
    @Deprecated public static final double BOMBER_ATTRITO = Bomber.ATTRITO;
    
    /** @deprecated Usa {@link Bomber#FATTORE_VELOCITA_MAX} */
    @Deprecated public static final double BOMBER_VELOCITA_MAX_FACTOR = Bomber.FATTORE_VELOCITA_MAX;
    
    /** @deprecated Usa {@link Bomber#LUNGHEZZA_TRAIL} */
    @Deprecated public static final int BOMBER_TRAIL_LENGTH = Bomber.LUNGHEZZA_TRAIL;
    
    /** @deprecated Usa {@link Bomber#RITARDO_TRAIL} */
    @Deprecated public static final int BOMBER_TRAIL_DELAY = Bomber.RITARDO_TRAIL;
    
    /** @deprecated Usa {@link Bomber#VELOCITA_INSEGUIMENTO} */
    @Deprecated public static final double BOMBER_FOLLOW_SPEED = Bomber.VELOCITA_INSEGUIMENTO;
    
    /** @deprecated Usa {@link Bomber#DISTANZA_MIN_INSEGUIMENTO} */
    @Deprecated public static final double BOMBER_MIN_FOLLOW_DIST = Bomber.DISTANZA_MIN_INSEGUIMENTO;
    
    /** @deprecated Usa {@link Bomber#FORZA_RIMBALZO} */
    @Deprecated public static final double BOMBER_BOUNCE_FORCE = Bomber.FORZA_RIMBALZO;
    
    /** @deprecated Usa {@link Bomber#DURATA_STORDIMENTO} */
    @Deprecated public static final int BOMBER_COLLISION_STUN_TICKS = Bomber.DURATA_STORDIMENTO;

    // --- Flat aliases for GameController / GameCanvas ---

    /** @see Loop#DT */
    public static final double DT = Loop.DT;
    /** @see Loop#LERP_SPINTA */
    public static final double LERP_SPINTA = Loop.LERP_SPINTA;
    /** @see Loop#EASING_SPINTA */
    public static final Interpolator.Preset EASING_SPINTA = Loop.EASING_SPINTA;
    /** @see Loop#FATTORE_IMPULSO_STELLE */
    public static final double FATTORE_IMPULSO_STELLE = Loop.FATTORE_IMPULSO_STELLE;
    /** @see Visuale#OFFSET_NORD_SPRITE */
    public static final double OFFSET_NORD_SPRITE = Visuale.OFFSET_NORD_SPRITE;
    /** @see Visuale#DIMENSIONE_TILE */
    public static final double DIMENSIONE_TILE_D = Visuale.DIMENSIONE_TILE;

    /** @see Mondo#COEFFICIENTE_RESTITUZIONE */
    public static final double COLLISION_RESTITUTION = Mondo.COEFFICIENTE_RESTITUZIONE;
}
