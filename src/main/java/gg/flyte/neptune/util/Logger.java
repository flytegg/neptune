package gg.flyte.neptune.util;

public final class Logger {
    public void debug(String... strings) {
        for (String string : strings) System.out.println("[Neptune] DEBUG: " + string);
    }

    public void info(String... strings) {
        for (String string : strings) System.out.println("[Neptune] INFO: " + string);
    }

    public void error(String... strings) {
        for (String string : strings) System.out.println("[Neptune] ERROR: " + string);
    }
}
