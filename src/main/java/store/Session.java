package store;

import app.App;
import java.util.List;
import java.util.Set;
import store.data.ProgressData;
import store.data.SessionData;
import store.data.WatchingData;

public class Session {

  private static Session current;

  public static final Session getCurrent() {
    return Session.current;
  }

  public final int id;
  public final Set<String> favorites;
  public final List<WatchingData> watching;
  public final List<ProgressData> progress;

  public void end() {
    try (
      var response = App
        .getCurrent()
        .store.from("sessions")
        .insert("id", "favorites")
        .setValue("id", this.id)
        .setValue("favorites", this.favorites)
        .execute()
    ) {} catch (Exception error) {
      error.printStackTrace();
    }

    this.watching.forEach(entry -> {
        try (
          var response = App
            .getCurrent()
            .store.from("watching")
            .insert("id", "watching_id")
            .setValue("id", entry.id)
            .setValue("watching_id", entry.watchingId)
            .execute()
        ) {} catch (Exception error) {
          error.printStackTrace();
        }
      });

    this.progress.forEach(entry -> {
        try (
          var response = App
            .getCurrent()
            .store.from("progress")
            .insert("id", "progress")
            .setValue("id", entry.id)
            .setValue("progress", entry.progress)
            .execute()
        ) {} catch (Exception error) {
          error.printStackTrace();
        }
      });
  }

  public Session(SessionData data) {
    this.id = data.id;
    this.favorites = data.favorites;
    this.watching = data.watching;
    this.progress = data.progress;

    Session.current = this;
  }
}
