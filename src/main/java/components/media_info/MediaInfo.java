package components.media_info;

import components.font_icon_button.FontIconButton;
import components.tag.Tag;
import javafx.beans.NamedArg;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import store.Session;
import store.data.MediaData;

public class MediaInfo extends GridPane {

  private enum Styles {
    TOGGLED;

    @Override
    public String toString() {
      return "toggled";
    }
  }

  private final MediaData mediaData;

  public final String getTitle() {
    return this.mediaData.title;
  }

  public final String getContent() {
    return this.mediaData.content;
  }

  private final boolean closeable;

  public final boolean getCloseable() {
    return this.closeable;
  }

  @FXML
  private HBox tagGroup;

  @FXML
  private FontIconButton favoriteControl, closeControl;

  public final void setOnClose(EventHandler<ActionEvent> value) {
    this.closeControl.setOnAction(value);
  }

  public final EventHandler<? super ActionEvent> getOnClose() {
    return this.closeControl.getOnAction();
  }

  @FXML
  private final void handleFavoriting(ActionEvent event) {
    var session = Session.getCurrent();
    var mediaId = this.mediaData.showId != null
      ? this.mediaData.showId
      : this.mediaData.id;
    var button = this.favoriteControl;

    if (event == null) {
      if (session.favorites.contains(mediaId)) {
        button.getStyleClass().add(Styles.TOGGLED.toString());
      }

      return;
    }

    if (session.favorites.contains(mediaId)) {
      button.getStyleClass().remove(Styles.TOGGLED.toString());
      session.favorites.remove(mediaId);

      return;
    }

    button.getStyleClass().add(Styles.TOGGLED.toString());
    session.favorites.add(mediaId);
  }

  public MediaInfo(
    @NamedArg(value = "mediaData") MediaData mediaData,
    @NamedArg(value = "closeable", defaultValue = "true") boolean closeable
  ) {
    if (mediaData == null) {
      this.mediaData = null;
      this.closeable = false;

      return;
    }

    this.mediaData = mediaData;
    this.closeable = closeable;

    var loader = new FXMLLoader(this.getClass().getResource("media_info.fxml"));

    loader.setRoot(this);
    loader.setController(this);

    try {
      loader.load();
    } catch (Exception error) {
      error.printStackTrace();
    }

    this.mediaData.tags.forEach(entry -> {
        this.tagGroup.getChildren().add(new Tag(entry));
      });

    this.handleFavoriting(null);
  }
}
