package uni.gaben.iscat.universe.effects;

import uni.gaben.iscat.universe.UniverseSettings;
import uni.gaben.iscat.universe.entities.AbstractPhysicalEntityModel;
import uni.gaben.iscat.universe.entities.EntityRecordBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Modello di gestione e generazione del campo stellato di sfondo (Starfield).
 * <p>
 * Estende {@link AbstractPhysicalEntityModel} integrando una struttura dati sequenziale per
 * la memorizzazione di oggetti {@link Star}. Fornisce algoritmi di popolamento procedurale
 * basati sulla densità superficiale complessiva dell'area di gioco.
 * </p>
 * <p>
 * La distribuzione del raggio delle stelle applica una curva di ponderazione quadratica
 * per favorire la proliferazione di stelle di piccole dimensioni (sfondo profondo) rispetto
 * a corpi stellari di magnitudo maggiore.
 * </p>
 */
public class Starfield extends AbstractPhysicalEntityModel {

    /** Elenco interno delle istanze geometriche delle stelle aggregate nel campo visivo. */
    private final List<Star> stars = new ArrayList<>();

    /**
     * Costruisce un nuovo oggetto Starfield ancorato a coordinate spaziali specifiche.
     * Inizializza la superclasse fisica passando un record di entità vuoto predefinito.
     *
     * @param x Coordinata d'origine X del campo stellato.
     * @param y Coordinata d'origine Y del campo stellato.
     */
    public Starfield(double x, double y) {
        super(x, y, new EntityRecordBuilder().build());
    }

    /** @return La lista contenente tutte le stelle correntemente allocate nello Starfield. */
    public List<Star> getStars() {
        return stars;
    }

    /**
     * Svuota completamente la collezione interna delle stelle rimovendole dalla memoria.
     */
    public void clear() {
        stars.clear();
    }

    /**
     * Inserisce una singola istanza di stella all'interno del sistema di tracciamento.
     *
     * @param star L'oggetto {@link Star} da aggiungere.
     */
    public void addStar(Star star) {
        stars.add(star);
    }

    /**
     * Rigenera proceduralmente il campo stellato all'interno di un perimetro bidimensionale definito.
     * <p>
     * Il numero complessivo di elementi generati viene determinato matematicamente moltiplicando
     * l'area geometrica per il coefficiente di densità globale definito in {@link UniverseSettings#STAR_DENSITY}:
     * </p>
     * <p>
     * $$\text{Count} = \text{width} \cdot \text{height} \cdot \text{STAR\_DENSITY}$$
     * </p>
     * <p>
     * Per ciascun nodo viene calcolata una coordinata pseudocasuale e una dimensione scalare calibrata
     * mediante la legge quadratica:
     * </p>
     * <p>
     * $$\text{Size} = \text{STAR\_MIN\_SIZE} + r^2 \cdot \text{STAR\_MAX\_SIZE\_ADD}$$
     * </p>
     * dove $r \in [0, 1)$ rappresenta il fattore di campionamento casuale.
     *
     * @param width  La larghezza massima dell'area limite in pixel mondo.
     * @param height L'altezza massima dell'area limite in pixel mondo.
     */
    public void generate(double width, double height) {
        Random rand = new Random();
        clear();
        int starCount = (int) (width * height * UniverseSettings.STAR_DENSITY);

        for (int i = 0; i < starCount; i++) {
            double x = rand.nextDouble() * Math.max(1, width);
            double y = rand.nextDouble() * Math.max(1, height);
            double r = rand.nextDouble();
            // Distribuzione quadratica non lineare
            double size = UniverseSettings.STAR_MIN_SIZE + r * r * UniverseSettings.STAR_MAX_SIZE_ADD;
            addStar(new Star(x, y, size));
        }
    }
}