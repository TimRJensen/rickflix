package components.input;

import java.util.Map;
import java.util.regex.Pattern;
import javafx.beans.NamedArg;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class Input extends VBox {

  private enum Styles {
    SUCCESS;

    @Override
    public final String toString() {
      return "success";
    }
  }

  private Input dependency;
  private Types type;
  private Enum<?> key;

  public final Enum<?> getKey() {
    return this.key;
  }

  private TextField input;
  private Map<Enum<?>, String> formData;

  private final StringProperty textProperty() {
    return this.input.textProperty();
  }

  public final String getText() {
    if (this.formData != null) {
      return this.formData.get(this.key);
    }

    return this.input.getText();
  }

  private Map<Input, Boolean> validations;

  private final ChangeListener<String> handleChange = (
    observer,
    prev,
    next
  ) -> {
    if (this.formData == null) {
      return;
    }

    this.formData.put(this.key, next);
  };

  private final ChangeListener<String> handleDependency = (
    observer,
    prev,
    next
  ) -> {
    if (this.getText() == null || this.getText().isEmpty()) {
      return;
    }

    var styleClass = this.input.getStyleClass();

    if (this.input.getText().equals(next)) {
      styleClass.add(Styles.SUCCESS.toString());
      this.validations.put(this, true);

      return;
    }

    styleClass.remove(Styles.SUCCESS.toString());
    this.validations.put(this, false);
  };

  private final ChangeListener<Boolean> handleFocus = (
    observer,
    prev,
    next
  ) -> {
    if (next) {
      this.input.getStyleClass().removeAll(Styles.SUCCESS.toString());
      this.validations.put(this, false);

      return;
    }

    var flag = false;
    var value = this.input.getText();

    switch (this.type) {
      case EMAIL:
        flag =
          value != null &&
          Pattern
            .compile(
              "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
            )
            .matcher(value)
            .matches();

        break;
      case PASSWORD:
        flag = value != null && !value.isEmpty() && value.length() > 0;

        break;
      default:
        flag = value != null && !value.isEmpty();
    }

    if (this.dependency != null) {
      flag =
        value != null &&
        !value.isEmpty() &&
        value.equals(this.dependency.getText());
    }

    this.validations.put(this, flag);

    if (!flag) {
      this.input.getStyleClass().remove(Styles.SUCCESS.toString());

      return;
    }

    this.input.getStyleClass().add(Styles.SUCCESS.toString());
  };

  public Input(
    @NamedArg(value = "type", defaultValue = "TEXT") Types type,
    @NamedArg(value = "promptText", defaultValue = "") String promptText,
    @NamedArg("key") Enum<?> key,
    @NamedArg("formData") Map<Enum<?>, String> formData,
    @NamedArg("validations") Map<Input, Boolean> validations,
    @NamedArg("dependency") Input dependency
  ) {
    this.type = type;
    this.key = key;
    this.formData = formData;
    this.validations = validations;
    this.dependency = dependency;

    this.input =
      type.equals(Types.PASSWORD) ? new PasswordField() : new TextField();
    this.input.getStyleClass().add("input");
    this.input.setPromptText(key != null ? key.toString() : promptText);

    this.input.textProperty().addListener(this.handleChange);

    if (formData != null) {
      this.input.setText(this.formData.get(this.key));
    }

    if (validations != null) {
      this.input.focusedProperty().addListener(this.handleFocus);
      this.validations.put(this, false);
    }

    if (dependency != null) {
      this.dependency.textProperty().addListener(this.handleDependency);
    }

    this.getChildren().add(this.input);

    var loader = new FXMLLoader(this.getClass().getResource("input.fxml"));

    loader.setRoot(this);
    loader.setController(this);

    try {
      loader.load();
    } catch (Exception error) {
      error.printStackTrace();
    }
  }
}
