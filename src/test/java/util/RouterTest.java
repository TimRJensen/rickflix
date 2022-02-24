package util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class RouterTest {

  private Router router;
  private HistoryManager history;

  @BeforeEach
  void initTests() {
    history = new HistoryManager();
    router = new Router(history);
  }

  @Test
  @DisplayName("Assert that new instance has an initial .location of null.")
  void newInstance() {
    assertNull(router.getLocation());
  }

  @Test
  @DisplayName("Assert that #navigate matches .location.path.")
  void navigate() {
    router.routes.put("/foo", new Route("/foo", "nothing.fxml"));
    router.navigate("/foo");

    assertEquals("/foo", router.getLocation().path);

    router.routes.put("/bar", new Route("/bar", "nothing.fxml"));
    router.navigate("/bar");

    assertEquals("/bar", router.getLocation().path);
  }

  @Test
  @DisplayName("Assert that #navigate matches .location.params or null")
  void navigateWithParams() {
    router.routes.put("/foo", new Route("/foo/:a/:b", "nothing.fxml"));
    router.routes.put("/bar", new Route("/bar/:c/:d", "nothing.fxml"));

    router.navigate("/foo/lorem/ipsum");

    assertEquals("lorem", router.getLocation().params.get("a"));
    assertEquals("ipsum", router.getLocation().params.get("b"));

    router.navigate("/bar/lorem/ipsum");

    assertEquals("lorem", router.getLocation().params.get("c"));
    assertEquals("ipsum", router.getLocation().params.get("d"));

    router.navigate("/foo");

    assertNull(router.getLocation().params.get("a"));
    assertNull(router.getLocation().params.get("b"));
  }

  @Test
  @DisplayName("Assert that #navigate does not navigate when no route exists.")
  void navigateWithNoRoute() {
    router.routes.put("/foo", new Route("/foo", "nothing.fxml"));
    router.navigate("/foo");

    var location = router.getLocation();

    router.navigate("/bar");

    assertEquals(location, router.getLocation());
  }

  @Test
  @DisplayName("Assert that .history.index listens to #handleChange and that .location matches .history#getLocation")
  void handleChange() {
    router.routes.put("/foo", new Route("/foo", "nothing.fxml"));
    router.routes.put("/bar", new Route("/bar", "nothing.fxml"));
    router.routes.put("/baz", new Route("/baz", "nothing.fxml"));
    router.routes.put("/qux", new Route("/qux", "nothing.fxml"));

    router.navigate("/foo");
    router.navigate("/bar");
    router.navigate("/baz");
    router.navigate("/qux");

    history.go(-2);

    assertEquals("/bar", router.getLocation().path);
    assertEquals(history.getLocation(), router.getLocation());

    history.go(-10);

    assertEquals("/foo", router.getLocation().path);
    assertEquals(history.getLocation(), router.getLocation());

    history.forward();

    assertEquals("/bar", router.getLocation().path);
    assertEquals(history.getLocation(), router.getLocation());

    history.back();

    assertEquals("/foo", router.getLocation().path);
    assertEquals(history.getLocation(), router.getLocation());

    history.go(2);

    assertEquals("/baz", router.getLocation().path);
    assertEquals(history.getLocation(), router.getLocation());

    history.go(10);

    assertEquals("/qux", router.getLocation().path);
    assertEquals(history.getLocation(), router.getLocation());
  }

  @Test
  @DisplayName("Assert that .history#clear does not set .location")
  void handleChangeOnClear() {
    router.routes.put("/foo", new Route("/foo", "nothing.fxml"));
    router.navigate("/foo");

    var location = router.getLocation();

    history.clear();

    assertEquals(location, router.getLocation());
  }
}
