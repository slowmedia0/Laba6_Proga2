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
     * Главный метод обработки запроса — вызывается из UDPServer
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

            ResponseBuilder.clear(); // очищаем перед каждой командой

            if ("load_file".equals(cmd)) {
                response = handleLoadFile(request, console);
            }
            else {
                response = processCommand(request, console);

                if ("exit".equalsIgnoreCase(cmd)) {
                    System.out.println("Клиент с адресом " + clientAddress + " отключился! Ожидаю новые подключения");
                }
            }

            // Отправляем ответ
            ResponseSender.sendResponse(channel, clientAddress, response);

        } catch (Exception e) {
            ResponseBuilder.clear();
            ResponseBuilder.appendError("Ошибка обработки запроса: " + e.getMessage());
            Response response = new Response(ExitCodeCommand.ERROR, ResponseBuilder.getOutput());

            // Попытка отправить ошибку клиенту
            try {
                ResponseSender.sendResponse(channel, clientAddress, response);
            } catch (Exception ignored) {}

            e.printStackTrace();
        }
    }

    // ==================== Вспомогательные методы ====================

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
                ResponseBuilder.append("Файл успешно загружен на сервер");
            } else {
                ResponseBuilder.appendError("Не удалось загрузить файл");
            }

            return new Response(result, ResponseBuilder.getOutput());

        } catch (Exception e) {
            ResponseBuilder.appendError("Ошибка загрузки файла: " + e.getMessage());
            return new Response(ExitCodeCommand.ERROR, ResponseBuilder.getOutput());
        }
    }

    private static Response processCommand(CommandRequest request, Console console) {
        try {
            String commandName = request.getNameOfCommand();
            String argument = request.getArgument() != null ? request.getArgument().toString() : "";

            ExitCodeCommand result;

            if (request.getVehicle() != null) {
                result = console.launchCommand(commandName, argument, request.getVehicle(), null, null);
            } else {
                result = console.launchCommand(commandName, argument);
            }

            // Сортировка (оставлено как было)
            if (commandName.equals("show")) {
                console.sortCollectionIfNeeded(commandName);
            }

            String message = (result == ExitCodeCommand.OK)
                    ? "Команда выполнена успешно."
                    : "Команда выполнена с ошибками.";

            ResponseBuilder.append(message);

            return new Response(result, ResponseBuilder.getOutput());

        } catch (Exception e) {
            ResponseBuilder.appendError("Ошибка выполнения команды: " + e.getMessage());
            return new Response(ExitCodeCommand.ERROR, ResponseBuilder.getOutput());
        }
    }
}