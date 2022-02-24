package components.view_manager;

import app.App;
import java.util.List;
import javafx.beans.NamedArg;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import util.Location;
import util.Route;

public class ViewManager extends BorderPane {

  private ChangeListener<Location> handleNavigation = (
      observer,
      prev,
      next) -> {
    this.setCenter(null);

    var app = App.getCurrent();

    app.router.locationProperty().removeListener(this.handleNavigation);

    var loader = new FXMLLoader(
        this.getClass()
            .getResource(App.getCurrent().router.routes.get(next.path).component));

    try {
      this.setCenter(loader.load());
    } catch (Exception error) {
      error.printStackTrace();
    }

    app.router.locationProperty().addListener(this.handleNavigation);
  };

  public ViewManager(@NamedArg("routes") List<Route> routes) {
    var loader = new FXMLLoader(
        this.getClass().getResource("view_manager.fxml"));

    loader.setRoot(this);
    loader.setController(this);

    try {
      loader.load();
    } catch (Exception error) {
      error.printStackTrace();
    }

    routes.forEach(entry -> {
      var keys = entry.path.split("/:");
      var match = keys[0];

      App.getCurrent().router.routes.put(match, entry);
    });

    this.sceneProperty()
        .addListener((observer, prev, next) -> {
          var app = App.getCurrent();

          if (next == null) {
            app.router.locationProperty().addListener(this.handleNavigation);

            return;
          }

          app.router.locationProperty().addListener(this.handleNavigation);
        });
  }
}
