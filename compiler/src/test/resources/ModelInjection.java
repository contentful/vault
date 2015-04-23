package test;

import com.contentful.sqlite.FieldMeta;
import com.contentful.sqlite.ModelHelper;
import java.util.ArrayList;
import java.util.List;

final class Test$AwesomeModel$$ModelHelper implements ModelHelper<test.Test.AwesomeModel> {
  final List<FieldMeta> fields = new ArrayList<FieldMeta>();

  public Test$AwesomeModel$$ModelHelper() {
    fields.add(new FieldMeta("fText", "fText", "STRING", null, "java.lang.String"));
    fields.add(new FieldMeta("fBoolean", "fBoolean", "INT", null, "java.lang.Boolean"));
    fields.add(new FieldMeta("fInteger", "fInteger", "INT", null, "java.lang.Integer"));
    fields.add(new FieldMeta("fDouble", "fDouble", "DOUBLE", null, "java.lang.Double"));
    fields.add(new FieldMeta("fMap", "fMap", "BLOB", null, "java.util.Map"));
    fields.add(new FieldMeta("fLinkedModel", "fLinkedModel", null, "Entry", "test.Test.AwesomeModel"));
    fields.add(new FieldMeta("fLinkedAsset", "fLinkedAsset", null, "Asset", "com.contentful.sqlite.Asset"));
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
    list.add("CREATE TABLE `entry_y2lk` (`remote_id` STRING NOT NULL UNIQUE, `created_at` STRING NOT NULL, `updated_at` STRING, `fText` STRING, `fBoolean` INT, `fInteger` INT, `fDouble` DOUBLE, `fMap` BLOB);");
    return list;
  }
}