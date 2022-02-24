package components.tag;

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
public class TagTest extends App {

  private Tag tag;

  @Start
  public void start(Stage stage) {
    this.router.routes.put(
        "/landing",
        new Route("/landing/:methodId/:queryId", null)
      );
    this.tag = new Tag("All");

    var root = new StackPane(this.tag);

    root
      .getStylesheets()
      .add(this.getClass().getResource("/css/index.css").toString());

    stage.setScene(new Scene(root, 1000, 400));
    stage.show();
  }

  @Test
  @DisplayName("Assert that Tag component navigates to route")
  void onClick(FxRobot robot) {
    robot.clickOn(this.tag);

    assertEquals("/landing", this.router.getLocation().path);

    robot.interact(() -> {
      this.tag.setText("");
    });

    robot.clickOn(this.tag);

    assertEquals("", this.tag.getText());
    assertEquals("/landing", this.router.getLocation().path);
    assertEquals("favorites", this.router.getLocation().params.get("methodId"));

    robot.interact(() -> {
      this.tag.setText("foo");
    });
    robot.clickOn(this.tag);

    assertEquals("/landing", this.router.getLocation().path);
    assertEquals("tag", this.router.getLocation().params.get("methodId"));
    assertEquals("foo", this.router.getLocation().params.get("queryId"));
  }
}
