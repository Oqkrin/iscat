package uni.gaben.iscat.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import uni.gaben.iscat.universe.entity.EntityRecord;

public class InfoCardController {

    public enum InfoMode {
        DESCRIPTION, STATS, EXTRA
    }

    @FXML private Label rightCardHeader;
    @FXML private TextArea description;

    @FXML
    public void initialize() {
        description.setEditable(false);
        description.setWrapText(true);
    }

    /**
     * Metodo generico per impostare del testo libero (es. per messaggi di blocco o fallback)
     */
    public void updateInfo(String header, String content) {
        rightCardHeader.setText(header.toUpperCase());
        description.setText(content);
    }

    /**
     * Genera autonomamente il testo formattato estraendo i dati dal JSON (EntityRecord)
     * Funziona sia per i Player che per gli Enemies in base all'InfoMode selezionato.
     * * @param mode Modalità di visualizzazione scelta (DESCRIPTION, STATS, EXTRA)
     * @param record Il record dell'entità letto dal JSON
     * @param killCount Il numero di uccisioni (usato solo per gli enemies, metti 0 per i player)
     */
    public void updateEntityInfo(InfoMode mode, EntityRecord record, int killCount) {
        if (record == null) {
            updateInfo("N/A", "Nessun dato disponibile.");
            return;
        }

        // Verifica centralizzata per capire se è un Player o un Enemy
        boolean isPlayer = record.player() != null || record.entityKey().toLowerCase().contains("player");

        switch (mode) {
            case DESCRIPTION -> {
                updateInfo("DESCRIPTION", record.description());
            }
            case STATS -> {
                // Scegliamo l'intestazione appropriata
                String title = isPlayer ? "STATISTICHE DELLA NAVE" : "STATISTICHE DI BASE";
                String hpLabel = isPlayer ? "Integrità Scafo" : "Punti Vita";
                String velLabel = isPlayer ? "Velocità di Manovra" : "Velocità Massima";
                String frictionLabel = isPlayer ? "Coefficiente Attrito" : "Attrito Lineare";
                String massLabel = isPlayer ? "Massa Strutturale" : "Massa";
                String forceLabel = isPlayer ? "Spinta Propulsori" : "Forza Massima";

                String statsText = String.format("""
                    %s
                    
                    ❤ %s: %.0f HP
                    ⚡ %s: %.1f m/s
                    ✨ Ricompensa Esperienza: %d XP
                    📐 Scala Grafica: %.1fx
                    ⚓ %s: %.1f
                    ⚙ %s: %.1f kg
                    💪 %s: %.1f N
                    """,
                        title,
                        hpLabel, record.initLife(),
                        velLabel, record.maxVelocity(),
                        record.xpReward(),
                        record.scale(),
                        frictionLabel, record.linearDamping(),
                        massLabel, record.mass(),
                        forceLabel, record.maxForce()
                );
                updateInfo("STATS", statsText);
            }
            case EXTRA -> {
                if (isPlayer) {
                    double cooldownSparo = record.actionCooldownSec();
                    double dashImpulse = 0;
                    double dashCooldown = 0;

                    if (record.player() != null) {
                        dashImpulse = record.player().dashImpulse();
                        dashCooldown = record.player().dashCooldownSec();
                        if (record.player().baseCooldownSec() > 0) {
                            cooldownSparo = record.player().baseCooldownSec();
                        }
                    }

                    String extraText = String.format("""
                        SPECIFICHE DI SISTEMA
                        
                        ⏱ Cooldown Fuoco Base: %.2f sec
                        💨 Impulso Propulsione (Dash): %.1f N/s
                        ⏱ Ricarica Scatto (Dash): %.2f sec
                        🆔 ID Interno Risorsa: %s
                        """,
                            cooldownSparo, dashImpulse, dashCooldown, record.entityKey()
                    );
                    updateInfo("EXTRA INFO", extraText);
                } else {
                    double cooldownSeconds = (record.actionCooldownSec() > 0) ? record.actionCooldownSec() : (record.actionCooldownSec() / 1000.0);

                    String extraText = String.format("""
                        INFORMAZIONI EXTRA
                        
                        👁 Raggio di Avvistamento: %.1f unità
                        ⚔ Raggio di Combattimento: %.1f unità
                        🎯 Raggio Preferito: %.1f unità
                        ⏱ Cooldown Azione: %.1f secondi
                        🆔 ID : %s
                        📊 Totale Uccisi: %d
                        """,
                            record.detectionRange(), record.combatRange(), record.preferredRange(),
                            cooldownSeconds, record.entityKey(), killCount
                    );
                    updateInfo("EXTRA INFO", extraText);
                }
            }
        }
    }
}