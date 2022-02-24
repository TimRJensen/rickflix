package util;

import java.util.HashMap;
import java.util.Map;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;

public class Router {

  public final Map<String, Route> routes = new HashMap<>();

  private final ObjectProperty<Location> location = new SimpleObjectProperty<>(
    this,
    "location"
  );

  public final ObjectProperty<Location> locationProperty() {
    return this.location;
  }

  public final Location getLocation() {
    return this.location.get();
  }

  private HistoryManager history;

  public final void navigate(String path) {
    for (var entry : this.routes.entrySet()) {
      var keys = entry.getValue().path.split("/:");
      var match = entry.getKey();

      if (!path.contains(match)) {
        continue;
      }

      var args = path.substring(match.length()).split("/");
      var params = new HashMap<String, String>();
      var i = 0;

      while (++i < args.length) {
        params.put(keys[i], args[i]);
      }

      this.history.push(new Location(match, params));

      break;
    }
  }

  private final ChangeListener<Number> handleChange = (
    observable,
    prev,
    next
  ) -> {
    if ((int) next == -1) {
      return;
    }

    this.location.set(this.history.getLocation());
  };

  public Router(HistoryManager history) {
    this.history = history;
    this.history.indexProperty().addListener(this.handleChange);
  }
}
