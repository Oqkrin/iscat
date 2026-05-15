package uni.gaben.iscat.utils;

/**
 * Utility class per gestire cooldown basati su tick.
 * Utile per abilità, attacchi, stun, e altri timer di gioco.
 * 
 * <p>Esempio d'uso:
 * <pre>
 * private Cooldown dashCooldown = new Cooldown();
 * 
 * // Quando si usa l'abilità
 * if (dashCooldown.isReady()) {
 *     performDash();
 *     dashCooldown.set(60); // 60 tick di cooldown
 * }
 * 
 * // Ogni tick
 * dashCooldown.tick();
 * </pre>
 */
public class Cooldown {
    private int ticks;
    
    /**
     * Crea un cooldown pronto (0 tick).
     */
    public Cooldown() {
        this.ticks = 0;
    }
    
    /**
     * Crea un cooldown con un valore iniziale.
     * @param initialTicks tick iniziali
     */
    public Cooldown(int initialTicks) {
        this.ticks = Math.max(0, initialTicks);
    }
    
    /**
     * Imposta il cooldown a un numero di tick.
     * @param ticks numero di tick (valori negativi vengono trattati come 0)
     */
    public void set(double ticks) {
        this.ticks = (int) Math.max(0, ticks);
    }
    
    /**
     * Decrementa il cooldown di 1 tick.
     * Non va sotto 0.
     */
    public void tick() {
        if (ticks > 0) ticks--;
    }
    
    /**
     * @return true se il cooldown è scaduto (0 tick rimanenti)
     */
    public boolean isReady() {
        return ticks == 0;
    }
    
    /**
     * @return true se il cooldown è attivo (tick rimanenti > 0)
     */
    public boolean isCoolingDown() {
        return ticks > 0;
    }
    
    /**
     * @return numero di tick rimanenti
     */
    public int remaining() {
        return ticks;
    }
    
    /**
     * Azzera il cooldown (lo rende pronto).
     */
    public void reset() {
        ticks = 0;
    }
    
    /**
     * @return percentuale di completamento (0.0 = appena iniziato, 1.0 = pronto)
     */
    public double getProgress(int maxTicks) {
        if (maxTicks <= 0) return 1.0;
        return 1.0 - ((double) ticks / maxTicks);
    }
}
