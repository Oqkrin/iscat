package uni.gaben.iscat.game.model.entities;

import java.util.ArrayList;
import java.util.List;

// GameModel.java – tiene il modello di gioco
public class GameModel {
    public Player player;
    public List<Monster> monsters = new ArrayList<>();

    public GameModel() {
        player = new Player(100, 100);
        // aggiungi mostri ecc.
    }
}