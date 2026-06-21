package uni.gaben.iscat.view.editor;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.json.JSONArray;
import org.json.JSONObject;
import uni.gaben.iscat.universe.entities.parsed.*;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * Costruisce l'interfaccia utente dinamica per l'Editor delle Entità.
 * Tutti i campi, i riquadri di sezione e gli elementi degli array vengono creati qui.
 * Utilizza FieldBuilder per garantire controlli di input uniformi.
 */
public class EntityEditorUIBuilder {

    /** Callback per forzare il ridisegno completo della UI in seguito a modifiche strutturali degli array. */
    private final Runnable rebuildCallback;

    /** Inizializza il builder della UI impostando il callback di ricostruzione. */
    public EntityEditorUIBuilder(Runnable rebuildCallback) {
        this.rebuildCallback = rebuildCallback;
    }

    /**
     * Svuota e ripopola interamente i contenitori grafici forniti basandosi sui dati del modello JSON.
     * Raggruppa i campi in sezioni logiche: identità, grafica, fisica, comportamento, IA avanzata e audio.
     */
    public void buildUI(JSONObject json,
                        VBox identity,
                        VBox visuals,
                        VBox physics,
                        VBox behavioural,
                        VBox advancedAI,
                        VBox audio) {

        identity.getChildren().clear();
        visuals.getChildren().clear();
        physics.getChildren().clear();
        behavioural.getChildren().clear();
        advancedAI.getChildren().clear();
        audio.getChildren().clear();

        // Identità
        identity.getChildren().addAll(
                FieldBuilder.createStringField("Entity Key", json, "entitykey"),
                FieldBuilder.createStringField("Display Name", json, "name"),
                FieldBuilder.createStringField("Description", json, "description"),
                FieldBuilder.createComboField("Threat Level", json, "threatlevel",
                        new String[]{"NONE", "LOW", "MEDIUM", "HIGH", "APOCALYPSE"}),
                FieldBuilder.createDoubleField("Bestiary Order", json, "bestiaryorder", 0)
        );

        // Componente Grafica
        visuals.getChildren().addAll(
                FieldBuilder.createStringField("Sprite Name", json, "spritename"),
                FieldBuilder.createDoubleField("Scale", json, "scale", 1.0),
                FieldBuilder.createDoubleField("Frame W", json, "framew", 32),
                FieldBuilder.createDoubleField("Frame H", json, "frameh", 32),
                FieldBuilder.createComboField("Shape Type", json, "shapetype",
                        new String[]{"CIRCLE", "SQUARE", "POLYGON"}),
                FieldBuilder.createBooleanField("Is Boss", json, "isboss"),
                FieldBuilder.createBooleanField("Has Entrance Anim", json, "hasentranceanimation")
        );

        // Componente Fisica
        physics.getChildren().addAll(
                FieldBuilder.createDoubleField("Initial Life", json, "initlife", 100),
                FieldBuilder.createDoubleField("Mass", json, "mass", 1.0),
                FieldBuilder.createDoubleField("Max Velocity", json, "maxvelocity", 10.0),
                FieldBuilder.createDoubleField("Max Force", json, "maxforce", 30.0),
                FieldBuilder.createDoubleField("Damping", json, "lineardamping", 2.0),
                FieldBuilder.createDoubleField("Bullet Damage", json, "dannoproiettile", 4.0),
                FieldBuilder.createDoubleField("XP Reward", json, "xpreward", 10)
        );

        // Componente Comportamentale
        behavioural.getChildren().addAll(
                FieldBuilder.createDoubleField("Detection Range", json, "detectionrange", 15.0),
                FieldBuilder.createDoubleField("Combat Range", json, "combatrange", 10.0),
                FieldBuilder.createDoubleField("Preferred Range", json, "preferredrange", 10.0),
                FieldBuilder.createDoubleField("Action Cooldown (s)", json, "actioncooldowns", 1.0)
        );

        // Componente Audio
        JSONObject audioJson = json.optJSONObject("audio");
        if (audioJson == null) {
            audioJson = new JSONObject();
            json.put("audio", audioJson);
        }
        final String[] categories = {"attack", "idle", "hurt", "death", "spawn"};
        for (String cat : categories) {
            audio.getChildren().add(
                    FieldBuilder.createStringArrayField(cat.toUpperCase() + " Sounds", audioJson, cat)
            );
        }

        // IA Avanzata
        buildAdvancedAI(json, advancedAI);
    }

    // ============================================================
    //  COSTRUTTORI PER IA AVANZATA
    // ============================================================

