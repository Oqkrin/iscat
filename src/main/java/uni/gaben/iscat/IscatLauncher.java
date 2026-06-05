package uni.gaben.iscat;

import javafx.application.Application;

/** Entry point: necessario per aggirare il controllo del manifest JavaFX. */
public class IscatLauncher {
    private IscatLauncher() {
        /* launcher */
    }

    public static void main(String [] args) {
        Application.launch(IscatApplication.class, args); }
}
