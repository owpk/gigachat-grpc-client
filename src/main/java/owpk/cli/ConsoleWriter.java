package owpk.cli;

import picocli.CommandLine;

public class ConsoleWriter {
    private static final String PATTERN = "@|fg(%s) %s|@";

    public static void write(String msg, Color color) {
        defaultWrite(msg, color);
    }

    public static void writeLn(String msg, Color color) {
        defaultWrite(msg + "\n", color);
    }

    public static void write(String msg) {
        defaultWrite(msg, Color.NO_COLOR);
    }

    public static void writeLn(String msg) {
        defaultWrite(msg + "\n", Color.NO_COLOR);
    }

    private static void defaultWrite(String msg, Color color) {
        System.out.print(colorize(msg, color));
    }

    public static String colorize(String msg, Color color) {
        if (color == Color.NO_COLOR)
            return msg;
        return CommandLine.Help.Ansi.AUTO.string(String.format(PATTERN, color.getColor(), msg));
    }

}