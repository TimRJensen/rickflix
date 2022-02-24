package views.auth;

import app.App;
import com.google.common.hash.Hashing;
import components.hint_modal.HintModal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import store.Session;
import store.User;
import store.data.UserData;

public class Auth {

  private final Map<Enum<FormKeys>, String> formData = new HashMap<>();

  public final Map<Enum<FormKeys>, String> getFormData() {
    return this.formData;
  }

  @FXML
  private StackPane view;

  @FXML
  private VBox content, inputGroup;

  @FXML
  protected final void handleSubmit(ActionEvent event) {
    this.view.requestFocus();

    var session = Session.getCurrent();

    if (session != null) {
      session.end();
    }

    var user = App
      .getCurrent()
      .store.from("users")
      .select()
      .where("email", this.formData.get(FormKeys.EMAIL))
      .where(
        "password",
        Hashing
          .sha256()
          .hashString(
            this.formData.get(FormKeys.PASSWORD),
            StandardCharsets.UTF_8
          )
          .toString()
      )
      .asSingle(UserData.class);

    if (user == null) {
      new HintModal(
        this.view,
        this.content,
        new ArrayList<>(
          List.of(new String[] { "Incorrect email or password." })
        )
      );

      return;
    }

    new User(user);

    new Session(user.session);
    App.getCurrent().router.navigate("/landing");
  }

  @FXML
  protected final void handleClick(MouseEvent event) {
    App.getCurrent().router.navigate("/account/new");
  }

  public void initialize() {
    //TODO remove this eventually.
    this.formData.put(FormKeys.EMAIL, "user@domain.com");
    this.formData.put(FormKeys.PASSWORD, "password");

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
