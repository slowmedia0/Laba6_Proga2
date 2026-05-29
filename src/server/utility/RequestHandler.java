package server.utility;

import common.commands.CommandRequest;
import common.interaction.Response;
import common.utility.Serializer;
import common.ExitCodeCommand;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.Selector;

public class RequestHandler {

    private static final int BUFFER_SIZE = 65536;

    /**
     * Главный метод обработки запроса
     */
    public static void handleRequest(DatagramChannel channel, Selector selector,
                                     Console console, FileManager fileManager) {

        SocketAddress clientAddress = null;
        try {
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
            clientAddress = channel.receive(buffer);

            if (clientAddress == null) return;

            buffer.flip();
            byte[] requestBytes = new byte[buffer.remaining()];
            buffer.get(requestBytes);

            CommandRequest request = Serializer.deserialize(requestBytes);

            System.out.println("← Получена команда: " + request.getNameOfCommand() + " от " + clientAddress);

            Response response;
            String cmd = request.getNameOfCommand();

            // Очищаем builder перед каждой новой командой
            ResponseBuilder.clear();

            if ("load_file".equals(cmd)) {
                response = handleLoadFile(request, console);
            }
            else if ("exit".equalsIgnoreCase(cmd)) {
                response = handleExit(request, console, fileManager);
            }
            else {
                response = processCommand(request, console);
            }

            // Отправляем ответ
            ResponseSender.sendResponse(channel, clientAddress, response);

        } catch (Exception e) {
            ResponseBuilder.clear();
            ResponseBuilder.appendError("Критическая ошибка сервера: " + e.getMessage());

            Response errorResponse = new Response(ExitCodeCommand.ERROR, ResponseBuilder.getOutput());

            try {
                if (clientAddress != null) {
                    ResponseSender.sendResponse(channel, clientAddress, errorResponse);
                }
            } catch (Exception ignored) {}

            e.printStackTrace();
        }
    }

    // ==================== Обработчики команд ====================

    private static Response handleLoadFile(CommandRequest request, Console console) {
        try {
            String fileName = request.getFileName();
            byte[] fileData = request.getFileData();

            if (fileName == null || fileData == null) {
                ResponseBuilder.appendError("Не переданы данные файла");
                return new Response(ExitCodeCommand.ERROR, ResponseBuilder.getOutput());
            }

            ExitCodeCommand result = console.loadCollectionFromBytes(fileName, fileData);

            if (result == ExitCodeCommand.OK) {
                ResponseBuilder.append("Файл успешно загружен и коллекция обновлена.");
            } else {
                ResponseBuilder.appendError("Не удалось загрузить файл.");
            }

            return new Response(result, ResponseBuilder.getOutput());

        } catch (Exception e) {
            ResponseBuilder.appendError("Ошибка при загрузке файла: " + e.getMessage());
            return new Response(ExitCodeCommand.ERROR, ResponseBuilder.getOutput());
        }
    }

    private static Response handleExit(CommandRequest request, Console console, FileManager fileManager) {
        try {
            ResponseBuilder.append("Сервер завершает работу...");

            boolean saved = fileManager.writeCollection();
            console.sortCollectionIfNeeded("exit");           // сортировка перед отправкой
            byte[] fileData = fileManager.getCollectionAsBytes();

            if (saved) {
                ResponseBuilder.append("Коллекция успешно сохранена.");
            } else {
                ResponseBuilder.appendError("Не удалось сохранить коллекцию в файл!");
            }

            Response response = new Response(
                    saved ? ExitCodeCommand.OK : ExitCodeCommand.ERROR,
                    ResponseBuilder.getOutput()
            );

            response.setFileData(fileData);
            response.setFileName(console.getLoadFileName());

            System.out.println("Клиент отключился. Ожидаю новые подключения...");
            return response;

        } catch (Exception e) {
            ResponseBuilder.appendError("Ошибка при завершении работы: " + e.getMessage());
            return new Response(ExitCodeCommand.ERROR, ResponseBuilder.getOutput());
        }
    }

    private static Response processCommand(CommandRequest request, Console console) {
        try {
            String commandName = request.getNameOfCommand();
            String argument = request.getArgument() != null ? request.getArgument().toString() : "";

            ExitCodeCommand result;

            if (request.getVehicle() != null) {
                result = console.launchCommand(commandName, argument, request.getVehicle(), null,null);
            } else {
                result = console.launchCommand(commandName, argument);
            }

            // Сортировка после изменяющих команд
            if (!commandName.equals("show") && !commandName.equals("info")) {
                console.sortCollectionIfNeeded(commandName);
            }

            String statusMessage = (result == ExitCodeCommand.OK)
                    ? "Команда выполнена успешно."
                    : "Команда выполнена с ошибками.";

            ResponseBuilder.append(statusMessage);

            Response response = new Response(result, ResponseBuilder.getOutput());
            System.out.println(ResponseBuilder.getOutput());
            return response;
        } catch (Exception e) {
            ResponseBuilder.appendError("Ошибка выполнения команды '" + request.getNameOfCommand() + "': " + e.getMessage());
            return new Response(ExitCodeCommand.ERROR, ResponseBuilder.getOutput());
        }
    }
}