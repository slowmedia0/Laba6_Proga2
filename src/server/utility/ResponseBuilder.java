package server.utility;

import common.ExitCodeCommand;
import common.interaction.Response;

/**
 * ResponseBuilder — собирает весь вывод в одну строку.
 * Единственное поле — StringBuilder.
 */
public class ResponseBuilder {

    private static final StringBuilder output = new StringBuilder();

    // ==================== Методы добавления текста ====================

    public static void append(String text) {
        if (text != null && !text.trim().isEmpty()) {
            if (output.length() > 0) {
                output.append("\n");
            }
            output.append(text.trim());
        }
    }

    public static void appendLn(String text) {
        append(text);
        output.append("\n");
    }

    public static void appendError(String text) {
        if (text != null) {
            append("Ошибка: " + text);
        }
    }

    public static void appendSuccess(String text) {
        if (text != null) {
            append("Успешно: " + text);
        }
    }

    public static void appendTable(String left, String right) {
        if (left != null && right != null) {
            append(String.format("%-40s%s", left, right));
        }
    }

    public static String getOutput() {
        return output.toString().trim();
    }

    public static void clear() {
        output.delete(0, output.length());
    }
}