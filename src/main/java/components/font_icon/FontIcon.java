package components.font_icon;

import javafx.beans.NamedArg;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;

public class FontIcon extends Label {

  private StringProperty type = new SimpleStringProperty(this, "type");

  public final StringProperty typeProperty() {
    return this.type;
  }

  public final void setType(String type) {
    switch (type) {
      case "arrow-circle-left":
        this.type.set("\uf0a8");
        break;
      case "arrow-circle-right":
        this.type.set("\uf0a9");
        break;
      case "chevron-left":
        this.type.set("\uf053");
        break;
      case "chevron-right":
        this.type.set("\uf054");
        break;
      case "close":
        this.type.set("\uf00d");
        break;
      case "minimize":
        this.type.set("\uf2d1");
        break;
      case "maximize":
        this.type.set("\uf2d0");
        break;
      case "resize":
        this.type.set("\uf2d2");
        break;
      case "favorite":
        this.type.set("\uf005");
        break;
      case "play":
        this.type.set("\uf04b");
        break;
      case "play-alt":
        this.type.set("\uf144");
        break;
      case "pause":
        this.type.set("\uf04c");
        break;
      case "replay":
        this.type.set("\uf01e");
        break;
      case "expand":
        this.type.set("\uf065");
        break;
      case "compress":
        this.type.set("\uf066");
        break;
      case "sign-out":
        this.type.set("\uf2f5");
        break;
      default:
        this.type.set("\uf128");
    }
  }

  public final void setSize(int size) {
    this.setStyle("-fx-font-size: " + size + "px;");
  }

  public FontIcon(
    @NamedArg("type") String type,
    @NamedArg("size") int size,
    @NamedArg(value = "visible", defaultValue = "true") boolean visible
  ) {
    this.textProperty().bind(this.type);
    this.setType(type);
    this.setSize(size);

    if (!visible) {
      this.setVisible(false);
      this.setManaged(false);
    }

    var loader = new FXMLLoader(this.getClass().getResource("font_icon.fxml"));

    loader.setRoot(this);
    loader.setController(this);
    try {
      loader.load();
    } catch (Exception error) {
      error.printStackTrace();
    }
  }

  public FontIcon(@NamedArg("type") String type, @NamedArg("size") int size) {
    this(type, size, true);
  }
}
