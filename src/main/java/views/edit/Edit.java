package views.edit;

import app.App;
import com.google.common.hash.Hashing;
import components.hint_modal.HintModal;
import components.input.Input;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import store.User;
import store.data.UserData;

public class Edit {

  private final Map<Enum<FormKeys>, String> formData = new HashMap<>();

  public final Map<Enum<FormKeys>, String> getFormData() {
    return this.formData;
  }

  private final Map<Input, Boolean> validations = new LinkedHashMap<>();

  public final Map<Input, Boolean> getValidations() {
    return this.validations;
  }

  @FXML
  private StackPane view;

  @FXML
  private VBox content, inputGroup;

  private final boolean validateNewUser() {
    if (this.formData.get(FormKeys.EMAIL).equals(User.getCurrent().email)) {
      return true;
    }

    var existingUser = App
      .getCurrent()
      .store.from("users")
      .select()
      .where("email", this.formData.get(FormKeys.EMAIL))
      .asSingle(UserData.class);

    if (existingUser == null) {
      return true;
    }

    new HintModal(
      this.view,
      this.content,
      new ArrayList<>(
        List.of(new String[] { "Provided email is already registered." })
      )
    );

    return false;
  }

  private final Map<String, String> validateFormData() {
    var user = User.getCurrent();
    var list = new ArrayList<String>();
    var result = new HashMap<String, String>();

    this.validations.entrySet()
      .forEach(entry -> {
        var key = (FormKeys) entry.getKey().getKey();
        var value = this.formData.get(key);

        switch (key) {
          case EMAIL:
            if (value.equals(user.email)) {
              return;
            }

            if (entry.getValue()) {
              result.put(key.toString(), value);

              return;
            }

            list.add(
              value.isEmpty()
                ? "Email is required."
                : "Provided email is invalid."
            );

            return;
          case PASSWORD:
            if (value.isEmpty()) {
              return;
            }

            if (entry.getValue()) {
              return;
            }

            list.add("Password must be 0 characters long.");

            return;
          case CONFIRMED_PASSWORD:
            var dependency = this.formData.get(FormKeys.PASSWORD);

            if (dependency.isEmpty()) {
              return;
            }

            if (entry.getValue()) {
              result.put(
                FormKeys.PASSWORD.toString(),
                Hashing
                  .sha256()
                  .hashString(value, StandardCharsets.UTF_8)
                  .toString()
              );

              return;
            }

            list.add(
              value.isEmpty()
                ? "Confirmed password is required"
                : "Password and confirmed password must match."
            );

            return;
          case NAME:
            if (value.equals(user.name)) {
              return;
            }

            if (entry.getValue()) {
              result.put(key.toString(), value);

              return;
            }

            list.add("Name is required.");

            return;
          case COUNTRY:
            if (value.equals(user.country)) {
              return;
            }

            if (entry.getValue()) {
              result.put(key.toString(), value);

              return;
            }

            list.add("Country is required.");

            return;
          default:
        }
      });

    if (list.size() > 0) {
      new HintModal(this.view, this.content, list);
    }

    return result;
  }

  @FXML
  private final void handleSubmit(ActionEvent event) {
    this.view.requestFocus();

    if (!this.validateNewUser()) {
      return;
    }

    var values = this.validateFormData();

    if (values.size() == 0) {
      return;
    }

    var fields = new ArrayList<String>();

    fields.add("id");
    fields.addAll(values.keySet());

    var app = App.getCurrent();

    new User(
      app.store
        .from("users")
        .insert(fields.toArray(new String[fields.size()]))
        .setValue("id", User.getCurrent().id)
        .setValues(values)
        .asSingle(UserData.class)
    );

    app.store.commit();

    new HintModal(
      this.view,
      this.content,
      new ArrayList<>(List.of(new String[] { "Profiled sucessfully updated." }))
    );
  }

  @FXML
  private final void handleCancel(ActionEvent event) {
    App.getCurrent().history.back();
  }

  public Edit() {
    var user = User.getCurrent();

    this.formData.put(FormKeys.EMAIL, user.email);
    this.formData.put(FormKeys.NAME, user.name);
    this.formData.put(FormKeys.COUNTRY, user.country);
  }

  public void initialize() {
    this.view.setVisible(false);
    this.view.sceneProperty()
      .addListener((observer, prev, next) -> {
        if (next == null) {
          return;
        }

        this.view.setVisible(!this.view.isVisible());
        this.view.requestFocus();

        this.content.translateYProperty()
          .bind(next.heightProperty().multiply(-0.15));
      });
  }
}
