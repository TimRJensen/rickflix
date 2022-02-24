package components.tag;

import app.App;
import components.font_icon.FontIcon;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;

public class Tag extends Button {

  @FXML
  public final void handleClick(ActionEvent event) {
    var router = App.getCurrent().router;
    var value = this.getText();

    if (value.equalsIgnoreCase("all")) {
      router.navigate("/landing");

      return;
    }

    if (value.isEmpty()) {
      router.navigate("/landing/favorites");

      return;
    }

    router.navigate("/landing/tag/" + value);
  }

  public Tag(String label) {
    this.setText(label);

    var loader = new FXMLLoader(this.getClass().getResource("tag.fxml"));

    loader.setRoot(this);
    loader.setController(this);

    try {
      loader.load();
    } catch (Exception error) {
      error.printStackTrace();
    }
  }

  public Tag(FontIcon icon) {
    this.setGraphic(icon);

    var loader = new FXMLLoader(this.getClass().getResource("tag.fxml"));

    loader.setRoot(this);
    loader.setController(this);

    try {
      loader.load();
    } catch (Exception error) {
      error.printStackTrace();
    }
  }
}
