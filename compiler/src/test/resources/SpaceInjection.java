import android.content.Context;
import com.contentful.vault.ModelHelper;
import com.contentful.vault.SpaceHelper;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Test$AwesomeSpace$$SpaceHelper extends SpaceHelper {
  final Map<Class<?>, ModelHelper<?>> models = new LinkedHashMap<Class<?>, ModelHelper<?>>();

  final Map<String, Class<?>> types = new LinkedHashMap<String, Class<?>>();

  public Test$AwesomeSpace$$SpaceHelper(Context context) {
    super(context, "space_c2lk", null, 1);
    models.put(Test.Model.class, new Test$Model$$ModelHelper());
    types.put("cid", Test.Model.class);
  }

  @Override
  public Map<Class<?>, ModelHelper<?>> getModels() {
    return models;
  }

  @Override
  public Map<String, Class<?>> getTypes() {
    return types;
  }
}