package components.input;

import static javafx.geometry.Pos.BOTTOM_CENTER;
import static org.junit.jupiter.api.Assertions.assertEquals;

import app.App;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

enum FormKeys {
  FOO("foo"),
  BAR("bar");

  private final String label;

  @Override
  public final String toString() {
    return this.label;
  }

  private FormKeys(String label) {
    this.label = label;
  }
}

@ExtendWith(ApplicationExtension.class)
public class InputTest extends App {

  private Map<Enum<?>, String> data;
  private Map<Input, Boolean> validations;
  private VBox root;

  @Start
  public void start(Stage stage) {
    this.root = new VBox();

    root
        .getStylesheets()
        .add(this.getClass().getResource("/css/index.css").toString());

    stage.setScene(new Scene(this.root, 1000, 400));
    stage.show();
  }

  @BeforeEach
  void initTest() {
    this.data = new HashMap<Enum<?>, String>();
    this.validations = new HashMap<Input, Boolean>();
  }

  @Test
  @DisplayName("Assert that new Input, with type text & data, sets text")
  void newInstanceWithData(FxRobot robot) {
    this.data.put(FormKeys.FOO, "foo");

    var input = new Input(
        Types.TEXT,
        "foo",
        FormKeys.FOO,
        this.data,
        null,
        null);

    robot.interact(() -> {
      this.root.getChildren().add(input);
    });

    assertEquals("foo", input.getText());
    assertEquals("foo", this.data.get(FormKeys.FOO));
  }

  @Test
  @DisplayName("Assert that #getText, with type text & no data, validation or dependency, gets user text")
  void newInstanceWithNoDataAndValidationAndDependency(FxRobot robot) {
    this.data.put(FormKeys.FOO, "foo");

    var input = new Input(Types.TEXT, "foo", FormKeys.FOO, null, null, null);

    robot.interact(() -> {
      this.root.getChildren().add(input);
    });

    robot.clickOn(input).write("foo");

    assertEquals("foo", input.getText());
  }

  @Test
  @DisplayName("Assert that #handleFocus with type text validates value on blur")
  void handleFocusWithText(FxRobot robot) {
    var input = new Input(
        Types.TEXT,
        "foo",
        FormKeys.FOO,
        this.data,
        this.validations,
        null);

    robot.interact(() -> {
      this.root.getChildren().add(input);
    });

    var textfield = (TextField) input.getChildren().get(0);

    robot.clickOn(input).write("foo");
    robot.interact(() -> {
      this.root.requestFocus();
    });

    assertEquals("foo", input.getText());
    assertEquals("foo", this.data.get(FormKeys.FOO));
    assertEquals(true, this.validations.get(input));
    assertEquals(true, textfield.getStyleClass().contains("success"));

    robot.clickOn(input);

    assertEquals(false, this.validations.get(input));
    assertEquals(false, textfield.getStyleClass().contains("success"));

    robot.interact(() -> {
      textfield.clear();
      this.root.requestFocus();
    });

    assertEquals("", input.getText());
    assertEquals("", this.data.get(FormKeys.FOO));
    assertEquals(false, this.validations.get(input));
    assertEquals(false, textfield.getStyleClass().contains("success"));
  }

  @Test
  @DisplayName("Assert that #handleFocus with type email validates value on blur")
  void handleFocusWithEmail(FxRobot robot) {
    var input = new Input(
        Types.EMAIL,
        "foo",
        FormKeys.FOO,
        this.data,
        this.validations,
        null);

    robot.interact(() -> {
      this.root.getChildren().add(input);
    });

    var textfield = (TextField) input.getChildren().get(0);

    robot.clickOn(textfield).write("foo@bar.com");
    robot.interact(() -> {
      this.root.requestFocus();
    });

    assertEquals("foo@bar.com", input.getText());
    assertEquals("foo@bar.com", this.data.get(FormKeys.FOO));
    assertEquals(true, this.validations.get(input));
    assertEquals(true, textfield.getStyleClass().contains("success"));

    robot.clickOn(textfield);

    assertEquals(false, this.validations.get(input));
    assertEquals(false, textfield.getStyleClass().contains("success"));

    robot.interact(() -> {
      textfield.clear();
    });

    robot.write("|@bar.com");
    robot.interact(() -> {
      this.root.requestFocus();
    });

    assertEquals("|@bar.com", input.getText());
    assertEquals("|@bar.com", this.data.get(FormKeys.FOO));
    assertEquals(false, this.validations.get(input));
    assertEquals(false, textfield.getStyleClass().contains("success"));

    robot.interact(() -> {
      textfield.clear();
      this.root.requestFocus();
    });

    assertEquals("", input.getText());
    assertEquals("", this.data.get(FormKeys.FOO));
    assertEquals(false, this.validations.get(input));
    assertEquals(false, textfield.getStyleClass().contains("success"));
  }

  @Test
  @DisplayName("Assert that #handleFocus with type password validates value on blur")
  void handleFocusWithPassword(FxRobot robot) {
    var inputA = new Input(
        Types.PASSWORD,
        "foo",
        FormKeys.FOO,
        this.data,
        this.validations,
        null);
    var inputB = new Input(
        Types.PASSWORD,
        "bar",
        FormKeys.BAR,
        this.data,
        this.validations,
        inputA);

    robot.interact(() -> {
      inputB.setAlignment(BOTTOM_CENTER);
      this.root.getChildren().addAll(inputA, inputB);
    });

    var textfieldA = (TextField) inputA.getChildren().get(0);
    var textfieldB = (TextField) inputB.getChildren().get(0);

    robot.clickOn(textfieldA).write("foobar");
    robot.interact(() -> {
      this.root.requestFocus();
    });

    assertEquals("foobar", textfieldA.getText());
    assertEquals("foobar", this.data.get(FormKeys.FOO));
    assertEquals(true, this.validations.get(inputA));
    assertEquals(true, textfieldA.getStyleClass().contains("success"));

    robot.clickOn(textfieldB).write("foobar");
    robot.interact(() -> {
      this.root.requestFocus();
    });

    assertEquals("foobar", textfieldB.getText());
    assertEquals("foobar", this.data.get(FormKeys.BAR));
    assertEquals(true, this.validations.get(inputB));
    assertEquals(true, textfieldB.getStyleClass().contains("success"));

    robot.clickOn(textfieldA);

    assertEquals(false, this.validations.get(inputA));
    assertEquals(false, textfieldA.getStyleClass().contains("success"));

    robot.interact(() -> {
      textfieldA.clear();
    });

    assertEquals("", inputA.getText());
    assertEquals("", this.data.get(FormKeys.FOO));
    assertEquals(false, this.validations.get(inputA));
    assertEquals(false, textfieldA.getStyleClass().contains("success"));

    assertEquals("foobar", this.data.get(FormKeys.BAR));
    assertEquals(false, this.validations.get(inputB));
    assertEquals(false, textfieldB.getStyleClass().contains("success"));
  }
}
