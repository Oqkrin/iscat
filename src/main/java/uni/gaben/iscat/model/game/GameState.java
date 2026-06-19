package uni.gaben.iscat.model.game;

public enum GameState {
    PLAYING,
    IN_PAUSE,
    IN_SETTINGS,
    GAME_OVER,
    WIN;

    public boolean isPaused() {
        return this == IN_PAUSE || this == IN_SETTINGS || this == GAME_OVER || this == WIN;
    }
    public boolean isPlaying() { return this == PLAYING; }
    public boolean isGameOver(){ return this == GAME_OVER; }

    public GameState onEscape() {
        return switch (this) {
            case PLAYING    -> IN_PAUSE;
            case IN_PAUSE   -> PLAYING;
            case IN_SETTINGS -> IN_PAUSE;
            case GAME_OVER  -> GAME_OVER;
            case WIN  -> WIN;
        };
    }
}