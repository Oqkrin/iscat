package uni.gaben.iscat.universe.entities.interfaces;

/**
 * Interfaccia per entità dotate di componenti grafiche bidimensionali (Sprite Rendering Capability).
 * <p>
 * Espone i metadati geometrici e le temporizzazioni necessarie ai moduli di rendering per campionare,
 * animare e orientare correttamente le sprite-sheet (o texture singole) ancorate alla controparte fisica dell'oggetto.
 * </p>
 */
public interface HasSprite {

    /**
     * @return Il percorso relativo (URI o asset path) all'interno delle risorse di gioco per caricare il file d'immagine.
     */
    String getSpritePath();

    /**
     * @return La larghezza in pixel del singolo frame di campionamento all'interno della sprite-sheet.
     */
    int getSpriteFrameWidth();

    /**
     * @return L'altezza in pixel del singolo frame di campionamento all'interno della sprite-sheet.
     */
    int getSpriteFrameHeight();

    /**
     * @return La durata temporale in secondi di permanenza su un singolo frame prima di avanzare nell'animazione.
     */
    double getFrameDuration();

    /**
     * Restituisce il disallineamento angolare intrinseco della texture rispetto all'asse di prua nominale del corpo fisico.
     * Viene sommato all'angolo del corpo rigido durante la fase di disegno per raddrizzare asset grafici orientati nativamente in modo errato.
     *
     * @return L'offset angolare visivo espresso in gradi sessagesimali (°).
     */
    double getVisualAngularOffsetDeg();

    /**
     * Determina se il motore grafico deve applicare la rotazione della trasformazione fisica alla sprite durante il rendering.
     *
     * @return {@code true} se la sprite deve orientarsi seguendo il corpo rigido, {@code false} se deve rimanere fissa (es. Billboarding o HUD di gioco).
     */
    default boolean canRotate() {
        return true;
    }
}