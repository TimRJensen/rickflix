package util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class HistoryManagerTest {

  private HistoryManager history;

  @BeforeEach
  public void initTest() {
    this.history = new HistoryManager();
  }

  @Test
  @DisplayName("Assert that new instance has an initial .index of -1 and #size equals 0.")
  void newInstance() {
    assertEquals(-1, history.getIndex());
    assertEquals(0, history.size());
  }

  @Test
  @DisplayName("Assert that #push increases .index by 1, size by 1 and that locations match.")
  void push() {
    var location = new Location("/foo", null);

    history.push(location);
    history.push(location);
    history.push(location);
    history.push(location);

    assertEquals(3, history.getIndex());
    assertEquals(4, history.size());
    assertEquals(location, history.getLocation());
  }

  @Test
  @DisplayName("Assert that #go moves .index by n and to 0 or #size - 1 when out of bounds.")
  void go() {
    var paths = new String[] { "/a", "/b", "/c", "/d", "/e" };

    for (var path : paths) {
      history.push(new Location(path, null));
    }

    history.go(-10);

    assertEquals(0, history.getIndex());
    assertEquals("/a", history.getLocation().path);

    history.go(2);

    assertEquals(2, history.getIndex());
    assertEquals("/c", history.getLocation().path);

    history.go(-2);

    assertEquals(0, history.getIndex());
    assertEquals("/a", history.getLocation().path);

    history.go(10);

    assertEquals(history.size() - 1, history.getIndex());
    assertEquals("/e", history.getLocation().path);
  }

  @Test
  @DisplayName("Assert that #back decreases .index by 1 and to 0 when out of bounds.")
  void back() {
    var paths = new String[] { "/a", "/b", "/c", "/d", "/e" };

    for (var path : paths) {
      history.push(new Location(path, null));
    }

    history.back();

    assertEquals(3, history.getIndex());
    assertEquals("/d", history.getLocation().path);

    history.go(-10);
    history.back();

    assertEquals(0, history.getIndex());
    assertEquals("/a", history.getLocation().path);
  }

  @Test
  @DisplayName("Assert that #forward sets .index to -1 and to #size - 1 when out of bounds.")
  void forward() {
    var paths = new String[] { "/a", "/b", "/c", "/d", "/e" };

    for (var path : paths) {
      history.push(new Location(path, null));
    }

    history.back();
    history.forward();

    assertEquals(4, history.getIndex());
    assertEquals("/e", history.getLocation().path);

    history.go(10);
    history.forward();

    assertEquals(history.size() - 1, history.getIndex());
    assertEquals("/e", history.getLocation().path);
  }

  @Test
  @DisplayName("Assert that #clear sets .index to -1 and #size equals 0.")
  void clear() {
    var i = -1;

    while (++i < 3) {
      history.push(new Location("/a", null));
    }

    history.clear();

    assertEquals(-1, history.getIndex());
    assertEquals(0, history.size());
  }
}
