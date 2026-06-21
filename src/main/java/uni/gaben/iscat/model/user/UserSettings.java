package uni.gaben.iscat.model.user;

/**
 * Modello di dati che rappresenta le impostazioni di configurazione personalizzate di un utente.
 * Incapsula i parametri relativi alla mappatura dei controlli di gioco, ai livelli audio,
 * alle preferenze di visualizzazione video, ai temi cromatici dell'interfaccia e alla scala di rendering.
 */
public class UserSettings {

    /** L'identificativo univoco dell'utente associato a queste impostazioni. */
    private final int userId;

    // CONTROLLI
    /** Tasto associato al movimento verso l'alto. */
    private String walkUp;
    /** Tasto associato al movimento verso il basso. */
    private String walkDown;
    /** Tasto associato al movimento verso sinistra. */
    private String walkLeft;
    /** Tasto associato al movimento verso destra. */
    private String walkRight;
    /** Tasto associato al comando di attacco. */
    private String attack;
    /** Tasto associato al comando di scatto. */
    private String dash;
    /** Tasto associato al comando rallenta il tempo */
    private String bulletTime;
    /** Tasto associato all'apertura del menu di pausa. */
    private String pauseGame;

    // AUDIO
    /** Livello del volume generale (Master), espresso come valore decimale. */
    private double volumeMaster;
    /** Livello del volume della musica di sottofondo (BGM), espresso come valore decimale. */
    private double volumeBgm;
    /** Livello del volume degli effetti sonori (SFX), espresso come valore decimale. */
    private double volumeSfx;

    // DISPLAY
    /** Flag indicatore per la visualizzazione del contatore FPS (1 attivo, 0 disattivato). */
    private int showFps;
    /** Flag indicatore per l'attivazione della modalità schermo intero (1 attivo, 0 finestrato). */
    private int fullscreen;
    /** Flag indicatore per l'attivazione della modalità di debug (1 attivo, 0 disattivato). */
    private int debugMode;

    // TEMI SPECIALI
    /** Flag indicatore per l'attivazione della modalità chiara (1 Light Mode, 0 Dark Mode). */
    private int lightmode = 0;
    /** Flag indicatore per l'attivazione dell'effetto cromatico Rainbow (1 attivo, 0 disattivato). */
    private int rainbowMode = 0;

    // TEMI CROMATICI (HEX)
    /** Codice esadecimale stringa associato all'accento cromatico primario. */
    private String primaryTheme;
    /** Codice esadecimale stringa associato all'accento cromatico secondario. */
    private String secondaryTheme;
    /** Codice esadecimale stringa associato all'accento cromatico ternario. */
    private String tertiaryTheme;
    /** Codice esadecimale stringa associato allo sfondo dell'interfaccia. */
    private String backgroundTheme;

    // SCALA
    /** Fattore di scala moltiplicativo per il ridimensionamento della UI. */
    private double scale;

