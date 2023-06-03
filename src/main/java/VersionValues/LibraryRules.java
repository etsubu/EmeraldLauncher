package VersionValues;

public class LibraryRules {
    private final String action;
    private final LibraryOs os;

    public LibraryRules(String action, LibraryOs os) {
        this.action = action;
        this.os = os;
    }

    public String getAction() { return action; }

    public LibraryOs getOs() { return os; }
}
