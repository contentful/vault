import android.database.Cursor;
import com.contentful.vault.Asset;
import com.contentful.vault.FieldMeta;
import com.contentful.vault.ModelHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Test$AwesomeModel$$ModelHelper extends ModelHelper<Test.AwesomeModel> {
  final List<FieldMeta> fields = new ArrayList<FieldMeta>();

  public Test$AwesomeModel$$ModelHelper() {
    fields.add(FieldMeta.builder().setId("textField").setName("textField").setSqliteType("STRING").build());
    fields.add(FieldMeta.builder().setId("booleanField").setName("booleanField").setSqliteType("INT").build());
    fields.add(FieldMeta.builder().setId("integerField").setName("integerField").setSqliteType("INT").build());
    fields.add(FieldMeta.builder().setId("doubleField").setName("doubleField").setSqliteType("DOUBLE").build());
    fields.add(FieldMeta.builder().setId("mapField").setName("mapField").setSqliteType("BLOB").build());
    fields.add(FieldMeta.builder().setId("assetLink").setName("assetLink").setLinkType("Asset").build());
    fields.add(FieldMeta.builder().setId("entryLink").setName("entryLink").setLinkType("Entry").build());
    fields.add(FieldMeta.builder().setId("arrayOfAssets").setName("arrayOfAssets").setArrayType(
        "com.contentful.vault.Asset").build());
    fields.add(FieldMeta.builder().setId("arrayOfModels").setName("arrayOfModels").setArrayType("Test.AwesomeModel").build());
    fields.add(FieldMeta.builder().setId("arrayOfSymbols").setName("arrayOfSymbols").setSqliteType(
        "BLOB").setArrayType("java.lang.String").build());
  }

  @Override
  public List<FieldMeta> getFields() {
    return fields;
  }

  @Override
  public String getTableName() {
    return "entry_y2lk";
  }

  @Override
  public List<String> getCreateStatements() {
    List<String> list = new ArrayList<String>();
    list.add("CREATE TABLE `entry_y2lk` (`remote_id` STRING NOT NULL UNIQUE, `created_at` STRING NOT NULL, `updated_at` STRING, `textField` STRING, `booleanField` INT, `integerField` INT, `doubleField` DOUBLE, `mapField` BLOB, `arrayOfSymbols` BLOB);");
    return list;
  }

  @Override
  public Test.AwesomeModel fromCursor(Cursor cursor) {
    Test.AwesomeModel result = new Test.AwesomeModel();
    result.textField = cursor.getString(3);
    result.booleanField = Integer.valueOf(1).equals(cursor.getInt(4));
    result.integerField = cursor.getInt(5);
    result.doubleField = cursor.getDouble(6);
    result.mapField = fieldFromBlob(HashMap.class, cursor, 7);
    result.arrayOfSymbols = fieldFromBlob(ArrayList.class, cursor, 8);
    return result;
  }

  @Override
  @SuppressWarnings("unchecked")
  public boolean setField(Test.AwesomeModel resource, String name, Object value) {
    if ("textField".equals(name)) {
      resource.textField = (String) value;
    }
    else if ("booleanField".equals(name)) {
      resource.booleanField = (Boolean) value;
    }
    else if ("integerField".equals(name)) {
      resource.integerField = (Integer) value;
    }
    else if ("doubleField".equals(name)) {
      resource.doubleField = (Double) value;
    }
    else if ("mapField".equals(name)) {
      resource.mapField = (Map) value;
    }
    else if ("assetLink".equals(name)) {
      resource.assetLink = (Asset) value;
    }
    else if ("entryLink".equals(name)) {
      resource.entryLink = (Test.AwesomeModel) value;
    }
    else if ("arrayOfAssets".equals(name)) {
      resource.arrayOfAssets = (List<Asset>) value;
    }
    else if ("arrayOfModels".equals(name)) {
      resource.arrayOfModels = (List<Test.AwesomeModel>) value;
    }
    else if ("arrayOfSymbols".equals(name)) {
      resource.arrayOfSymbols = (List<String>) value;
    }
    else {
      return false;
    }
    return true;
  }
}