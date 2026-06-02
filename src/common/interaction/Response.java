package common.interaction;

import common.ExitCodeCommand;

import java.io.Serializable;
import java.util.Base64;

public class Response implements Serializable {

    private static final long serialVersionUID = 1L;

    private final ExitCodeCommand status;
    private final String message;
    private final String commandName;
    private final String fileName;
    private final String fileData;   // ← теперь String (Base64)

    public Response(ExitCodeCommand status, String message) {
        this(status, message, null, null, null);
    }

    public Response(ExitCodeCommand status, String message, String commandName) {
        this(status, message, commandName, null, null);
    }

    public Response(ExitCodeCommand status, String message, String fileName, String fileData) {
        this(status, message, null, fileName, fileData);
    }

    public Response(ExitCodeCommand status, String message, String commandName,
                    String fileName, String fileData) {
        this.status = status;
        this.message = message != null ? message : "";
        this.commandName = commandName;
        this.fileName = fileName;
        this.fileData = fileData;
    }

    public ExitCodeCommand getStatus() { return status; }
    public String getMessage() { return message; }
    public String getCommandName() { return commandName; }
    public String getFileName() { return fileName; }
    public String getFileData() { return fileData; }

    // Удобный метод для получения байтов
    public byte[] getFileDataAsBytes() {
        if (fileData == null || fileData.isEmpty()) return null;
        return Base64.getDecoder().decode(fileData);
    }

    public boolean isSuccess() {
        return status == ExitCodeCommand.OK || status == ExitCodeCommand.EXIT;
    }
}