    /** Genera la sottosezione dell'interfaccia dedicata ai parametri di IA avanzata (Steering, Rotazione, Modificatori, Abilità). */
    private void buildAdvancedAI(JSONObject json, VBox container) {
        JSONObject ai = json.optJSONObject("ai");
        if (ai == null) {
            ai = new JSONObject();
            json.put("ai", ai);
        }
        final JSONObject finalAi = ai;

        // Steering 
        VBox steerBox = createSectionBox("Steering Goal");
        JSONObject steering = finalAi.optJSONObject("steering");
        if (steering == null) {
            steering = new JSONObject();
            finalAi.put("steering", steering);
        }
        final JSONObject finalSteering = steering;
        steerBox.getChildren().addAll(
                FieldBuilder.createEnumComboField("Type", finalSteering, "type",
                        SteeringGoalIndex.values(), e -> e.jsonKey, rebuildCallback),
                FieldBuilder.createDoubleField("Max Prediction", finalSteering, "maxpredictiontime", 2.5),
                FieldBuilder.createDoubleField("Min Distance", finalSteering, "mindistance", 2.5),
                FieldBuilder.createDoubleField("Max Distance", finalSteering, "maxdistance", 10.0)
        );
        container.getChildren().add(steerBox);

        //  Rotazione 
        VBox rotBox = createSectionBox("Rotation Goal");
        JSONObject rotation = finalAi.optJSONObject("rotation");
        if (rotation == null) {
            rotation = new JSONObject();
            finalAi.put("rotation", rotation);
        }
        final JSONObject finalRotation = rotation;
        rotBox.getChildren().addAll(
                FieldBuilder.createEnumComboField("Type", finalRotation, "type",
                        RotationGoalIndex.values(), e -> e.jsonKey, rebuildCallback),
                FieldBuilder.createDoubleField("Spin Speed (rad/s)", finalRotation, "spinspeedradpersec", 0.0),
                FieldBuilder.createStringField("Target Name", finalRotation, "target")
        );
        container.getChildren().add(rotBox);

        // Modificatori
        VBox modBox = createSectionBox("Steering Modifiers");
        buildJsonArrayUI(modBox, finalAi, "modifiers",
                this::buildSingleModifierUI,
                () -> {
                    JSONObject m = new JSONObject();
                    m.put("type", "SEPARATION");
                    m.put("radius", 5.0);
                    m.put("weight", 1.0);
                    return m;
                }
        );
        container.getChildren().add(modBox);

        // Abilità
        VBox abBox = createSectionBox("Abilities");
        buildJsonArrayUI(abBox, finalAi, "abilities",
                this::buildSingleAbilityUI,
                () -> {
                    JSONObject a = new JSONObject();
                    a.put("type", "SHOOT");
                    a.put("cooldownsec", 1.0);
                    return a;
                }
        );
        container.getChildren().add(abBox);
    }

    /** Metodo di supporto per creare un contenitore VBox stilizzato munito di titolo della sezione. */
    private VBox createSectionBox(String title) {
        VBox box = new VBox(8);
        box.getStyleClass().add("editor-section-box");
        Label label = new Label(title);
        label.getStyleClass().add("editor-section-title");
        box.getChildren().add(label);
        return box;
    }

    /** Genera ricorsivamente e dinamicamente l'interfaccia per la gestione degli array JSON, includendo i tasti di aggiunta e rimozione. */
    private void buildJsonArrayUI(VBox container,
                                  JSONObject parent,
                                  String arrayKey,
                                  BiConsumer<VBox, JSONObject> itemBuilder,
                                  Supplier<JSONObject> defaultFactory) {

        JSONArray arr = parent.optJSONArray(arrayKey);
        if (arr == null) {
            arr = new JSONArray();
            parent.put(arrayKey, arr);
        }
        final JSONArray finalArr = arr;

        container.getChildren().clear();

        // Titolo della sezione
        Label titleLabel = new Label(arrayKey.substring(0, 1).toUpperCase() + arrayKey.substring(1));
        titleLabel.getStyleClass().add("editor-section-title");
        container.getChildren().add(titleLabel);

        // Elementi già esistenti
        for (int i = 0; i < finalArr.length(); i++) {
            final JSONObject item = finalArr.getJSONObject(i);
            final int index = i;

            VBox itemBox = new VBox(5);
            itemBox.getStyleClass().add("editor-array-item");

            // Intestazione con pulsante di eliminazione
            HBox header = new HBox(10);
            header.setAlignment(Pos.CENTER_LEFT);
            Label idxLabel = new Label("[" + index + "]");
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            Button deleteBtn = new Button("Delete");
            deleteBtn.setOnAction(e -> {
                finalArr.remove(index);
                rebuildCallback.run();
            });
            header.getChildren().addAll(idxLabel, spacer, deleteBtn);
            itemBox.getChildren().add(header);

            // Genera i campi dell'elemento
            itemBuilder.accept(itemBox, item);
            container.getChildren().add(itemBox);
        }

        // Pulsante di aggiunta
        Button addBtn = new Button("+ Add " + arrayKey);
        addBtn.setOnAction(e -> {
            finalArr.put(defaultFactory.get());
            rebuildCallback.run();
        });
        container.getChildren().add(addBtn);
    }

