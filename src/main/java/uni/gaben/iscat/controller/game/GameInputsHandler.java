package uni.gaben.iscat.controller.game;

import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import uni.gaben.iscat.model.user.UserSettings;
import uni.gaben.iscat.utils.SessionManager;

/**
 * Gestore dell'input fisico (tastiera e mouse) grezzo per il loop di gioco.
 * Lo stato dei tasti di movimento e di fuoco viene letto in modo asincrono e continuo ad ogni frame (polling).
 * Gestisce l'attivazione della modalità rallentatore (Slow-motion), il tracciamento delle coordinate del mouse,
 * il campionamento "one-shot" del comando di pausa e implementa un sistema di tracciamento basato su timing
 * per l'attivazione dello scatto rapido tramite doppio tocco (Quick-dash).
 */
public class GameInputsHandler {

    /** Flag di stato letti ad ogni frame per determinare la direzione del movimento continuo. */
    public boolean up, down, left, right;

    /** Flag di stato per determinare se il giocatore sta attivamente sparando. */
    public boolean shooting;

    /** Trigger di attivazione dello scatto (dash) tramite pressione del relativo tasto configurato. */
    public boolean dashKeyPressed;

    /** Trigger di attivazione secondario (es. per abilità di schivata o alterazione temporale). */
    public boolean dashMousePressed;

    /** Flag che indica se l'utente sta richiedendo l'attivazione del rallentatore (Dodge/Slow-motion). */
    public boolean slowMotionRequested;

    /** Coordinate bidimensionali correnti del cursore del mouse all'interno dell'area di gioco. */
    public double mouseX, mouseY;

    /** Componente X della direzione dello scatto rapido generato da doppio tocco (-1, 0, 1). */
    private int quickDashX = 0;

    /** Componente Y della direzione dello scatto rapido generato da doppio tocco (-1, 0, 1). */
    private int quickDashY = 0;

    /** Flag per impedire la consumazione multipla del medesimo comando di scatto rapido nello stesso frame. */
    private boolean quickDashConsumed = false;

    /** Timestamp in millisecondi dell'ultima pressione per asse, impiegati per il calcolo del doppio tocco. */
    private long lastUpPress = 0;
    private long lastDownPress = 0;
    private long lastLeftPress = 0;
    private long lastRightPress = 0;

    /** Soglia temporale massima (espressa in millisecondi) entro cui registrare due tocchi consecutivi come scatto. */
    private static final long DOUBLE_TAP_THRESHOLD_MS = 200;

    /** Flag di richiesta della pausa di gioco, consumato con pattern cooperativo one-shot. */
    private boolean pauseRequested = false;

    /**
     * Registra i listener per la cattura degli eventi di pressione e rilascio tasti/mouse sulla {@link Scene} attiva.
     *
     * @param scene La scena dell'applicazione a cui agganciare l'ascolto degli input.
     */
    public void attachToScene(Scene scene) {
        scene.setOnKeyPressed(e -> handleKey(e.getCode(), true));
        scene.setOnKeyReleased(e -> handleKey(e.getCode(), false));
        scene.setOnMousePressed(e -> handleMouse(e.getButton(), true));
        scene.setOnMouseReleased(e -> handleMouse(e.getButton(), false));
    }

    /**
     * Registra i listener per il tracciamento del movimento e del trascinamento del cursore del mouse sul canvas.
     *
     * @param canvas Il nodo grafico (es. Canvas o Pane) che cattura i movimenti del mouse nell'area di gioco.
     */
    public void attachToCanvas(Node canvas) {
        canvas.setOnMouseMoved(e -> {
            mouseX = e.getX();
            mouseY = e.getY();
        });
        canvas.setOnMouseDragged(e -> {
            mouseX = e.getX();
            mouseY = e.getY();
        });
    }

    /**
     * Ripristina istantaneamente lo stato di tutti i flag di input principali ai valori di default (falso).
     * Viene invocato tipicamente in caso di perdita di focus della finestra per prevenire il blocco logico dei tasti.
     */
    public void resetInputs() {
        up = down = left = right = false;
        shooting = false;
        dashKeyPressed = false;
        dashMousePressed = false;
        slowMotionRequested = false;
        pauseRequested = false;
    }

