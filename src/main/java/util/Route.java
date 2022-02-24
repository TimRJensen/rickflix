package util;

import javafx.beans.NamedArg;

public class Route {

  public final String path;
  public final String component;

  public Route(
    @NamedArg("path") String path,
    @NamedArg("component") String component
  ) {
    this.path = path;
    this.component = component;
  }
}
