package uni.gaben.iscat.controller.components;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import uni.gaben.iscat.universe.entities.EntityRecord;
import uni.gaben.iscat.universe.entities.ThreatLevel;

/**
 * Controller per la gestione della scheda informativa dell'interfaccia utente (InfoCard).
 * Si occupa di visualizzare la lore, le statistiche e i dettagli extra di entità,
 * navi o nemici all'interno di diverse aree di testo non modificabili.
 */
public class InfoCardController {

    /** Area di testo dedicata alla descrizione e al livello di minaccia dell'entità. */
    @FXML private TextArea loreArea;

    /** Area di testo dedicata alle statistiche fisiche e vitali dell'entità. */
    @FXML private TextArea statsArea;

    /** Area di testo dedicata alle specifiche di sistema (Player) o ai parametri IA (Enemy). */
    @FXML private TextArea extraArea;

    /**
     * Inizializza il componente configurando le aree di testo.
     * Imposta le aree come non modificabili, attiva il wrapping del testo, disabilita
     * il focus automatico e impedisce la selezione visiva del testo.
     */
    @FXML
    public void initialize() {
        for (TextArea area : new TextArea[]{loreArea, statsArea, extraArea}) {
            area.setEditable(false);
            area.setWrapText(true);
            area.setFocusTraversable(false);
            area.selectionProperty().addListener((obs, old, newSel) -> {
                if (newSel != null && newSel.getLength() > 0) area.deselect();
            });
        }
    }

    /**
     * Aggiorna la scheda informativa con testo generico strutturato in intestazione e contenuto.
     * Utilizzato come fallback per mantenere la compatibilità con stringhe grezze.
     *
     * @param header  L'intestazione o titolo da mostrare.
     * @param content Il testo principale da visualizzare nell'area lore.
     */
    public void updateInfo(String header, String content) {
        loreArea.setText(header + "\n\n" + content);
        statsArea.setText("N/A");
        extraArea.setText("N/A");
    }

    /**
     * Popola e aggiorna simultaneamente le tre sezioni informative (Lore, Stats, Extra)
     * estraendo e formattando i dati statici e dinamici da un singolo record di entità.
     *
     * @param record Il record immutabile contenente i dati dell'entità da mostrare,
     * può essere {@code null}.
     */
    public void updateEntityInfo(EntityRecord record) {
        if (record == null) {
            loreArea.setText("No data");
            statsArea.setText("No data");
            extraArea.setText("No data");
            return;
        }

        // ---- Lore (Description) ----
        String description = record.description() != null ? record.description() : "No description available.";
        ThreatLevel threat = record.threatLevel() != null ? record.threatLevel() : ThreatLevel.NONE;
        String threatDisplay = switch (threat) {
            case NONE -> "[No Threat]";
            case LOW -> "[Low Risk]";
            case NORMAL -> "[Normal Risk]";
            case HIGH -> "[High Risk]";
            case EXTREME -> "[Extreme Risk]";
            case APOCALYPSE -> "[APOCALYPSE RISK]";
        };
        loreArea.setText("⚠️ Threat Level: " + threatDisplay + "\n\n" + description);

        // ---- Stats ----
        boolean isPlayer = record.player() != null || record.entityKey().toLowerCase().contains("player");
        String hpLabel = isPlayer ? "Hull Integrity" : "Health";
        String velLabel = isPlayer ? "Maneuver Speed" : "Max Speed";
        String frictionLabel = isPlayer ? "Friction Coefficient" : "Linear Damping";
        String massLabel = isPlayer ? "Structural Mass" : "Mass";
        String forceLabel = isPlayer ? "Thrust Power" : "Max Force";

        String statsText = String.format("""
                %s (Index #%03d)
                ❤ %s: %.0f HP
                ⚡ %s: %.1f m/s
                ✨ XP Reward: %d
                📐 Scale: %.1fx
                ⚓ %s: %.1f
                ⚙ %s: %.1f kg
                💪 %s: %.1f N
                """,
                isPlayer ? "SHIP STATS" : "ENEMY STATS",
                record.bestiaryOrder() != null ? record.bestiaryOrder() : 0,
                hpLabel, record.initLife(),
                velLabel, record.maxVelocity(),
                record.xpReward(),
                record.scale(),
                frictionLabel, record.linearDamping(),
                massLabel, record.mass(),
                forceLabel, record.maxForce()
        );
        statsArea.setText(statsText);

        // ---- Extra ----
        String extraText;
        if (isPlayer) {
            double cooldown = record.actionCooldownSec();
            double dashImpulse = 0, dashCooldown = 0;
            if (record.player() != null) {
                dashImpulse = record.player().dashImpulse();
                dashCooldown = record.player().dashCooldownSec();
                if (record.player().baseCooldownSec() > 0) {
                    cooldown = record.player().baseCooldownSec();
                }
            }
            extraText = String.format("""
                    SYSTEM SPECS
                    🔢 Index: #%03d
                    ⏱ Base Fire Cooldown: %.2f sec
                    💨 Dash Impulse: %.1f N/s
                    ⏱ Dash Recharge: %.2f sec
                    🆔 Internal ID: %s
                    """,
                    record.bestiaryOrder() != null ? record.bestiaryOrder() : 0,
                    cooldown, dashImpulse, dashCooldown, record.entityKey()
            );
        } else {
            double cooldown = record.actionCooldownSec() > 0 ? record.actionCooldownSec() : record.actionCooldownSec() / 1000.0;
            extraText = String.format("""
                    EXTRA INFO
                    🔢 Index: #%03d
                    ☠️ Threat Class: %s
                    👁 Detection Range: %.1f units
                    ⚔ Combat Range: %.1f units
                    🎯 Preferred Range: %.1f units
                    ⏱ Action Cooldown: %.1f s
                    🆔 ID: %s
                    """,
                    record.bestiaryOrder() != null ? record.bestiaryOrder() : 0,
                    threat.getDisplayName(),
                    record.detectionRange(), record.combatRange(), record.preferredRange(),
                    cooldown, record.entityKey()
            );
        }
        extraArea.setText(extraText);
    }
}