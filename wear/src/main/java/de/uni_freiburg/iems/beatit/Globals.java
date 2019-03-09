package de.uni_freiburg.iems.beatit;

public class Globals {
    private static Globals instance;

    private final boolean DebugMode = false;

    private Globals() {}

    public static synchronized Globals getInstance() {
        if(instance == null) {
            instance = new Globals();
        }
        return instance;
    }

    public boolean isDebugMode() {
        return DebugMode;
    }
}
