package components.font_icon_button;

import components.font_icon.FontIcon;
import javafx.beans.NamedArg;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;

public class FontIconButton extends Button {

  private String type;

  @FXML
  private FontIcon fontIcon;

  public final String getType() {
    return this.type;
  }

  public final void setType(String value) {
    this.type = value;
    this.fontIcon.setType(value);
  }

  private final int size;

  public final int getSize() {
    return this.size;
  }

  public FontIconButton(
    @NamedArg("type") String type,
    @NamedArg("size") int size
  ) {
    this.type = type;
    this.size = size;

    var loader = new FXMLLoader(
      this.getClass().getResource("font_icon_button.fxml")
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
