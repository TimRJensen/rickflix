package components.account_dropdown;

import app.App;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.MenuButton;
import store.User;

public class AccountDropdown extends MenuButton {

  @FXML
  private final void handleEdit(ActionEvent event) {
    this.hide();

    App.getCurrent().router.navigate("/account/edit");
  }

  @FXML
  private final void handleFavorites(ActionEvent event) {
    this.hide();

    App.getCurrent().router.navigate("/landing/favorites");
  }

  @FXML
  private final void handleLogout(ActionEvent event) {
    var app = App.getCurrent();

    this.hide();

    app.history.clear();
    app.router.navigate("/");
  }

  public AccountDropdown() {
    var loader = new FXMLLoader(
      this.getClass().getResource("account_dropdown.fxml")
    );

    this.setText(
        String.valueOf(User.getCurrent().name.toCharArray()[0]).toUpperCase()
      );

    loader.setRoot(this);
    loader.setController(this);

    try {
      loader.load();
    } catch (Exception error) {
      error.printStackTrace();
    }
  }
}
