package components.navigator_controls;

import static org.junit.jupiter.api.Assertions.assertEquals;

import app.App;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import util.Route;

@ExtendWith(ApplicationExtension.class)
public class NavigatorControlsTest extends App {

  private NavigatorControls navigatorControls;

  @Start
  public void start(Stage stage) {
    this.router.routes.put("/", new Route("/", null));
    this.router.routes.put("/foo", new Route("/foo", null));
    this.router.routes.put("/bar", new Route("/bar", null));

    this.navigatorControls = new NavigatorControls();

    var root = new StackPane();

    root.getChildren().add(this.navigatorControls);
    root
      .getStylesheets()
      .add(this.getClass().getResource("/css/index.css").toString());

    stage.setScene(new Scene(root, 1000, 400));
    stage.show();
    this.router.navigate("/");
  }

  @Test
  @DisplayName("Assert that new NavigatorControls children are disabled")
  void handleClick(FxRobot robot) {
    assertEquals(true, robot.lookup("#backControl").query().isDisable());
    assertEquals(true, robot.lookup("#forwardControl").query().isDisable());
  }

  @Test
  @DisplayName("Assert that new #handleBack decreases history")
  void handleBack(FxRobot robot) {
    this.router.navigate("/foo");

    var backControl = robot.lookup("#backControl").query();
    var forwardControl = robot.lookup("#forwardControl").query();

    assertEquals(false, backControl.isDisable());
    assertEquals(true, forwardControl.isDisable());

    this.router.navigate("/bar");

    robot.clickOn(backControl);

    assertEquals(false, backControl.isDisable());
    assertEquals(false, forwardControl.isDisable());

    robot.clickOn(backControl);

    assertEquals(true, backControl.isDisable());
    assertEquals(false, forwardControl.isDisable());
  }

  @Test
  @DisplayName("Assert that new #handleForward increases history")
  void handleForward(FxRobot robot) {
    this.router.navigate("/foo");
    this.router.navigate("/bar");

    var backControl = robot.lookup("#backControl").query();
    var forwardControl = robot.lookup("#forwardControl").query();

    robot.clickOn(backControl);
    robot.clickOn(backControl);

    assertEquals(true, backControl.isDisable());
    assertEquals(false, forwardControl.isDisable());

    robot.clickOn(forwardControl);

    assertEquals(false, backControl.isDisable());
    assertEquals(false, forwardControl.isDisable());

    robot.clickOn(forwardControl);

    assertEquals(false, backControl.isDisable());
    assertEquals(true, forwardControl.isDisable());
  }

  @Test
  @DisplayName(
    "Assert that new #handleNavigation disables $forwardControl on new navigation"
  )
  void handleNavigation(FxRobot robot) {
    this.router.navigate("/foo");
    this.router.navigate("/bar");

    var backControl = robot.lookup("#backControl").query();
    var forwardControl = robot.lookup("#forwardControl").query();

    robot.clickOn(backControl);

    assertEquals(false, backControl.isDisable());
    assertEquals(false, forwardControl.isDisable());

    this.router.navigate("/bar");

    assertEquals(false, backControl.isDisable());
    assertEquals(true, forwardControl.isDisable());
  }
}
