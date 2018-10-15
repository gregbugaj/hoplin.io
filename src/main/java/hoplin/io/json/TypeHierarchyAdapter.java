package hoplin.io.json;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Objects;

/**
 * {@link TypeHierarchyAdapter} for dealing with class hierarchy serialization
 * 
 * <pre>
 * final BiMap<String, Class<?>> types = HashBiMap.create();
 * types.put("http", HttpHealthConfig.class);
 * types.put("script", ScriptHealthConfig.class);
 * 
 * builder.registerTypeAdapter(HealthConfig.class, new TypeHierarchyAdapter<>(types));
 * </pre>
 * 
 * @param <T>
 *            base type to serialize
 */
public class TypeHierarchyAdapter<T> implements JsonSerializer<T>, JsonDeserializer<T>
{
    private static final String TYPE_PROP = "__type__";

    private final BiMap<String, Class<?>> types;

    @SuppressWarnings("unchecked")
    public TypeHierarchyAdapter(final Entry<String, Class<?>>... entries)
    {
        Objects.requireNonNull(entries);
        types = HashBiMap.create();
        Arrays.asList(entries).forEach(e -> types.put(e.getKey(), e.getValue()));
    }

    public TypeHierarchyAdapter(final BiMap<String, Class<?>> types)
    {
        Objects.requireNonNull(types);
        this.types = types;
    }

    @Override
    public JsonElement serialize(final T in, final Type typeOfT, final JsonSerializationContext context)
    {
        final String value = types.inverse().get(in.getClass());
        final JsonObject elem = (JsonObject) context.serialize(in);
        elem.addProperty(TYPE_PROP, value);
        return elem;
    }

    @Override
    public T deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)
        throws JsonParseException
    {
        final JsonObject jo = json.getAsJsonObject();
        final JsonElement te = jo.get(TYPE_PROP);
        final String sval = te.getAsString();
        final Class<?> clazz = types.get(sval);

        return context.deserialize(json, clazz);
    }
}
