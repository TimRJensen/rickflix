package components.hint_modal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import app.App;
import components.font_icon_button.FontIconButton;
import java.util.List;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

@ExtendWith(ApplicationExtension.class)
public class HintModalTest extends App {

  private Button target;
  private HintModal hintModal;

  @Start
  public void start(Stage stage) {
    var anchor = new StackPane();

    this.target = new Button("show");
    this.target.setPrefSize(50, 50);
    this.target.setOnAction(event -> {
        this.hintModal = new HintModal(anchor, target, (List.of("foo", "bar")));
      });

    anchor.getChildren().add(this.target);
    anchor
      .getStylesheets()
      .add(this.getClass().getResource("/css/index.css").toString());

    stage.setScene(new Scene(anchor, 1000, 400));
    stage.show();
  }

  @Test
  @DisplayName("Assert that HintModal shows on condition")
  void handleClick(FxRobot robot) {
    robot.clickOn(this.target);

    assertNotNull(this.hintModal);
    assertEquals("foo", robot.lookup("foo").queryAs(Label.class).getText());
  }

  @Test
  @DisplayName("Assert that #handleClose closes the modal")
  void handleClose(FxRobot robot) {
    robot
      .clickOn(this.target)
      .clickOn(robot.lookup("").queryAs(FontIconButton.class));

    assertNull(this.hintModal.getScene());
  }
}
