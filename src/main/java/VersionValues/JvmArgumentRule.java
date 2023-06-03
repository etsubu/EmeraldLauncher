package VersionValues;

public class JvmArgumentRule {
    private final String action;
    private final OperatingSystem os;

    public JvmArgumentRule(String action, OperatingSystem os) {
        this.action = action;
        this.os = os;
    }

    public String getAction() { return action; }

    public OperatingSystem getOs() { return os; }
}
