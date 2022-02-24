package components.media_container;

import app.App;
import components.font_icon_button.FontIconButton;
import java.io.File;
import javafx.beans.NamedArg;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Slider;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaMarkerEvent;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.util.Duration;
import store.Session;
import store.data.MediaData;
import store.data.ProgressData;
import util.Location;

public class MediaContainer extends StackPane {

  private final double fitHeight;

  public final double getFitHeight() {
    return this.fitHeight;
  }

  private final double fitWidth;

  public final double getFitWidth() {
    return this.fitWidth;
  }

  private MediaPlayer mediaPlayer;
  private MediaData mediaData;
  private ProgressData progressData;

  @FXML
  private MediaView mediaView;

  @FXML
  private Slider mediaProgress;

  @FXML
  private FontIconButton mediaPlaybackAction, mediaViewAction;

  @FXML
  @SuppressWarnings("incomplete-switch")
  private void handleMediaControl(ActionEvent event) {
    switch (this.mediaPlayer.getStatus()) {
      case STOPPED:
      case READY:
      case PAUSED:
        this.mediaPlaybackAction.setType("pause");

        if (this.mediaPlayer.getStatus() == Status.STOPPED) {
          this.mediaPlayer.seek(this.mediaPlayer.getStartTime());
        }

        this.mediaPlayer.play();
        break;
      case PLAYING:
        this.mediaPlaybackAction.setType("play");

        this.mediaPlayer.pause();
        break;
    }
  }

  @FXML
  private void handleMediaViewControl(ActionEvent event) {
    var stage = (Stage) this.getScene().getWindow();
    var flag = stage.isFullScreen();

    stage.setFullScreen(!flag);
    this.mediaViewAction.setType(flag ? "expand" : "compress");
  }

  @FXML
  private void handleMediaProgressChange(
    Property<Number> observer,
    Number prev,
    Number next
  ) {
    if (!this.mediaProgress.isValueChanging()) {
      return;
    }

    this.mediaPlayer.seek(Duration.millis((double) next));
  }

  private void setProgress(double value) {
    this.mediaProgress.setValueChanging(false);
    this.mediaProgress.setValue(value);
    this.mediaProgress.setValueChanging(true);
  }

  private final EventHandler<MediaMarkerEvent> handleMark = event -> {
    this.setProgress(event.getMarker().getValue().toMillis());
  };

  private final Runnable handleEnd = () -> {
    this.mediaPlaybackAction.setType("replay");
    this.mediaPlayer.stop();
  };

  private final Runnable handleLoaded = () -> {
    var media = this.mediaPlayer.getMedia();
    var markers = media.getMarkers();
    var duration = media.getDuration();

    if (markers.size() == 0) {
      var length = duration.toSeconds();
      var i = -1;

      while (++i < length) {
        markers.put(i + "000ms", Duration.millis(i * 1000));
      }
    }

    this.mediaProgress.setDisable(false);
    this.mediaProgress.setMax(duration.toMillis());
    this.setProgress(
        this.progressData != null ? this.progressData.progress : 0D
      );

    this.mediaPlayer.seek(Duration.millis(this.mediaProgress.getValue()));

    this.mediaPlaybackAction.setDisable(false);
  };

  private Scene prevScene;
  private Pane prevParent;

  private final ChangeListener<Boolean> handleFullscreen = (
    observer,
    prev,
    next
  ) -> {
    var scene = this.getScene();
    var stage = (Stage) scene.getWindow();

    if (stage == null) {
      return;
    }

    if (next) {
      this.prevScene = scene;

      this.prevParent = (Pane) this.getParent();
      this.prevParent.getChildren().remove(this);

      this.mediaView.fitHeightProperty().bind(stage.heightProperty());
      this.mediaView.fitWidthProperty().bind(stage.widthProperty());

      var nextScene = new Scene(this);

      nextScene
        .getStylesheets()
        .add(this.prevScene.getRoot().getStylesheets().get(0));

      stage.setScene(nextScene);

      return;
    }

    stage.setScene(prevScene);

    this.prevParent.getChildren().add(this);

    this.mediaView.fitHeightProperty().unbind();
    this.mediaView.fitWidthProperty().unbind();
    this.mediaView.setFitHeight(this.getFitHeight());
    this.mediaView.setFitWidth(this.getFitWidth());

    this.prevScene = null;
  };

  private final ChangeListener<Location> handleNavigation = (
    observer,
    prev,
    next
  ) -> {
    var store = App.getCurrent().store;
    var session = Session.getCurrent();

    if (this.mediaPlayer.getStatus() == Status.STOPPED) {
      session.progress.remove(this.progressData);

      store.from("progress").where("id", this.progressData.id).delete();

      return;
    }

    if (this.progressData != null) {
      session.progress.remove(this.progressData);
      session.progress.add(
        new ProgressData(
          this.progressData,
          this.mediaPlayer.getCurrentTime().toMillis()
        )
      );

      this.mediaPlayer.stop();

      return;
    }

    session.progress.add(
      store
        .from("progress")
        .insert("session_id", "media_id", "progress")
        .setValue("session_id", session.id)
        .setValue("media_id", this.mediaData.id)
        .setValue("progress", this.mediaPlayer.getCurrentTime().toMillis())
        .asSingle(ProgressData.class)
    );

    this.mediaPlayer.stop();
  };

  public MediaContainer(
    @NamedArg("fitHeight") double fitHeight,
    @NamedArg("fitWidth") double fitWidth,
    @NamedArg("mediaData") MediaData mediaData
  ) {
    this.fitHeight = fitHeight;
    this.fitWidth = fitWidth;
    this.mediaData = mediaData;
    this.progressData =
      Session
        .getCurrent()
        .progress.stream()
        .filter(entry -> entry.mediaId.equals(this.mediaData.id))
        .findFirst()
        .orElse(null);

    var loader = new FXMLLoader(
      this.getClass().getResource("media_container.fxml")
    );

    loader.setRoot(this);
    loader.setController(this);

    try {
      loader.load();
    } catch (Exception error) {
      error.printStackTrace();
    }

    this.mediaPlayer =
      new MediaPlayer(
        new Media(new File(this.mediaData.mediaPath).toURI().toString())
      );
    this.mediaPlayer.setOnReady(this.handleLoaded);
    this.mediaPlayer.setOnEndOfMedia(this.handleEnd);
    this.mediaPlayer.setOnMarker(this.handleMark);

    this.mediaView.setMediaPlayer(this.mediaPlayer);

    this.sceneProperty()
      .addListener((observer, prev, next) -> {
        var app = App.getCurrent();

        if (this.prevScene != null) {
          return;
        }

        if (next == null) {
          ((Stage) prev.getWindow()).fullScreenProperty()
            .removeListener(this.handleFullscreen);
          app.router.locationProperty().removeListener(this.handleNavigation);

          return;
        }

        ((Stage) next.getWindow()).fullScreenProperty()
          .addListener(this.handleFullscreen);
        app.router.locationProperty().addListener(this.handleNavigation);
      });
  }
}