    /**
     * Costruttore completo per l'inizializzazione di tutti i parametri di configurazione utente.
     *
     * @param userId          L'identificativo univoco dell'utente.
     * @param walkUp          Tasto per il movimento verso l'alto.
     * @param walkDown        Tasto per il movimento verso il basso.
     * @param walkLeft        Tasto per il movimento verso sinistra.
     * @param walkRight       Tasto per il movimento verso destra.
     * @param attack          Tasto per l'azione di attacco.
     * @param dash            Tasto primario per lo scatto.
     * @param bulletTime      Tasto per rallentare il tempo
     * @param pauseGame       Tasto per mettere il gioco in pausa.
     * @param volumeMaster    Livello del volume generale.
     * @param volumeBgm       Livello del volume della musica.
     * @param volumeSfx       Livello del volume degli effetti.
     * @param showFps         Stato del contatore FPS (1 o 0).
     * @param fullscreen      Stato dello schermo intero (1 o 0).
     * @param debugMode       Stato della modalità debug (1 o 0).
     * @param lightmode       Stato della modalità chiara (1 o 0).
     * @param rainbowMode     Stato della modalità Rainbow (1 o 0).
     * @param primaryTheme    Esadecimale del tema primario.
     * @param secondaryTheme  Esadecimale del tema secondario.
     * @param tertiaryTheme   Esadecimale del tema ternario.
     * @param backgroundTheme Esadecimale dello sfondo.
     * @param scale           Fattore di scala della UI.
     */
    public UserSettings(int userId, String walkUp, String walkDown, String walkLeft, String walkRight,
                        String attack, String dash, String bulletTime, String pauseGame,
                        double volumeMaster, double volumeBgm, double volumeSfx,
                        int showFps, int fullscreen, int debugMode, int lightmode, int rainbowMode,
                        String primaryTheme, String secondaryTheme, String tertiaryTheme, String backgroundTheme,
                        double scale) {
        this.userId = userId;
        this.walkUp = walkUp;
        this.walkDown = walkDown;
        this.walkLeft = walkLeft;
        this.walkRight = walkRight;
        this.attack = attack;
        this.dash = dash;
        this.bulletTime = bulletTime;
        this.pauseGame = pauseGame;
        this.volumeMaster = volumeMaster;
        this.volumeBgm = volumeBgm;
        this.volumeSfx = volumeSfx;
        this.showFps = showFps;
        this.fullscreen = fullscreen;
        this.debugMode = debugMode;
        this.lightmode = lightmode;
        this.rainbowMode = rainbowMode;
        this.primaryTheme = primaryTheme;
        this.secondaryTheme = secondaryTheme;
        this.tertiaryTheme = tertiaryTheme;
        this.backgroundTheme = backgroundTheme;
        this.scale = scale;
    }

    /** @return L'identificativo univoco dell'utente proprietario del profilo impostazioni. */
    public int getUserId() { return userId; }

    /** @return La stringa identificativa del tasto associato al movimento verso l'alto. */
    public String getWalkUp() { return walkUp; }
    /** @param walkUp Il tasto da associare al movimento verso l'alto. */
    public void setWalkUp(String walkUp) { this.walkUp = walkUp; }

    /** @return La stringa identificativa del tasto associato al movimento verso il basso. */
    public String getWalkDown() { return walkDown; }
    /** @param walkDown Il tasto da associare al movimento verso il basso. */
    public void setWalkDown(String walkDown) { this.walkDown = walkDown; }

    /** @return La stringa identificativa del tasto associato al movimento verso sinistra. */
    public String getWalkLeft() { return walkLeft; }
    /** @param walkLeft Il tasto da associare al movimento verso sinistra. */
    public void setWalkLeft(String walkLeft) { this.walkLeft = walkLeft; }

    /** @return La stringa identificativa del tasto associato al movimento verso destra. */
    public String getWalkRight() { return walkRight; }
    /** @param walkRight Il tasto da associare al movimento verso destra. */
    public void setWalkRight(String walkRight) { this.walkRight = walkRight; }

    /** @return La stringa identificativa del tasto associato all'azione di attacco. */
    public String getAttack() { return attack; }
    /** @param attack Il tasto da associare all'azione di attacco. */
    public void setAttack(String attack) { this.attack = attack; }

    /** @return La stringa identificativa del tasto primario per lo scatto. */
    public String getDash() { return dash; }
    /** @param dash Il tasto primario da associare allo scatto. */
    public void setDash(String dash) { this.dash = dash; }

    /** @return La stringa identificativa del tasto secondario per lo scatto. */
    public String getBulletTime() { return bulletTime; }
    /** @param bulletTime Il tasto secondario da associare allo scatto. */
    public void setBulletTime(String bulletTime) { this.bulletTime = bulletTime; }

    /** @return La stringa identificativa del tasto per mettere in pausa il gioco. */
    public String getPauseGame() { return pauseGame; }
    /** @param pauseGame Il tasto da associare alla pausa. */
    public void setPauseGame(String pauseGame) { this.pauseGame = pauseGame; }

