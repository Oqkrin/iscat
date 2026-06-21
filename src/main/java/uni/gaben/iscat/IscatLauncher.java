package uni.gaben.iscat;

import javafx.application.Application;

/**
 * Entry Point dell'applicazione.
 */
public class IscatLauncher {

    /**
     * Metodo principale che avvia il ciclo di vita dell'applicazione JavaFX delegando a IscatApplication.
     *
     * @param args Gli argomenti passati da riga di comando.
     */
    public static void main(String [] args) {
        Application.launch(IscatApplication.class, args);
    }
}