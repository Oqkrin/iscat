package uni.gaben.iscat.game.input;

import uni.gaben.iscat.game.player.PlayerModel;
import uni.gaben.iscat.game.GameCanvas;

/**
 * Gestisce il wrapping del giocatore ai bordi dello schermo.
 *
 * Trajectory-based: estende la traiettoria all'indietro dal punto di uscita
 * e trova dove rientra nel rettangolo dello schermo.
 * La velocità rimane invariata — solo la posizione cambia.
 */
public class GameWrapController {

    /**
     * Applica il wrapping se il giocatore è uscito dai bordi.
     * @return true se è avvenuto un wrap (per saltare l'aggiornamento direzione)
     */
    public boolean wrap(PlayerModel p, double w, double h) {
        if (w <= 0 || h <= 0) return false;

        double half = GameCanvas.TILE_SIZE / 2.0;
        double cx   = p.getX() + half;
        double cy   = p.getY() + half;

        double vx = p.getVelocity().x;
        double vy = p.getVelocity().y;

        boolean exitRight  = cx > w;
        boolean exitLeft   = cx < 0;
        boolean exitBottom = cy > h;
        boolean exitTop    = cy < 0;

        if (!exitRight && !exitLeft && !exitBottom && !exitTop) return false;

        // Ray origin clamped to the exit edge, direction = -velocity
        double rx  = exitRight ? w : exitLeft ? 0 : cx;
        double ry  = exitBottom ? h : exitTop ? 0 : cy;
        double rdx = -vx;
        double rdy = -vy;

        double entryX, entryY;

        if (Math.abs(rdx) < 0.001 && Math.abs(rdy) < 0.001) {
            // No velocity — standard opposite-side wrap
            entryX = exitRight ? 0 : exitLeft ? w : cx;
            entryY = exitBottom ? 0 : exitTop ? h : cy;
        } else {
            double bestT = Double.MAX_VALUE;
            double bx = cx, by = cy;

            if (rdx != 0) {
                double t = (0 - rx) / rdx;
                if (t > 0.001) { double iy = ry + t * rdy; if (iy >= 0 && iy <= h && t < bestT) { bestT = t; bx = 0;  by = iy; } }
                t = (w - rx) / rdx;
                if (t > 0.001) { double iy = ry + t * rdy; if (iy >= 0 && iy <= h && t < bestT) { bestT = t; bx = w;  by = iy; } }
            }
            if (rdy != 0) {
                double t = (0 - ry) / rdy;
                if (t > 0.001) { double ix = rx + t * rdx; if (ix >= 0 && ix <= w && t < bestT) { bestT = t; bx = ix; by = 0;  } }
                t = (h - ry) / rdy;
                if (t > 0.001) { double ix = rx + t * rdx; if (ix >= 0 && ix <= w && t < bestT) { bestT = t; bx = ix; by = h;  } }
            }

            entryX = bx;
            entryY = by;
        }

        p.setX(entryX - half);
        p.setY(entryY - half);
        return true;
    }
}
