package views.auth;

public enum FormKeys {
  EMAIL("email"),
  PASSWORD("password");

  private final String label;

  @Override
  public final String toString() {
    return this.label;
  }

  private FormKeys(String label) {
    this.label = label;
  }
}
