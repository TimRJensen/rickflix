package components.navigator_controls;

import app.App;
import components.font_icon_button.FontIconButton;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.FlowPane;

public class NavigatorControls extends FlowPane {

  @FXML
  private FontIconButton backControl, forwardControl;

  @FXML
  private final void handleBack(ActionEvent event) {
    App.getCurrent().history.back();
  }

  @FXML
  private final void handleForward(ActionEvent event) {
    App.getCurrent().history.forward();
  }

  private ChangeListener<Number> handleNavigation = (observer, prev, next) -> {
    if ((int) next > 0 && this.backControl.isDisable()) {
      this.backControl.setDisable(false);
    }

    if ((int) next < (int) prev && this.forwardControl.isDisable()) {
      this.forwardControl.setDisable(false);
    }

    if ((int) next == 0) {
      this.backControl.setDisable(true);
    }

    if ((int) next == App.getCurrent().history.size() - 1) {
      this.forwardControl.setDisable(true);
    }
  };

  public NavigatorControls() {
    var loader = new FXMLLoader(
      this.getClass().getResource("navigator_controls.fxml")
    );

    loader.setRoot(this);
    loader.setController(this);
    try {
      loader.load();
    } catch (Exception error) {
      error.printStackTrace();
    }

    this.sceneProperty()
      .addListener((observer, prev, next) -> {
        var app = App.getCurrent();

        if (next == null) {
          app.history.indexProperty().removeListener(this.handleNavigation);

          return;
        }

        app.history.indexProperty().addListener(this.handleNavigation);
      });
  }
}
