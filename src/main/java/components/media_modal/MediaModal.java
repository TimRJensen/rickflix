package components.media_modal;

import app.App;
import components.media_card.MediaCard;
import java.io.File;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import store.data.MediaData;

public class MediaModal extends FlowPane {

  private final MediaData mediaData;

  public final MediaData getMediaData() {
    return this.mediaData;
  }

  @FXML
  private HBox content;

  @FXML
  private ImageView imageBox;

  @FXML
  private final void handleClose(ActionEvent event) {
    ((Pane) this.getParent()).getChildren().remove(this);
  }

  @FXML
  private final void handleRedirect(ActionEvent event) {
    App.getCurrent().router.navigate(this.mediaData.url);
  }

  public MediaModal(Pane anchor, MediaCard target, MediaData mediaData) {
    if (mediaData == null) {
      this.mediaData = null;

      return;
    }

    this.mediaData = mediaData;

    var loader = new FXMLLoader(
      this.getClass().getResource("media_modal.fxml")
    );

    loader.setRoot(this);
    loader.setController(this);

    try {
      loader.load();
    } catch (Exception error) {
      error.printStackTrace();
    }

    this.maxHeightProperty().bind(target.heightProperty());

    var widthProperty = anchor.widthProperty();

    this.content.prefWidthProperty().bind(widthProperty);
    this.content.maxWidthProperty().bind(widthProperty);

    this.translateYProperty().bind(target.layoutYProperty());

    this.imageBox.setImage(
        new Image(new File(this.mediaData.imagePath).toURI().toString())
      );
  }
}
