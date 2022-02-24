package components.media_card_container;

import components.media_card.MediaCard;
import java.util.List;
import javafx.beans.NamedArg;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import store.data.MediaData;

public class MediaCardContainer extends FlowPane {

  private double widthRatio;

  private List<MediaData> mediaData;

  private final ChangeListener<Number> handleResize = (
    observer,
    prev,
    next
  ) -> {
    if (this.mediaData.size() == 0) {
      return;
    }

    var childWidth = this.getChildren().get(0).getBoundsInParent().getWidth();
    var i = Math.floor(next.doubleValue() * this.widthRatio / childWidth);
    var newWidth = i * childWidth + (i - 1) * this.getHgap();

    this.setPrefWidth(newWidth);
    this.setMaxWidth(newWidth);
  };

  public MediaCardContainer(
    @NamedArg("widthRatio") double widthRatio,
    @NamedArg("mediaData") List<MediaData> mediaData,
    @NamedArg("root") Pane anchor
  ) {
    if (mediaData == null) {
      return;
    }

    this.widthRatio = widthRatio;
    this.mediaData = mediaData;

    var loader = new FXMLLoader(
      this.getClass().getResource("media_card_container.fxml")
    );

    loader.setRoot(this);
    loader.setController(this);

    try {
      loader.load();
    } catch (Exception error) {
      error.printStackTrace();
    }

    this.mediaData.forEach(entry -> {
        this.getChildren().add(new MediaCard(entry, anchor));
      });

    this.hgapProperty()
      .addListener((observer, prev, next) -> {
        this.handleResize.changed(null, 0, this.getScene().getWidth());
      });
    this.sceneProperty()
      .addListener((observer, prev, next) -> {
        if (next == null) {
          return;
        }

        next.widthProperty().addListener(this.handleResize);
        this.handleResize.changed(null, 0, next.getWidth());
      });
  }
}
