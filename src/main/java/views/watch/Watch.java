package views.watch;

import app.App;
import components.input.Input;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import store.Session;
import store.data.MediaData;
import store.data.SeasonData;
import store.data.WatchingData;
import util.Location;

public class Watch {

  private MediaData mediaData;

  public final MediaData getMediaData() {
    return this.mediaData;
  }

  private List<SeasonData> seasonData;

  public final List<SeasonData> getSeasonData() {
    return this.seasonData;
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

  private final ChangeListener<Location> handleNavigation = (
    observer,
    prev,
    next
  ) -> {
    var store = App.getCurrent().store;
    var session = Session.getCurrent();
    var params = prev.params;
    var mediaId = params.get("mediaId");

    var watchingData = session.watching
      .stream()
      .filter(entry -> entry.mediaId.equals(mediaId))
      .findFirst()
      .orElse(null);

    if (next.path.contains("/watch")) {
      if (watchingData != null) {
        session.watching.remove(watchingData);

        store.from("watching").where("id", watchingData.id).delete();
      }

      return;
    }

    session.watching.add(
      store
        .from("watching")
        .insert("session_id", "media_id", "watching_id")
        .setValue("session_id", session.id)
        .setValue("media_id", mediaId)
        .setValue("watching_id", this.mediaData.id)
        .asSingle(WatchingData.class)
    );
  };

  public Watch() {
    var app = App.getCurrent();
    var store = app.store;
    var session = Session.getCurrent();
    var params = app.router.getLocation().params;
    var mediaTypeId = params.get("mediaTypeId");
    var mediaId = params.get("mediaId");
    var episodeId = params.get("episodeId");

    var watchingData = session.watching
      .stream()
      .filter(entry -> entry.mediaId.equals(mediaId))
      .findFirst()
      .orElse(null);

    if (mediaTypeId.equals("show")) {
      this.seasonData =
        store
          .from("seasons")
          .select()
          .where("show_id", mediaId)
          .asArrayList(SeasonData.class);

      if (watchingData != null && watchingData.watchingId != null) {
        this.mediaData =
          store
            .from("media")
            .where("id", watchingData.watchingId)
            .select()
            .asSingle(MediaData.class);

        return;
      }

      this.mediaData =
        store
          .from("media")
          .where("id", episodeId)
          .select()
          .asSingle(MediaData.class);

      return;
    }

    this.seasonData = null;
    this.mediaData =
      store
        .from("media")
        .where("id", params.get("mediaId"))
        .select()
        .asSingle(MediaData.class);
  }

  public void initialize() {
    this.view.setVisible(false);
    this.view.sceneProperty()
      .addListener((observer, prev, next) -> {
        var app = App.getCurrent();

        if (next == null) {
          app.router.locationProperty().removeListener(this.handleNavigation);

          return;
        }

        this.view.setVisible(!this.view.isVisible());
        this.view.requestFocus();
        app.router.locationProperty().addListener(this.handleNavigation);
      });
  }
}