    /**
     * Verifica e consuma la richiesta di scatto rapido (Quick-dash) rilevata nel frame corrente.
     * Calcola e restituisce un vettore di direzione bidimensionale normalizzato.
     *
     * @return Un oggetto {@link Point2D} rappresentante la direzione dello scatto,
     * oppure {@code null} se nessun comando è stato impartito o se è già stato consumato.
     */
    public Point2D consumeQuickDash() {
        if (quickDashConsumed || (quickDashX == 0 && quickDashY == 0)) {
            return null;
        }
        quickDashConsumed = true;
        double len = Math.sqrt(quickDashX * quickDashX + quickDashY * quickDashY);
        double normX = quickDashX / len;
        double normY = quickDashY / len;

        quickDashX = 0;
        quickDashY = 0;
        return new Point2D(normX, normY);
    }

    /**
     * Consuma in modalità "one-shot" la richiesta di pausa del gioco.
     * Resetta immediatamente il flag interno per evitare letture duplicate nei frame successivi.
     *
     * @return {@code true} se la pausa è stata richiesta ed è attualmente da gestire, {@code false} altrimenti.
     */
    public boolean consumePause() {
        boolean p = pauseRequested;
        pauseRequested = false;
        return p;
    }

    /**
     * Intercetta gli eventi della tastiera mappandoli sulle variabili di stato in base al profilo
     * delle impostazioni utente ({@link UserSettings}) correnti. Se non vi sono configurazioni personalizzate,
     * applica un fallback sui controlli standard (WASD / Frecce direzionali).
     *
     * @param code      Il codice {@link KeyCode} del tasto premuto o rilasciato.
     * @param isPressed {@code true} se l'evento descrive la pressione del tasto, {@code false} se descrive il rilascio.
     */
    private void handleKey(KeyCode code, boolean isPressed) {
        UserSettings settings = SessionManager.getInstance().getCurrentSettings();

        if (settings != null) {
            if (matchKey(code, settings.getWalkUp()))    up = isPressed;
            if (matchKey(code, settings.getWalkDown()))  down = isPressed;
            if (matchKey(code, settings.getWalkLeft()))  left = isPressed;
            if (matchKey(code, settings.getWalkRight())) right = isPressed;
            if (matchKey(code, settings.getAttack()))    shooting = isPressed;

            if (isPressed) {
                if (matchKey(code, settings.getDash1())) dashKeyPressed = true;
                if (matchKey(code, settings.getDash2())) dashMousePressed = true;
                if (matchKey(code, settings.getPauseGame())) pauseRequested = true;

                detectQuickDash(code);
            } else {
                if (matchKey(code, settings.getDash1())) dashKeyPressed = false;
                if (matchKey(code, settings.getDash2())) dashMousePressed = false;
            }
        } else {
            switch (code) {
                case W, UP    -> up = isPressed;
                case S, DOWN  -> down = isPressed;
                case A, LEFT  -> left = isPressed;
                case D, RIGHT -> right = isPressed;
                case Z        -> shooting = isPressed;
                case SPACE    -> { if (isPressed) dashKeyPressed = true; }
                case ESCAPE, P -> { if (isPressed) pauseRequested = true; }
                default -> {}
            }

            if (isPressed) {
                detectQuickDash(code);
            }
        }
    }

    /**
     * Intercetta i click del mouse associandoli alle azioni di gioco (fuoco, rallentatore, pausa)
     * effettuando la conversione in base alle preferenze di assegnazione comandi salvate.
     *
     * @param button    Il pulsante {@link MouseButton} che ha generato l'evento.
     * @param isPressed {@code true} se il pulsante risulta premuto, {@code false} se rilasciato.
     */
    private void handleMouse(MouseButton button, boolean isPressed) {
        UserSettings settings = SessionManager.getInstance().getCurrentSettings();

        if (settings != null) {
            if (matchMouse(button, settings.getWalkUp()))    up = isPressed;
            if (matchMouse(button, settings.getWalkDown()))  down = isPressed;
            if (matchMouse(button, settings.getWalkLeft()))  left = isPressed;
            if (matchMouse(button, settings.getWalkRight())) right = isPressed;
            if (matchMouse(button, settings.getAttack()))    shooting = isPressed;

            if (isPressed) {
                if (matchMouse(button, settings.getDash1())) dashKeyPressed = true;
                if (matchMouse(button, settings.getDash2())) {
                    slowMotionRequested = true;
                }
                if (matchMouse(button, settings.getPauseGame())) pauseRequested = true;
            } else {
                if (matchMouse(button, settings.getDash1())) dashKeyPressed = false;
                if (matchMouse(button, settings.getDash2())) slowMotionRequested = false;
            }
        } else {
            if (button == MouseButton.PRIMARY) shooting = isPressed;
            if (button == MouseButton.MIDDLE) {
                slowMotionRequested = isPressed;
            }
        }
    }

