package components.menu_bar;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.HBox;

public class MenuBar extends HBox {

  public MenuBar() {
    var loader = new FXMLLoader(this.getClass().getResource("menu_bar.fxml"));

    loader.setRoot(this);
    loader.setController(this);

    try {
      loader.load();
    } catch (Exception error) {
      error.printStackTrace();
    }
  }
}
