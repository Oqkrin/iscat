package uni.gaben.iscat.game;

import javax.swing.JFrame;

public class Main {
    public static void main(String[] args){
        // creiamo la finestra del gioco
        JFrame window = new JFrame();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.setTitle("ISCAT");

        GamePanel gamePanel = new GamePanel();
        window.add(gamePanel);

        window.pack(); // fa in modo che window abbia la giusta size per i sotto componenti (in questo caso gamePanel)

        window.setLocationRelativeTo(null); // con null la location viene messa al centro dello schermo
        window.setVisible(true);

        gamePanel.startGameThread();
    }
}
