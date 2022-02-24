package components.hint_modal;

import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class HintModal extends VBox {

  @FXML
  private GridPane content;

  @FXML
  private VBox hintGroup;

  @FXML
  private final void handleClose(ActionEvent event) {
    ((Pane) this.getParent()).getChildren().remove(this);
  }

  public HintModal(Pane anchor, Region target, List<String> labels) {
    var loader = new FXMLLoader(this.getClass().getResource("hint_modal.fxml"));

    loader.setRoot(this);
    loader.setController(this);

    try {
      loader.load();
    } catch (Exception error) {
      error.printStackTrace();
    }

    labels.forEach(entry -> {
      this.hintGroup.getChildren().add(new Label(entry));
    });

    anchor.getChildren().add(this);

    this.maxHeightProperty().bind(target.heightProperty().add(2));

    var widthProperty = anchor.widthProperty();

    this.content.prefWidthProperty().bind(widthProperty.multiply(0.33));
    this.content.maxWidthProperty().bind(widthProperty.multiply(0.33));

    this.translateYProperty().bind(target.translateYProperty());
  }
}
