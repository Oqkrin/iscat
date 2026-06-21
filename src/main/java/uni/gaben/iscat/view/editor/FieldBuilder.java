package uni.gaben.iscat.view.editor;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

/**
 * Utility factory per la creazione di componenti di input grafici allineati in riga (HBox).
 * Sincronizza automaticamente le modifiche effettuate sulla UI con un oggetto modello JSON di destinazione.
 */
public class FieldBuilder {

    /** Crea una riga di layout contenente un'etichetta descrittiva e il rispettivo controllo JavaFX. */
    public static HBox createRow(String labelText, Control control) {
        HBox row = new HBox(8);
        row.getStyleClass().add("editor-field-row");
        row.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label(labelText);
        label.getStyleClass().add("label");
        row.getChildren().add(label);
        row.getChildren().add(control);

        // Make control grow horizontally
        if (control instanceof TextField || control instanceof ComboBox || control instanceof Spinner) {
            HBox.setHgrow(control, Priority.ALWAYS);
        }
        return row;
    }

    /** Genera una riga con un campo di testo (TextField) collegato a una proprietà String del JSON. */
    public static HBox createStringField(String label, JSONObject json, String key) {
        TextField field = new TextField(json.optString(key, ""));
        field.textProperty().addListener((obs, old, val) -> json.put(key, val));
        return createRow(label, field);
    }

    /** Genera una riga con uno Spinner numerico decimale collegato a una proprietà numerica del JSON. */
    public static HBox createDoubleField(String label, JSONObject json, String key, double def) {
        Spinner<Double> spinner = new Spinner<>(-9999.0, 9999.0, json.optDouble(key, def), 0.5);
        spinner.setEditable(true);
        spinner.valueProperty().addListener((obs, old, val) -> json.put(key, val));
        return createRow(label, spinner);
    }

    /** Genera una riga con una ComboBox di stringhe predefinite collegata a una proprietà di testo del JSON. */
    public static HBox createComboField(String label, JSONObject json, String key, String[] options) {
        ComboBox<String> combo = new ComboBox<>();
        combo.getItems().addAll(options);
        String current = json.optString(key, options[0]).toUpperCase();
        combo.getSelectionModel().select(current);
        combo.setOnAction(e -> json.put(key, combo.getSelectionModel().getSelectedItem()));
        return createRow(label, combo);
    }

    /** Genera una riga con una ComboBox tipizzata su un Enum, sincronizzando la chiave estratta nel JSON. */
    public static <E extends Enum<E>> HBox createEnumComboField(String label, JSONObject json, String key, E[] values, java.util.function.Function<E, String> keyExtractor) {
        String[] jsonKeys = Arrays.stream(values).map(keyExtractor).toArray(String[]::new);
        ComboBox<String> combo = new ComboBox<>();
        combo.getItems().addAll(jsonKeys);
        String current = json.optString(key, jsonKeys[0]);
        String matched = Arrays.stream(jsonKeys)
                .filter(k -> k.equalsIgnoreCase(current))
                .findFirst().orElse(jsonKeys[0]);
        combo.getSelectionModel().select(matched);
        combo.setOnAction(e -> json.put(key, combo.getSelectionModel().getSelectedItem()));
        return createRow(label, combo);
    }

    /** Genera una riga con una CheckBox booleana collegata a un flag del JSON. */
    public static HBox createBooleanField(String label, JSONObject json, String key) {
        CheckBox check = new CheckBox();
        check.setSelected(json.optBoolean(key, false));
        check.selectedProperty().addListener((obs, old, val) -> json.put(key, val));
        return createRow(label, check);
    }

    /** Genera un campo per array di stringhe (es. tracce audio) tokenizzando il testo inserito separato da virgole. */
    public static HBox createStringArrayField(String label, JSONObject json, String key) {
        TextField field = new TextField();
        HBox.setHgrow(field, Priority.ALWAYS);

        JSONArray arr = json.optJSONArray(key);
        if (arr != null) {
            List<String> items = new java.util.ArrayList<>();
            for (int i = 0; i < arr.length(); i++) {
                items.add(arr.getString(i));
            }
            field.setText(String.join(", ", items));
        } else {
            json.put(key, new JSONArray());
        }

        field.textProperty().addListener((obs, old, val) -> {
            JSONArray newArr = new JSONArray();
            if (!val.trim().isEmpty()) {
                String[] parts = val.split(",");
                for (String p : parts) {
                    if (!p.trim().isEmpty()) newArr.put(p.trim());
                }
            }
            json.put(key, newArr);
        });

        return createRow(label, field);
    }
}