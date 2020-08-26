package VersionValues;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class JvmArgument {
    private final List<JvmArgumentRule> rules;
    private final Object value;

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
        } else if(value instanceof List<?>) {
            // Elements will be copied to avoid List typecasting warnings
            List<?> list = (List<?>) value;
            if(list.isEmpty()) {
                return new ArrayList<>();
            }
            if(!(list.get(0) instanceof String)) {
                throw new RuntimeException("First jvm argument contained non string value");
            }
            List<String> strList = new ArrayList<>(list.size());
            list.forEach(x -> strList.add(x.toString()));
            return strList;
        }
        throw new RuntimeException("Jvm argument contained non string values");
    }
}
