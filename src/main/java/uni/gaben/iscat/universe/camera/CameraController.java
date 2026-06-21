package uni.gaben.iscat.universe.camera;

/**
 * Controller logico responsabile dell'aggiornamento cinematico del {@link CameraModel}.
 * <p>
 * Coordina l'allineamento della telecamera calcolando la posizione ideale del mirino basandosi
 * sull'interpolazione tra l'entità target (giocatore) e il cursore del mouse (Look-Ahead/Mouse Influence).
 * Gestisce lo snap iniziale per eliminare artefatti visivi di traslazione all'avvio e integra
 * le equazioni fisiche delle molle orizzontali e verticali basandosi sul Delta Time corrente.
 * </p>
 */
public class CameraController {

    /**
     * Aggiorna lo stato fisico e la posizione della telecamera per inseguire il target configurato.
     * <p>
     * L'algoritmo esegue i seguenti passaggi strutturali:
     * <ol>
     * <li>Normalizza il fattore di zoom corrente forzandolo a un valore minimo di sicurezza pari a 0.1.</li>
     * <li>Calcola un vettore di offset direzionale (Look-Ahead) pesato al 15% della distanza tra il giocatore e il cursore del mouse.</li>
     * <li>Applica un vincolo geometrico di clamping dinamico sull'offset basato sul livello di zoom attuale ($\frac{150}{\text{zoom}}$).</li>
     * <li>Imposta le nuove coordinate di destinazione sulle molle ed esegue, se richiesto (primo frame valido), lo snap istantaneo della posizione.</li>
     * <li>Invia il comando di integrazione temporale alle rispettive componenti fisiche {@link uni.gaben.iscat.utils.Spring}.</li>
     * </ol>
     * </p>
     *
     * @param model        Il modello della telecamera da aggiornare (non deve essere {@code null}).
     * @param targetWorldX La coordinata X d'origine dell'entità target (es. il centro del giocatore) in pixel mondo.
     * @param targetWorldY La coordinata Y d'origine dell'entità target in pixel mondo.
     * @param viewW        La larghezza corrente del canvas espressa in pixel schermo (deve essere maggiore di 0 per lo snap).
     * @param viewH        L'altezza corrente del canvas espressa in pixel schermo (deve essere maggiore di 0 per lo snap).
     * @param mouseWorldX  La coordinata X attuale del puntatore del mouse convertita in coordinate mondo.
     * @param mouseWorldY  La coordinata Y attuale del puntatore del mouse convertita in coordinate mondo.
     * @param dt           Il passo temporale (Delta Time) espresso in frazioni di secondo, utilizzato per l'integrazione delle molle.
     */
    public void update(CameraModel model, double targetWorldX, double targetWorldY,
                       double viewW, double viewH, double mouseWorldX, double mouseWorldY, double dt) {

        // Se lo zoom scende sotto lo 0.1, lo costringiamo a un valore minimo sicuro.
        // Usiamo setActualZoom visto che setZoom non esiste nel tuo modello.
        double zoom = model.getZoom();
        if (zoom < 0.1) {
            zoom = 0.1;
            model.setActualZoom(0.1);
        }

        // 1. Calculate Look-Ahead (toward mouse)
        double offsetX = (mouseWorldX - targetWorldX) * 0.15;
        double offsetY = (mouseWorldY - targetWorldY) * 0.15;

        // 2. Clamp it
        double maxOffset = 150 / zoom;
        offsetX = Math.clamp(offsetX, -maxOffset, maxOffset);
        offsetY = Math.clamp(offsetY, -maxOffset, maxOffset);

        // 3. SET TARGET AS WORLD CENTER
        double targetCentreX = targetWorldX + offsetX;
        double targetCentreY = targetWorldY + offsetY;

        model.getSpringX().setTarget(targetCentreX);
        model.getSpringY().setTarget(targetCentreY);

        // Snap on first frame
        if (!model.isSnapped() && viewW > 0 && viewH > 0) {
            model.getSpringX().setPosition(targetCentreX);
            model.getSpringY().setPosition(targetCentreY);
            model.setSnapped(true);
        }

        model.getSpringX().update(dt);
        model.getSpringY().update(dt);
    }
}