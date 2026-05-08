package uni.gaben.iscat.game.utils.physics;

/**
 * Direzioni di input discrete.
 * {@code dx}/{@code dy} sono +1, -1 o 0 — niente normalizzazione.
 * Il controller somma le direzioni attive in un vettore grezzo prima di passarlo alla fisica.
 */
public enum InputDirection {
    UP   ( 0, -1),
    DOWN ( 0, +1),
    LEFT (-1,  0),
    RIGHT(+1,  0);

    /** Componente orizzontale grezza: -1, 0 o +1. */
    public final int dx;
    /** Componente verticale grezza: -1, 0 o +1. */
    public final int dy;

    InputDirection(int dx, int dy) { this.dx = dx; this.dy = dy; }
}
