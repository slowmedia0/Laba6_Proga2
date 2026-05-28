package common.interaction;

import common.ExitCodeCommand;
import common.models.Vehicle;

import java.io.Serializable;
import java.util.List;

/**
 * Класс ответа от сервера клиенту
 */
public class Response implements Serializable {
    private static final long serialVersionUID = 1L;

    private ExitCodeCommand exitCode;
    private String message;
    private String fileName;
    private byte[] fileData;

    // ==================== Конструкторы ====================

    public Response(ExitCodeCommand exitCode, String message) {
        this.exitCode = exitCode;
        this.message = message;
    }

    public Response(ExitCodeCommand exitCode, String message, String fileName, byte[] fileData) {
        this.exitCode = exitCode;
        this.message = message;
        this.fileName = fileName;
        this.fileData = fileData;
    }

    public Response(String message) {
        this(ExitCodeCommand.OK, message);
    }

    // ==================== Геттеры и Сеттеры ====================

    public ExitCodeCommand getExitCode() {
        return exitCode;
    }

    public String getMessage() {
        return message;
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getFileData() {
        return fileData;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFileData(byte[] fileData) {
        this.fileData = fileData;
    }

    public boolean isSuccess() {
        return exitCode == ExitCodeCommand.OK;
    }

    @Override
    public String toString() {
        return "Response{exitCode=" + exitCode + ", message='" + message + "'}";
    }

}