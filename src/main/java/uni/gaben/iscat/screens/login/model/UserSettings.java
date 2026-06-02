package uni.gaben.iscat.screens.login.model;

public class UserSettings {
    private final int userId;
    private String walkUp;
    private String walkDown;
    private String walkLeft;
    private String walkRight;
    private String attack;
    private String dash1;
    private String dash2;
    private String pauseGame;

    private double volumeMaster;
    private double volumeBgm;
    private double volumeSfx;

    private int showFps = 0;
    private int fullscreen = 0;

    public UserSettings(int userId, String walkUp, String walkDown, String walkLeft, String walkRight,
                        String attack, String dash1, String dash2, String pauseGame,
                        double volumeMaster, double volumeBgm, double volumeSfx,
                        int showFps, int fullscreen) {
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
}