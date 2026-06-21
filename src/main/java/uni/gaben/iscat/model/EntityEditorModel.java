package uni.gaben.iscat.model;

import org.json.JSONObject;

public class EntityEditorModel {
    private JSONObject currentJson;
    private String originPath;

    public EntityEditorModel() {
        this.currentJson = new JSONObject();
        this.originPath = null;
    }

    public JSONObject getCurrentJson() { return currentJson; }
    public void setCurrentJson(JSONObject currentJson) { this.currentJson = currentJson; }

    public String getOriginPath() { return originPath; }
    public void setOriginPath(String originPath) { this.originPath = originPath; }
}