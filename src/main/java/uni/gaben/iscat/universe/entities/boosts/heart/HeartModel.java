package uni.gaben.iscat.universe.entities.boosts.heart;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import uni.gaben.iscat.universe.entities.AbstractLivingEntityModel;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseCollisionLayers;
import uni.gaben.iscat.universe.entities.parsed.EntityRecordBuilder;
import uni.gaben.iscat.universe.entities.interfaces.HasSprite;

/**
 * Modello fisico e grafico dell'entità Cuore / Consumabile (Heart Entity Model).
 * <p>
 * Rappresenta l'oggetto collezionabile nel mondo di gioco. Estende {@link AbstractLivingEntityModel}
 * e implementa {@link HasSprite} per interfacciarsi nativamente con il sottosistema di rendering bidimensionale a sprite foglio (Sprite Sheet).
 * </p>
 * <p>
 * <b>Configurazione Fisica:</b> Imposta la propria fixture come sensore (Sensor Mode). Questo permette
 * al motore di generare eventi di contatto senza applicare risposte di rimbalzo elastico o arresto cinematico.
 * </p>
 */
public class HeartModel extends AbstractLivingEntityModel implements HasSprite {

    /**
     * Costruisce il modello del cuore posizionandolo alle coordinate fornite.
     * Configura la geometria circolare di collisione, i filtri di livello e blocca l'asse rotazionale.
     *
     * @param x Coordinata X iniziale nel mondo (espressa in metri).
     * @param y Coordinata Y iniziale nel mondo (espressa in metri).
     */
    public HeartModel(double x, double y) {
        super(x, y, new EntityRecordBuilder().build());

        // Conversione del raggio da pixel a metri del mondo reale e creazione della fixture circolare
        BodyFixture fixture = addFixture(Geometry.createCircle(UU.pxToM(HeartSettings.RAGGIO_COLLISIONE_PX)));

        // Assegna il filtro BOOST per la categorizzazione selettiva nei calcoli di contatto di dyn4j
        fixture.setFilter(UniverseCollisionLayers.BOOST_FILTER);

        // Attivando lo stato di sensore, l'entità registra il trigger di sovrapposizione ma è permeabile ai corpi rigidi
        fixture.setSensor(true);

        // Impedisce alle forze esterne o agli impatti tangenziali di far ruotare lo sprite su se stesso
        this.setMass(MassType.FIXED_ANGULAR_VELOCITY);
    }

    /**
     * Forza la velocità terminale asintotica dell'oggetto a un valore fisso ($10\text{ m/s}$).
     */
    @Override
    public double getTerminalVelocity() {
        return 10.0;
    }

    /**
     * @return Il percorso della risorsa (URI/Path) dello Sprite Sheet all'interno delle cartelle degli asset.
     */
    @Override
    public String getSpritePath() {
        return HeartSettings.sprite;
    }

    /**
     * @return La larghezza in pixel di un singolo frame di animazione all'interno dello Sprite Sheet.
     */
    @Override
    public int getSpriteFrameWidth() {
        return HeartSettings.DIM_SPRITE;
    }

    /**
     * Calcola la durata temporale di persistenza a schermo di un singolo frame dello sprite.
     * * @return Tempo espresso in secondi basato sulla frequenza di clock dell'universo ($Tick \times 6$).
     */
    @Override
    public double getFrameDuration() {
        return UU.UNIVERSE_TICK * 6.0;
    }

    /**
     * @return L'altezza in pixel di un singolo frame di animazione all'interno dello Sprite Sheet.
     */
    @Override
    public int getSpriteFrameHeight() {
        return HeartSettings.DIM_SPRITE;
    }

    /**
     * Restituisce il disallineamento angolare visivo introdotto in fase di rendering.
     * * @return Correzione fissa di orientamento pari a **90°**.
     */
    @Override
    public double getVisualAngularOffsetDeg() {
        return 90.0;
    }

    /**
     * Definisce se l'entità è immune ad alterazioni di stato esterne o modificatori ambientali.
     * * @return {@code false} (il consumabile può subire traslazioni o effetti logici).
     */
    @Override
    public boolean isInalterable() {
        return false;
    }
}