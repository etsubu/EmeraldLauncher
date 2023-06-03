package VersionValues;

import com.google.gson.Gson;

import java.util.LinkedList;
import java.util.List;

public class GameArguments {
    private List<Object> game;
    private List<Object> jvm;

    public GameArguments(List<Object> game, List<Object> jvm) {
        this.game = game;
        this.jvm = jvm;
    }

    public List<JvmArgument> getJvmArguments() {
        List<JvmArgument> args = new LinkedList<>();
        for(Object o : jvm) {
            if(o instanceof String) {
                JvmArgument arg = new JvmArgument((String) o);
                args.add(arg);
            } else {
                args.add(new Gson().fromJson(new Gson().toJson(o), JvmArgument.class));
            }
        }
        return args;
    }

    public List<String> getGameArguments() {
        List<String> gameArgs = new LinkedList<>();
        for(Object o : game) {
            if(o instanceof String) {
                gameArgs.add((String)o);
            }
        }
        return gameArgs;
    }
}
