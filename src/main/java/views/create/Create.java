package views.create;

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
import java.util.stream.Collectors;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import store.Session;
import store.User;
import store.data.SessionData;
import store.data.UserData;

public class Create {

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

  private final boolean validateFormData() {
    var list = new ArrayList<String>();

    this.validations.entrySet()
      .forEach(entry -> {
        var key = (FormKeys) entry.getKey().getKey();
        var value = this.formData.get(key);

        switch (key) {
          case EMAIL:
            if (entry.getValue()) {
              return;
            }

            list.add(
              value == null || value.isEmpty()
                ? "Email is required."
                : "Provided email is invalid."
            );

            return;
          case PASSWORD:
            if (entry.getValue()) {
              return;
            }

            list.add(
              value == null || value.isEmpty()
                ? "Password is required."
                : "Password must be 0 characters long."
            );

            return;
          case CONFIRMED_PASSWORD:
            var dependency = this.formData.get(FormKeys.PASSWORD);

            if (dependency == null || dependency.isEmpty()) {
              return;
            }

            if (entry.getValue()) {
              this.formData.put(
                  FormKeys.PASSWORD,
                  Hashing
                    .sha256()
                    .hashString(value, StandardCharsets.UTF_8)
                    .toString()
                );

              return;
            }

            list.add(
              value.isEmpty()
                ? "Confirmed password is required."
                : "Password and confirmed password must match."
            );

            return;
          case NAME:
            if (entry.getValue()) {
              return;
            }

            list.add("Name is required.");

            return;
          case COUNTRY:
            if (entry.getValue()) {
              return;
            }

            list.add("Country is required.");

            return;
          default:
        }
      });

    if (list.size() > 0) {
      new HintModal(this.view, this.content, list);

      return false;
    }

    return true;
  }

  @FXML
  private final void handleSubmit(ActionEvent event) {
    this.view.requestFocus();

    if (!this.validateNewUser()) {
      return;
    }

    if (!this.validateFormData()) {
      return;
    }

    var session = Session.getCurrent();

    if (session != null) {
      session.end();
    }

    var values =
      this.formData.entrySet()
        .stream()
        .filter(entry -> entry.getKey() != FormKeys.CONFIRMED_PASSWORD)
        .collect(
          Collectors.toMap(
            entry -> entry.getKey().toString(),
            entry -> entry.getValue()
          )
        );

    var app = App.getCurrent();
    var user = new User(
      app.store
        .from("users")
        .insert(values.keySet().toArray(new String[values.size()]))
        .setValues(values)
        .asSingle(UserData.class)
    );

    new Session(
      app.store
        .from("sessions")
        .insert("user_id")
        .setValue("user_id", user.id)
        .asSingle(SessionData.class)
    );

    app.store.commit();

    app.router.navigate("/landing");
  }

  @FXML
  private final void handleCancel(ActionEvent event) {
    App.getCurrent().router.navigate("/");
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