    /** @return Il livello del volume generale (Master). */
    public double getVolumeMaster() { return volumeMaster; }
    /** @param volumeMaster Il livello di volume master da impostare. */
    public void setVolumeMaster(double volumeMaster) { this.volumeMaster = volumeMaster; }

    /** @return Il livello del volume della musica (BGM). */
    public double getVolumeBgm() { return volumeBgm; }
    /** @param volumeBgm Il livello di volume musicale da impostare. */
    public void setVolumeBgm(double volumeBgm) { this.volumeBgm = volumeBgm; }

    /** @return Il livello del volume degli effetti sonori (SFX). */
    public double getVolumeSfx() { return volumeSfx; }
    /** @param volumeSfx Il livello di volume degli effetti sonori da impostare. */
    public void setVolumeSfx(double volumeSfx) { this.volumeSfx = volumeSfx; }

    /** @return Lo stato di visibilità del contatore dei fotogrammi (1 visibile, 0 nascosto). */
    public int getShowFps() { return showFps; }
    /** @param showFps Imposta la visibilità del contatore FPS (1 o 0). */
    public void setShowFps(int showFps) { this.showFps = showFps; }

    /** @return Lo stato della modalità schermo intero (1 attivo, 0 finestrato). */
    public int getFullscreen() { return fullscreen; }
    /** @param fullscreen Imposta lo stato di schermo intero (1 o 0). */
    public void setFullscreen(int fullscreen) { this.fullscreen = fullscreen; }

    /** @return Lo stato dei widget e dei log di debug (1 attivo, 0 disattivato). */
    public int getDebugMode() { return debugMode; }
    /** @param debugMode Imposta lo stato della modalità debug (1 o 0). */
    public void setDebugMode(int debugMode) { this.debugMode = debugMode; }

    /** @return Lo stato del flag associato alla Light Mode (1 Light, 0 Dark). */
    public int getLightmode() { return lightmode; }
    /** @param lightmode Imposta l'attivazione della modalità chiara (1 o 0). */
    public void setLightmode(int lightmode) { this.lightmode = lightmode; }

    /** @return Lo stato del flag associato all'effetto arcobaleno (1 attivo, 0 disattivato). */
    public int getRainbowMode() { return rainbowMode; }
    /** @param rainbowMode Imposta l'attivazione della Rainbow Mode (1 o 0). */
    public void setRainbowMode(int rainbowMode) { this.rainbowMode = rainbowMode; }

    /** @return La stringa esadecimale dell'accento cromatico primario. */
    public String getPrimaryTheme() { return primaryTheme; }
    /** @param primaryTheme Il codice esadecimale da salvare per il tema primario. */
    public void setPrimaryTheme(String primaryTheme) { this.primaryTheme = primaryTheme; }

    /** @return La stringa esadecimale dell'accento cromatico secondario. */
    public String getSecondaryTheme() { return secondaryTheme; }
    /** @param secondaryTheme Il codice esadecimale da salvare per il tema secondario. */
    public void setSecondaryTheme(String secondaryTheme) { this.secondaryTheme = secondaryTheme; }

    /** @return La stringa esadecimale dell'accento cromatico ternario. */
    public String getTertiaryTheme() { return tertiaryTheme; }
    /** @param tertiaryTheme Il codice esadecimale da salvare per il tema ternario. */
    public void setTertiaryTheme(String tertiaryTheme) { this.tertiaryTheme = tertiaryTheme; }

    /** @return La stringa esadecimale associata al colore dello sfondo principale. */
    public String getBackgroundTheme() { return backgroundTheme; }
    /** @param backgroundTheme Il codice esadecimale da salvare per lo sfondo. */
    public void setBackgroundTheme(String backgroundTheme) { this.backgroundTheme = backgroundTheme; }

    /** @return Il fattore di scala globale configurato per l'interfaccia utente. */
    public double getScale() { return scale; }
    /** @param scale Il fattore moltiplicativo di scala da applicare alla UI. */
    public void setScale(double scale) { this.scale = scale; }
}