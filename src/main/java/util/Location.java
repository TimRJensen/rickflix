package util;

import java.util.Map;

public class Location {

  public final String path;
  public final Map<String, String> params;

  public Location(String path, Map<String, String> params) {
    this.path = path;
    this.params = params;
  }
}
