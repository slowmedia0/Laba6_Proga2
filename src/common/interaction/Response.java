package common.interaction;

import common.models.Vehicle;

import java.io.Serializable;
import java.util.List;

/**
 * Класс ответа сервера клиенту.
 * Используется для передачи результата выполнения команды.
 */
public class Response implements Serializable {

    private static final long serialVersionUID = 1L;

    private final boolean success;           // Успешно ли выполнена команда
    private final String message;            // Основное сообщение
    private final List<Vehicle> data;        // Данные (например, для show, print_*)
    private final String commandName;        // Опционально: имя команды, которая выполнялась

    // ==================== Конструкторы ====================

    public Response(boolean success, String message) {
        this(success, message, null, null);
    }

    public Response(boolean success, String message, List<Vehicle> data) {
        this(success, message, data, null);
    }

    public Response(boolean success, String message, List<Vehicle> data, String commandName) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.commandName = commandName;
    }

    // ==================== Геттеры ====================

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public List<Vehicle> getData() {
        return data;
    }

    public String getCommandName() {
        return commandName;
    }

    // ==================== Удобные статические фабрики ====================

    public static Response ok(String message) {
        return new Response(true, message);
    }

    public static Response ok(String message, List<Vehicle> data) {
        return new Response(true, message, data);
    }

    public static Response error(String message) {
        return new Response(false, message);
    }

    // ==================== toString для отладки ====================

    @Override
    public String toString() {
        return "Response{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", dataSize=" + (data != null ? data.size() : 0) +
                '}';
    }


}