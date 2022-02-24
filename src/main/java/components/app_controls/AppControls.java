package components.app_controls;

import components.font_icon_button.FontIconButton;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.FlowPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class AppControls extends FlowPane {

  @FXML
  private FontIconButton resizeControl;

  public final void handleClose(ActionEvent event) {
    this.getScene().getWindow().hide();

    Platform.exit();
  }

  public final void handleMinimize(ActionEvent event) {
    var stage = (Stage) this.getScene().getWindow();

    stage.setIconified(true);
  }

  public final void handleResize(ActionEvent event) {
    var stage = (Stage) this.getScene().getWindow();

    if (stage.isMaximized()) {
      this.resizeControl.setType("maximize");

      stage.setMaximized(false);

      return;
    }

    this.resizeControl.setType("resize");

    stage.setMaximized(true);

    var screen = Screen
      .getScreensForRectangle(stage.getX(), stage.getY(), 1D, 1D)
      .get(0);
    var bounds = screen.getVisualBounds();

    stage.setHeight(bounds.getHeight());
  }

  public AppControls() {
    var loader = new FXMLLoader(
      this.getClass().getResource("app_controls.fxml")
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
