package components.season_info;

import components.media_card.MediaCard;
import components.media_card_container.MediaCardContainer;
import java.util.List;
import javafx.beans.NamedArg;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import store.data.MediaData;
import store.data.SeasonData;

public class SeasonInfo extends GridPane {

  private enum Styles {
    TOGGLED;

    @Override
    public String toString() {
      return "toggled";
    }
  }

  private MediaData mediaData;

  private List<SeasonData> seasonData;

  public List<MediaData> getEpisodes() {
    return this.seasonData.get(this.mediaData.season).episodes;
  }

  @FXML
  private FlowPane linkGroup;

  @FXML
  private MediaCardContainer mediaCardContainer;

  private final EventHandler<MouseEvent> handleClick = event -> {
    var links = this.linkGroup.getChildren();

    links.forEach(entry -> {
      entry.getStyleClass().remove(Styles.TOGGLED.toString());
    });

    var i = event != null
        ? links.indexOf(event.getSource())
        : this.mediaData.season;

    links.get(i).getStyleClass().add(Styles.TOGGLED.toString());

    var mediaCards = this.mediaCardContainer.getChildren();

    mediaCards.clear();

    this.seasonData.get(i).episodes.forEach(mediaInfo -> {
      mediaCards.add(new MediaCard(mediaInfo, null));
    });
  };

  public SeasonInfo(
      @NamedArg("mediaData") MediaData mediaData,
      @NamedArg("seasonData") List<SeasonData> seasonData) {
    if (mediaData == null || seasonData == null) {
      return;
    }

    this.mediaData = mediaData;
    this.seasonData = seasonData;

    if (this.seasonData.size() == 0) {
      return;
    }

    var loader = new FXMLLoader(
        this.getClass().getResource("season_info.fxml"));

    loader.setRoot(this);
    loader.setController(this);

    try {
      loader.load();
    } catch (Exception error) {
      error.printStackTrace();
    }

    this.seasonData.forEach(entry -> {
      var link = new Label(entry.title);

      link.getStyleClass().add("link");
      link.setOnMouseClicked(this.handleClick);

      this.linkGroup.getChildren().add(link);
    });

    this.handleClick.handle(null);
    this.getRowConstraints()
        .get(0)
        .prefHeightProperty()
        .bind(this.linkGroup.heightProperty());
  }
}
