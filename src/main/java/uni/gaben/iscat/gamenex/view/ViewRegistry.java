package uni.gaben.iscat.gamenex.view;

import uni.gaben.iscat.gamenex.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.gamenex.lib.interfaces.view.Drawable;
import java.util.HashMap;
import java.util.Map;

/**
 * Registry for mapping Model classes to their corresponding Renderers (View layer).
 * Lives strictly in the view package to maintain MVC separation.
 */
public class ViewRegistry {
    private static ViewRegistry instance;
    private final Map<Class<?>, Drawable<?>> renderers = new HashMap<>();

    private ViewRegistry() {}

    public static synchronized ViewRegistry getInstance() {
        if (instance == null) {
            instance = new ViewRegistry();
        }
        return instance;
    }

    public <T extends AbstractEntityModel> void register(Class<T> modelClass, Drawable<T> renderer) {
        renderers.put(modelClass, renderer);
    }

    @SuppressWarnings("unchecked")
    public <T extends AbstractEntityModel> Drawable<T> getRenderer(Class<T> modelClass) {
        return (Drawable<T>) renderers.get(modelClass);
    }
}
