package uni.gaben.iscat.model.game;

public enum GameState {
    PLAYING,
    IN_PAUSE,
    IN_OPTIONS,
    GAME_OVER;

    public boolean isPaused()  { return this == IN_PAUSE || this == IN_OPTIONS; }
    public boolean isPlaying() { return this == PLAYING; }
    public boolean isGameOver(){ return this == GAME_OVER; }

    /** Transizione ESC: PLAYING<->IN_PAUSE, IN_OPTIONS->IN_PAUSE */
    public GameState onEscape() {
        return switch (this) {
            case PLAYING    -> IN_PAUSE;
            case IN_PAUSE   -> PLAYING;
            case IN_OPTIONS -> IN_PAUSE;
            case GAME_OVER  -> GAME_OVER;
        };
    }
}