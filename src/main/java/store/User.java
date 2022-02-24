package store;

import store.data.UserData;

public class User {

  private static User current;

  public static final User getCurrent() {
    return User.current;
  }

  public final int id;
  public final String email;
  public final String password;
  public final String name;
  public final String country;

  public User(UserData data) {
    this.id = data.id;
    this.email = data.email;
    this.password = data.password;
    this.name = data.name;
    this.country = data.country;

    User.current = this;
  }
}
