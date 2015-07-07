package com.contentful.vaultintegration.lib.allthethings;

import com.contentful.vault.Asset;
import com.contentful.vault.ContentType;
import com.contentful.vault.Field;
import com.contentful.vault.Resource;
import java.util.List;
import java.util.Map;

@ContentType("2585ClSxW8OeuMEoIiKW4i")
public class AllTheThingsResource extends Resource {
  @Field String text;
  
  @Field Integer number;
  
  @Field Double decimal;

  @Field Boolean yesno;

  @Field String dateTime;

  @Field Map location;

  @Field AllTheThingsResource entry;

  @Field Asset asset;

  @Field Map object;

  @Field List<AllTheThingsResource> entries;

  @Field List<Asset> assets;

  @Field List<String> symbols;

  public String text() {
    return text;
  }

  public Integer number() {
    return number;
  }

  public Double decimal() {
    return decimal;
  }

  public Boolean yesno() {
    return yesno;
  }

  public String dateTime() {
    return dateTime;
  }

  public Map location() {
    return location;
  }

  public AllTheThingsResource entry() {
    return entry;
  }

  public Asset asset() {
    return asset;
  }

  public Map object() {
    return object;
  }

  public List<AllTheThingsResource> entries() {
    return entries;
  }

  public List<Asset> assets() {
    return assets;
  }

  public List<String> symbols() {
    return symbols;
  }
}