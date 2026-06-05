package uni.gaben.iscat.model.user;

public class UserSettings {
    private final int userId;

    // Controls
    private String walkUp;
    private String walkDown;
    private String walkLeft;
    private String walkRight;
    private String attack;
    private String dash1;
    private String dash2;
    private String pauseGame;

    // Audio
    private double volumeMaster;
    private double volumeBgm;
    private double volumeSfx;

    // Display
    private int showFps;
    private int fullscreen;
    private int debugMode;

    // Special Themes
    private int lightmode = 0;
    private int rainbowMode = 0;

    // Themes
    private String primaryTheme;
    private String secondaryTheme;
    private String tertiaryTheme;
    private String backgroundTheme;

    // Scale
    private double scale;

    public UserSettings(int userId, String walkUp, String walkDown, String walkLeft, String walkRight,
                        String attack, String dash1, String dash2, String pauseGame,
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
        this.dash1 = dash1;
        this.dash2 = dash2;
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

    public int getUserId() { return userId; }

    public String getWalkUp() { return walkUp; }
    public void setWalkUp(String walkUp) { this.walkUp = walkUp; }

    public String getWalkDown() { return walkDown; }
    public void setWalkDown(String walkDown) { this.walkDown = walkDown; }

    public String getWalkLeft() { return walkLeft; }
    public void setWalkLeft(String walkLeft) { this.walkLeft = walkLeft; }

    public String getWalkRight() { return walkRight; }
    public void setWalkRight(String walkRight) { this.walkRight = walkRight; }

    public String getAttack() { return attack; }
    public void setAttack(String attack) { this.attack = attack; }

    public String getDash1() { return dash1; }
    public void setDash1(String dash1) { this.dash1 = dash1; }

    public String getDash2() { return dash2; }
    public void setDash2(String dash2) { this.dash2 = dash2; }

    public String getPauseGame() { return pauseGame; }
    public void setPauseGame(String pauseGame) { this.pauseGame = pauseGame; }

    public double getVolumeMaster() { return volumeMaster; }
    public void setVolumeMaster(double volumeMaster) { this.volumeMaster = volumeMaster; }

    public double getVolumeBgm() { return volumeBgm; }
    public void setVolumeBgm(double volumeBgm) { this.volumeBgm = volumeBgm; }

    public double getVolumeSfx() { return volumeSfx; }
    public void setVolumeSfx(double volumeSfx) { this.volumeSfx = volumeSfx; }

    public int getShowFps() { return showFps; }
    public void setShowFps(int showFps) { this.showFps = showFps; }

    public int getFullscreen() { return fullscreen; }
    public void setFullscreen(int fullscreen) { this.fullscreen = fullscreen; }

    public int getDebugMode() { return debugMode; }
    public void setDebugMode(int debugMode) { this.debugMode = debugMode; }

    public int getLightmode() { return lightmode; }
    public void setLightmode(int lightmode) { this.lightmode = lightmode; }

    public int getRainbowMode() { return rainbowMode; }
    public void setRainbowMode(int rainbowMode) { this.rainbowMode = rainbowMode; }

    public String getPrimaryTheme() { return primaryTheme; }
    public void setPrimaryTheme(String primaryTheme) { this.primaryTheme = primaryTheme; }

    public String getSecondaryTheme() { return secondaryTheme; }
    public void setSecondaryTheme(String secondaryTheme) { this.secondaryTheme = secondaryTheme; }

    public String getTertiaryTheme() { return tertiaryTheme; }
    public void setTertiaryTheme(String tertiaryTheme) { this.tertiaryTheme = tertiaryTheme; }

    public String getBackgroundTheme() { return backgroundTheme; }
    public void setBackgroundTheme(String backgroundTheme) { this.backgroundTheme = backgroundTheme; }

    public double getScale() { return scale; }
    public void setScale(double scale) { this.scale = scale; }
}