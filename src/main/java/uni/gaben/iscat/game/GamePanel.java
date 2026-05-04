package uni.gaben.iscat.game;

import javax.swing.JPanel;
import java.awt.*;

public class GamePanel extends JPanel implements Runnable{
    // Impostazioni dello schermo
    final int scale = 3;
    final int screenWidth = 500;
    final int screenHeight = 500;

    int FPS = 60;
    boolean FPS_visible = true; // cambia a false per non vederli nella console

    // oggetto keylistener
    KeyHandler keyHandler = new KeyHandler();
    Thread gameThread;

    // Posizione iniziale del player
    int player_x = 100;
    int player_y = 100;
    int player_speed = 4;

    public GamePanel(){
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.black);
        this.setDoubleBuffered(true); // serve per migliorare le performance del rendering
        this.addKeyListener(keyHandler);
        this.setFocusable(true);
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    // qui dentro run() nasce il game loop, quindi il core del gioco
    // in base agli FPS diciamo al gioco di update() e repaint()
    @Override
    public void run() {
            // 1.000.000.000 in nanosencondi è un secondo
            double drawInterval = 1000000000/FPS; // 0.01666 secondi quindi disegna FPS volte al secondo
            double delta = 0;
            long lastTime = System.nanoTime();
            long currentTime;
            long timer = 0;
            int drawCount = 0;

            while (gameThread != null) {
                currentTime = System.nanoTime();
                delta += (currentTime - lastTime) / drawInterval;
                timer += (currentTime - lastTime);
                lastTime = currentTime;

                if(delta >= 1) {
                    update(); // aggiorna pos player
                    repaint(); // disegna il player
                    delta--;
                    drawCount++;
                }

                if(timer >= 1000000000 && FPS_visible){
                    System.out.print("FPS:"+drawCount);

                    // >>>[--- DA ELIMINARE ---]<<<
                    if(drawCount == 60){
                        System.out.print(" YUPPI!!! :D");
                        System.out.println(" ");
                    } else {
                        System.out.print(" NOOOOOOOO AGHAJGDHSGJADHK!!!");
                        System.out.println(" ");
                    }
                    // una volta eliminata questa porzione di codice bisogna mettere println nel primo syso degli FPS
                    // >>>[--- DA ELIMINARE ---]<<<
                    drawCount = 0;
                    timer = 0;
                }
            }
    }

    public void update() {
        if(keyHandler.upPressed) {
            player_y -= player_speed;
        }
        else if (keyHandler.downPressed) {
            player_y += player_speed;
        }
        else if (keyHandler.rightPressed) {
            player_x += player_speed;
        }
        else if (keyHandler.leftPressed) {
            player_x -= player_speed;
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        g2.setColor(Color.white);
        g2.fillRect(player_x,player_y,50,50);
        g2.dispose();
    }
}
