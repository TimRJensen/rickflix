package util;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class HistoryManager {

  private final List<Location> locations = new ArrayList<>();
  private final IntegerProperty index = new SimpleIntegerProperty(
    this,
    "index",
    -1
  );

  public final IntegerProperty indexProperty() {
    return this.index;
  }

  public final int getIndex() {
    return this.index.get();
  }

  public void push(Location location) {
    this.locations.retainAll(this.locations.subList(0, this.getIndex() + 1));

    this.locations.add(location);
    this.index.set(this.getIndex() + 1);
  }

  public HistoryManager go(int index) {
    var next = this.getIndex() + index;

    if (next < 0) {
      this.index.set(0);

      return this;
    }

    if (next >= this.locations.size()) {
      this.index.set(this.locations.size() - 1);

      return this;
    }

    this.index.set(next);

    return this;
  }

  public HistoryManager back() {
    return this.go(-1);
  }

  public HistoryManager forward() {
    return this.go(1);
  }

  public Location getLocation() {
    return this.locations.get(this.getIndex());
  }

  public int size() {
    return this.locations.size();
  }

  public void clear() {
    this.locations.clear();
    this.index.set(-1);
  }
}
