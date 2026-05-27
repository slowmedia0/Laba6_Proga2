package server.utility;

import common.ExitCodeCommand;
import common.commands.CommandRequest;
import common.interaction.Response;
import common.models.Vehicle;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class RequestHandler {

    private final Console console;
    private final FileManager fileManager;

    public RequestHandler(Console console, FileManager fileManager) {
        this.console = console;
        this.fileManager = fileManager;
    }

    public Response handle(CommandRequest request) {
        try {
            String cmd = request.getNameOfCommand().toLowerCase().trim();
            String arg = request.getArgument() != null ? request.getArgument() : "";
            Vehicle vehicle = request.getVehicle();
            File fileArg = request.getFile();

            ExitCodeCommand result;

            if (vehicle != null || fileArg != null) {
                result = console.launchCommand(cmd, arg, vehicle, fileArg);
            } else {
                result = console.launchCommand(cmd, arg);
            }

            // Специальная обработка exit
            if ("exit".equals(cmd)) {
                File savedFile = fileManager.getCurrentFile();
                byte[] fileData = null;
                String fileName = null;

                if (savedFile != null && savedFile.exists()) {
                    fileName = savedFile.getName();
                    try {
                        fileData = Files.readAllBytes(savedFile.toPath());
                    } catch (IOException ignored) {}
                }

                return new Response(ExitCodeCommand.EXIT,
                        "Коллекция успешно сохранена на сервере.",
                        "exit", fileName, fileData);
            }

            String message = (result == ExitCodeCommand.OK || result == ExitCodeCommand.SUCCESS)
                    ? "Команда выполнена успешно."
                    : "Команда выполнена с ошибками.";

            return new Response(result, message, cmd);

        } catch (Exception e) {
            return new Response(ExitCodeCommand.ERROR, "Ошибка: " + e.getMessage());
        }
    }
}