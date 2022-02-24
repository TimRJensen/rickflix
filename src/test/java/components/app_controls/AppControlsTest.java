package components.app_controls;

import static org.junit.jupiter.api.Assertions.assertEquals;

import app.App;
import components.font_icon_button.FontIconButton;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

@ExtendWith(MockitoExtension.class)
@ExtendWith(ApplicationExtension.class)
public class AppControlsTest extends App {

  private AppControls appControls;

  @Start
  public void start(Stage stage) {
    this.appControls = new AppControls();

    var root = new StackPane(this.appControls);

    root
      .getStylesheets()
      .add(this.getClass().getResource("/css/index.css").toString());

    stage.setScene(new Scene(root, 1000, 400));
    stage.show();
  }

  @Test
  @DisplayName("Assert that #handleMinimize minimizes the application")
  void handleMinimize(FxRobot robot) {
    robot.clickOn(this.appControls.getChildren().get(0));

    assertEquals(
      true,
      ((Stage) this.appControls.getScene().getWindow()).isIconified()
    );

    robot.interact(() -> {
      ((Stage) this.appControls.getScene().getWindow()).setIconified(false);
    });
  }

  @Test
  @DisplayName("Assert that #handleResize resizes the application")
  void handleResize(FxRobot robot) {
    var control = (FontIconButton) this.appControls.getChildren().get(1);

    robot.clickOn(control);

    assertEquals("resize", control.getType());
    assertEquals(
      true,
      ((Stage) this.appControls.getScene().getWindow()).isMaximized()
    );

    robot.clickOn(this.appControls.getChildren().get(1));

    assertEquals("maximize", control.getType());
    assertEquals(
      false,
      ((Stage) this.appControls.getScene().getWindow()).isMaximized()
    );
  }

  @Test
  @Disabled
  @DisplayName("Assert that #handleClose closes the application")
  void handleClose(FxRobot robot) {
    robot.clickOn(this.appControls.getChildren().get(2));

    assertEquals(true, Platform.isImplicitExit());
  }
}