    /** Costruisce i campi di input grafici associati a un singolo modificatore di movimento (Steering Modifier). */
    private void buildSingleModifierUI(VBox box, JSONObject mod) {
        box.getChildren().addAll(
                FieldBuilder.createEnumComboField("Type", mod, "type",
                        ModifierIndex.values(), e -> e.jsonKey, rebuildCallback),
                FieldBuilder.createDoubleField("Radius", mod, "radius", 5.0),
                FieldBuilder.createDoubleField("Weight", mod, "weight", 1.0)
        );
    }

    /** Costruisce i campi specifici e i sotto-pattern condizionali per una singola abilità dell'entità. */
    private void buildSingleAbilityUI(VBox box, JSONObject ability) {
        box.getChildren().addAll(
                FieldBuilder.createEnumComboField("Type", ability, "type",
                        AbilityIndex.values(), e -> e.jsonKey, rebuildCallback),
                FieldBuilder.createDoubleField("Combat Range", ability, "combatrange", 10.0),
                FieldBuilder.createDoubleField("Cooldown (s)", ability, "cooldownsec", 1.0)
        );

        final String type = ability.optString("type", "");
        if (type.contains("shoot") || type.contains("Shoot") || type.equals(AbilityIndex.SUMMON.jsonKey)) {
            box.getChildren().addAll(
                    FieldBuilder.createStringField("Bullet Type", ability, "bullettype"),
                    FieldBuilder.createBooleanField("Aim at Target", ability, "aimattarget")
            );

            // Sotto-sezione dei Pattern di attacco
            VBox patBox = createSectionBox("Pattern");
            JSONObject pat = ability.optJSONObject("pattern");
            if (pat == null) {
                pat = new JSONObject();
                pat.put("type", PatternIndex.SINGLE_SHOT.jsonKey);
                ability.put("pattern", pat);
            }
            buildPatternUI(patBox, pat);
            box.getChildren().add(patBox);
        }

        if (type.equals(AbilityIndex.DASH.jsonKey) || type.equals(AbilityIndex.PLUNGE.jsonKey)) {
            box.getChildren().addAll(
                    FieldBuilder.createDoubleField("Dash Duration (ms)", ability, "dashdurationms", 500),
                    FieldBuilder.createDoubleField("Dash Impulse", ability, "dashimpulse", 30)
            );
        }
    }

    /** Costruisce ricorsivamente e dinamicamente i parametri geometrici e numerici associati a un pattern di fuoco/attacco. */
    private void buildPatternUI(VBox box, JSONObject pattern) {
        box.getChildren().clear();

        // Titolo della sezione
        Label title = new Label("Pattern");
        title.getStyleClass().add("editor-section-title");
        box.getChildren().add(title);

        // Selettore a tendina per il tipo di pattern
        box.getChildren().add(
                FieldBuilder.createEnumComboField("Pattern Type", pattern, "type",
                        PatternIndex.values(), e -> e.jsonKey, rebuildCallback)
        );

        final String type = pattern.optString("type", "");
        box.getChildren().add(
                FieldBuilder.createDoubleField("Count", pattern, "count", 1)
        );

        if (type.equals(PatternIndex.SPREAD.jsonKey) ||
                type.equals(PatternIndex.MULTI_DIRECTION.jsonKey) ||
                type.equals(PatternIndex.PARALLEL_LINE.jsonKey)) {
            box.getChildren().add(
                    FieldBuilder.createDoubleField("Angle Step (deg)", pattern, "anglestepdeg", 15.0)
            );
        }

        if (type.equals(PatternIndex.REPEATER.jsonKey)) {
            box.getChildren().addAll(
                    FieldBuilder.createDoubleField("Repeats", pattern, "repeats", 3),
                    FieldBuilder.createDoubleField("Interval (s)", pattern, "intervalsec", 0.1)
            );
        }

        if (type.equals(PatternIndex.MULTI_DIRECTION.jsonKey) || type.equals(PatternIndex.REPEATER.jsonKey)) {
            VBox innerBox = createSectionBox("Inner Pattern");
            JSONObject inner = pattern.optJSONObject("pattern");
            if (inner == null) {
                inner = new JSONObject();
                inner.put("type", PatternIndex.SINGLE_SHOT.jsonKey);
                pattern.put("pattern", inner);
            }
            buildPatternUI(innerBox, inner);
            box.getChildren().add(innerBox);
        }
    }
}