    /**
     * Algoritmo di analisi temporale per l'individuazione del doppio tocco sulle direzioni.
     * Se l'intervallo di tempo tra due pressioni della stessa direzione è inferiore alla soglia
     * definita, viene inizializzato un vettore direzionale pronto per la consumazione del Quick-dash.
     * Supporta la composizione simultanea di due assi per generare vettori diagonali.
     *
     * @param code Il codice del tasto di movimento registrato.
     */
    private void detectQuickDash(KeyCode code) {
        long now = System.currentTimeMillis();

        if (code == KeyCode.UP || code == KeyCode.W) {
            if (now - lastUpPress < DOUBLE_TAP_THRESHOLD_MS) {
                quickDashY = -1;
                quickDashConsumed = false;
            }
            lastUpPress = now;
        } else if (code == KeyCode.DOWN || code == KeyCode.S) {
            if (now - lastDownPress < DOUBLE_TAP_THRESHOLD_MS) {
                quickDashY = 1;
                quickDashConsumed = false;
            }
            lastDownPress = now;
        } else if (code == KeyCode.LEFT || code == KeyCode.A) {
            if (now - lastLeftPress < DOUBLE_TAP_THRESHOLD_MS) {
                quickDashX = -1;
                quickDashConsumed = false;
            }
            lastLeftPress = now;
        } else if (code == KeyCode.RIGHT || code == KeyCode.D) {
            if (now - lastRightPress < DOUBLE_TAP_THRESHOLD_MS) {
                quickDashX = 1;
                quickDashConsumed = false;
            }
            lastRightPress = now;
        }
    }

    /**
     * Verifica la corrispondenza tra un codice tasto e la stringa identificativa memorizzata nel database.
     *
     * @param code         Il {@link KeyCode} nativo da verificare.
     * @param settingValue La stringa di configurazione registrata nelle preferenze.
     * @return {@code true} se il tasto corrisponde all'azione impostata, {@code false} altrimenti.
     */
    private boolean matchKey(KeyCode code, String settingValue) {
        if (settingValue == null) return false;
        String dbKey = settingValue.trim().toUpperCase();
        String fxKey = code.toString().toUpperCase();

        return switch (dbKey) {
            case "UP" -> code == KeyCode.UP;
            case "DOWN" -> code == KeyCode.DOWN;
            case "LEFT" -> code == KeyCode.LEFT;
            case "RIGHT" -> code == KeyCode.RIGHT;
            case "SPACE" -> code == KeyCode.SPACE;
            default -> fxKey.equals(dbKey);
        };
    }

    /**
     * Verifica la corrispondenza tra il pulsante del mouse cliccato e il valore testuale salvato.
     *
     * @param button       Il tipo di pulsante {@link MouseButton} premuto.
     * @param settingValue La stringa di configurazione memorizzata nel database delle impostazioni.
     * @return {@code true} se il pulsante coincide con la mappatura, {@code false} altrimenti.
     */
    private boolean matchMouse(MouseButton button, String settingValue) {
        if (settingValue == null) return false;
        String val = settingValue.trim().toUpperCase();
        return switch (button) {
            case PRIMARY -> val.equals("MOUSEPRIMARY");
            case SECONDARY -> val.equals("MOUSESECONDARY");
            case MIDDLE -> val.equals("MOUSEMIDDLE");
            default -> false;
        };
    }
}