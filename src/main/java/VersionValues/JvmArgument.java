package VersionValues;

import java.util.LinkedList;
import java.util.List;

public class JvmArgument {
    private List<JvmArgumentRule> rules;
    private Object value;

    public JvmArgument(List<JvmArgumentRule> rules, Object value) {
        this.rules = rules;
        this.value = value;
    }

    public JvmArgument(String value) {
        this.rules = new LinkedList<>();
        this.value = value;
    }

    public List<JvmArgumentRule> getRules() { return rules; }

    public List<String> getValue() {
        if(value instanceof String) {
            List<String> values = new LinkedList<>();
            values.add((String) value);
            return values;
        }
        return (List<String>) value;
    }
}
