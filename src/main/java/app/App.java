package app;

import java.nio.file.Paths;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import store.Session;
import store.Store;
import util.HistoryManager;
import util.Router;

//TODO unit tests -.-
//FIXME fix 'Scener ut en äkteskab'

public class App extends Application {

  private static App current;

  public static final App getCurrent() {
    return App.current;
  }

  private final String appName = "rickflix";
  private final String appLogo = "/public/new-logo.png";

  public final Store store;
  public final HistoryManager history;
  public final Router router;

  public static void main(String[] args) {
    App.launch(args);
  }

  public App() {
    App.current = this;

    this.store =
      new Store(Paths.get("").resolve(".data").toAbsolutePath().toString());
    this.history = new HistoryManager();
    this.router = new Router(this.history);

    Font.loadFont(
      this.getClass()
        .getResource("/public/fontawesome-webfont.ttf")
        .toExternalForm(),
      12
    );
  }

  private double x = 0;
  private double y = 0;

  @Override
  public void start(Stage stage) {
    try {
      var scene = new Scene(
        FXMLLoader.load(this.getClass().getResource("/views/index.fxml"))
      );

      scene.setOnMousePressed(event -> {
        this.x = event.getSceneX();
        this.y = event.getSceneY();
      });
      scene.setOnMouseDragged(event -> {
        stage.setX(event.getScreenX() - x);
        stage.setY(event.getScreenY() - y);
      });

      stage.initStyle(StageStyle.UNDECORATED);
      stage.setTitle(this.appName);
      stage
        .getIcons()
        .add(new Image(this.getClass().getResource(this.appLogo).toString()));
      stage.setScene(scene);
      stage.show();

      this.router.navigate("/");
    } catch (Exception error) {
      error.printStackTrace();
    }
  }

  @Override
  public void stop() {
    this.router.navigate("/");

    if (Session.getCurrent() != null) {
      Session.getCurrent().end();
    }

    this.store.close();

    System.out.println("closing App");

    System.exit(0);
  }
}
