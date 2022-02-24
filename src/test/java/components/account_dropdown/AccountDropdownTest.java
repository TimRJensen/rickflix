package components.account_dropdown;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import app.App;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import store.User;
import store.data.UserData;
import util.Route;

@ExtendWith(ApplicationExtension.class)
@ExtendWith(MockitoExtension.class)
public class AccountDropdownTest extends App {

  private AccountDropdown accountDropdown;

  @Start
  public void start(Stage stage) {
    this.router.routes.put("/", new Route("/", null));
    this.router.routes.put("/account/edit", new Route("/account/edit", null));
    this.router.routes.put(
        "/landing",
        new Route("/landing/:methodId/:queryId", null)
      );

    var connection = mock(Connection.class);
    var statement = mock(Statement.class);
    var response = mock(ResultSet.class);

    try {
      when(response.getInt("id")).thenReturn(0);
      when(response.getString("email")).thenReturn("foo@bar.com");
      when(response.getString("password")).thenReturn("foobar");
      when(response.getString("name")).thenReturn("Arthur Fonzarelli");
      when(response.getString("country")).thenReturn("US");
      when(response.getStatement()).thenReturn(statement);

      when(statement.getConnection()).thenReturn(connection);
      when(statement.executeQuery(contains("sessions"))).thenReturn(response);
      when(statement.executeQuery(contains("watching"))).thenReturn(response);

      when(connection.createStatement()).thenReturn(statement);

      new User(new UserData(response));
    } catch (SQLException e) {
      e.printStackTrace();
    }

    this.accountDropdown = new AccountDropdown();

    var root = new StackPane(this.accountDropdown);

    root
      .getStylesheets()
      .add(this.getClass().getResource("/css/index.css").toString());

    stage.setScene(new Scene(root, 1000, 400));
    stage.show();
  }

  @Test
  @DisplayName("Assert that #handleEdit navigates to location")
  void handleEdit(FxRobot robot) {
    robot.clickOn(this.accountDropdown).clickOn("Profile");

    assertEquals("/account/edit", this.router.getLocation().path);
  }

  @Test
  @DisplayName("Assert that #handleFavorites navigates to location")
  void handleFavorites(FxRobot robot) {
    robot.clickOn(this.accountDropdown).clickOn("Favorites");

    assertEquals("/landing", this.router.getLocation().path);
    assertEquals("favorites", this.router.getLocation().params.get("methodId"));
  }

  @Test
  @DisplayName("Assert that #handleLogout navigates to location")
  void handleLogout(FxRobot robot) {
    robot.clickOn(this.accountDropdown).clickOn("Logout");

    assertEquals("/", this.router.getLocation().path);
  }
}
