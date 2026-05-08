package uni.gaben.iscat.game.utils.interfaces;

/**
 * Entità che sa come disegnarsi — fornisce il proprio renderer.
 *
 * Implementare questa interfaccia elimina la necessità di instanceof in GameModel.addEntity().
 * Il renderer è stateless e può essere condiviso tra istanze dello stesso tipo.
 *
 * Esempio:
 * <pre>
 *   public class PlayerModel extends LivingEntityModel implements HasRenderer {
 *       private static final PlayerView VIEW = new PlayerView();
 *       public EntityRenderer<PlayerModel> getRenderer() { return VIEW; }
 *   }
 * </pre>
 */
public interface HasRenderer {

    /**
     * Restituisce il renderer per questa entità.
     * Il renderer deve essere stateless — non memorizzare stato per frame.
     */
    @SuppressWarnings("rawtypes")
    EntityRenderer getRenderer();
}
