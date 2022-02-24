package views.landing;

import app.App;
import components.input.Input;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import store.Session;
import store.data.MediaData;
import store.data.SearchData;
import store.data.TagData;

public class Landing {

  private final List<MediaData> mediaData;

  public final List<MediaData> getMediaData() {
    return this.mediaData;
  }

  private final List<TagData> tagData;

  public final List<TagData> getTagData() {
    return this.tagData;
  }

  @FXML
  private GridPane view;

  @FXML
  private final void handleSearch(KeyEvent event) {
    if (event.getCode().equals(KeyCode.ENTER)) {
      App
        .getCurrent()
        .router.navigate(
          "/landing/search/" + ((Input) event.getSource()).getText()
        );
    }
  }

  public Landing() {
    var app = App.getCurrent();
    var store = app.store;
    var params = app.router.getLocation().params;

    this.tagData =
      store
        .from("tags")
        .select()
        .not()
        .where("title", "movie", "show")
        .asArrayList(TagData.class);

    if (params.get("methodId") == null) {
      this.mediaData =
        store
          .from("media")
          .select()
          .where("type", "movie", "show")
          .sort("production_year", "ranking")
          .asArrayList(MediaData.class);

      return;
    }

    if (params.get("methodId").equals("favorites")) {
      this.mediaData =
        store
          .from("media")
          .select()
          .where(
            "id",
            Session
              .getCurrent()
              .favorites.toArray(
                new String[Session.getCurrent().favorites.size()]
              )
          )
          .asArrayList(MediaData.class);

      return;
    }

    if (params.get("methodId").equals("tag")) {
      this.mediaData =
        store
          .from("media")
          .select()
          .where("type", "movie", "show")
          .like("tags", params.get("queryId"))
          .sort("production_year", "ranking")
          .asArrayList(MediaData.class);

      return;
    }

    if (params.get("methodId").equals("search")) {
      this.mediaData =
        new ArrayList<>(
          store
            .from("search_table")
            .select()
            .textSearch(params.get("queryId"))
            .asArrayList(SearchData.class)
            .stream()
            .map(entry -> new MediaData(entry))
            .toList()
        );

      return;
    }

    this.mediaData = null;
  }

  public void initialize() {
    this.view.setVisible(false);
    this.view.sceneProperty()
      .addListener((observer, prev, next) -> {
        this.view.setVisible(!this.view.isVisible());
        this.view.requestFocus();
      });
  }
}
