import com.contentful.vault.ModelHelper;
import com.contentful.vault.Resource;
import com.contentful.vault.SpaceHelper;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Test$AwesomeSpace$$SpaceHelper extends SpaceHelper {
  final Map<Class<?>, ModelHelper<?>> models = new LinkedHashMap<Class<?>, ModelHelper<?>>();

  final Map<String, Class<? extends Resource>> types = new LinkedHashMap<String, Class<? extends Resource>>();

  public Test$AwesomeSpace$$SpaceHelper() {
    models.put(Test.Model.class, new Test$Model$$ModelHelper());
    types.put("cid", Test.Model.class);
  }

  @Override
  public String getDatabaseName() {
    return "space_c2lk";
  }

  @Override
  public int getDatabaseVersion() {
    return 1;
  }

  @Override
  public Map<Class<?>, ModelHelper<?>> getModels() {
    return models;
  }

  @Override
  public Map<String, Class<? extends Resource>> getTypes() {
    return types;
  }

  @Override
  public String getCopyPath() {
    return null;
  }

  @Override
  public String getSpaceId() {
    return "sid";
  }
}