package components.media_card;

import app.App;
import components.media_modal.MediaModal;
import java.io.File;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import store.data.MediaData;

public class MediaCard extends VBox {

  private static MediaModal infoModal;

  private final Pane root;
  private final MediaData mediaData;

  public final String getTitle() {
    return this.mediaData.title;
  }

  @FXML
  private ImageView imageBox;

  @FXML
  private void handleClick(MouseEvent event) {
    if (this.root == null) {
      App.getCurrent().router.navigate(this.mediaData.url);

      return;
    }

    if (MediaCard.infoModal != null) {
      this.root.getChildren().remove(MediaCard.infoModal);
    }

    MediaCard.infoModal =
      new MediaModal((Pane) this.getParent(), this, this.mediaData);

    this.root.getChildren().add(MediaCard.infoModal);
  }

  public MediaCard(MediaData mediaData, Pane root) {
    if (mediaData == null) {
      this.mediaData = null;
      this.root = null;

      return;
    }

    this.mediaData = mediaData;
    this.root = root;

    var loader = new FXMLLoader(this.getClass().getResource("media_card.fxml"));

    loader.setRoot(this);
    loader.setController(this);

    try {
      loader.load();
    } catch (Exception error) {
      error.printStackTrace();
    }

    this.imageBox.setImage(
        new Image(new File(this.mediaData.imagePath).toURI().toString())
      );
  }
}
