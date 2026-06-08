package uni.gaben.iscat.model.game;

public enum GameState {
    PLAYING,
    IN_PAUSE,
    IN_OPTIONS,
    GAME_OVER,
    WIN;

    public boolean isPaused() {
        return this == IN_PAUSE || this == IN_OPTIONS || this == GAME_OVER || this == WIN;
    }
    public boolean isPlaying() { return this == PLAYING; }
    public boolean isGameOver(){ return this == GAME_OVER; }

    /** ESC transition: PLAYING ↔ IN_PAUSE, IN_OPTIONS → IN_PAUSE. */
    public GameState onEscape() {
        return switch (this) {
            case PLAYING    -> IN_PAUSE;
            case IN_PAUSE   -> PLAYING;
            case IN_OPTIONS -> IN_PAUSE;
            case GAME_OVER  -> GAME_OVER;
            case WIN  -> WIN;
        };
    }
}