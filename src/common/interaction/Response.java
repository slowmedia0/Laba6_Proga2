package common.interaction;

import common.ExitCodeCommand;

import java.io.Serializable;

public class Response implements Serializable {

    private static final long serialVersionUID = 1L;

    private final ExitCodeCommand status;
    private final String message;
    private final String commandName;
    private final String fileName;      // имя файла
    private final byte[] fileData;      // содержимое файла (для exit/save)

    // ==================== Конструкторы ====================

    public Response(ExitCodeCommand status, String message) {
        this(status, message, null, null, null);
    }

    public Response(ExitCodeCommand status, String message, String commandName) {
        this(status, message, commandName, null, null);
    }

    public Response(ExitCodeCommand status, String message, String fileName, byte[] fileData) {
        this(status, message, null, fileName, fileData);
    }

    public Response(ExitCodeCommand status, String message, String commandName,
                    String fileName, byte[] fileData) {
        this.status = status;
        this.message = message;
        this.commandName = commandName;
        this.fileName = fileName;
        this.fileData = fileData;
    }

    // ==================== Геттеры ====================

    public ExitCodeCommand getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getCommandName() {
        return commandName;
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getFileData() {
        return fileData;
    }

    public boolean isSuccess() {
        return status == ExitCodeCommand.OK;
    }

    @Override
    public String toString() {
        return "Response{status=" + status + ", message='" + message +
                "', fileName=" + (fileName != null ? fileName : "null") + "}";
    }
}