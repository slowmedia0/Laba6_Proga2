package server.utility;

import common.commands.CommandRequest;
import common.interaction.Response;
import common.utility.ResponseBuilder;
import common.utility.Serializer;
import common.ExitCodeCommand;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.Selector;

public class RequestHandler {

    private static final int BUFFER_SIZE = 262144;

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

            System.out.println("<- Получен запрос: " + request.getNameOfCommand() + " от " + clientAddress);

            Response response = processCommand(request, console);

            // Просто передаём ответ дальше (сжатие будет в ResponseSender)
            ResponseSender.sendResponse(channel, clientAddress, response);

        } catch (Exception e) {
            ResponseBuilder.appendLn("Критическая ошибка сервера: " + e.getMessage());

            Response errorResponse = new Response(ExitCodeCommand.ERROR, ResponseBuilder.getOutput());

            try {
                if (clientAddress != null) {
                    ResponseSender.sendResponse(channel, clientAddress, errorResponse);
                }
            } catch (Exception ignored) {}

            e.printStackTrace();
        }
        ResponseBuilder.clear();
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

            String statusMessage = (result == ExitCodeCommand.OK | result == ExitCodeCommand.EXIT)
                    ? "Команда выполнена успешно."
                    : "Команда выполнена с ошибками.";

            ResponseBuilder.append(statusMessage);

            Response response = new Response(result, ResponseBuilder.getOutput());
            System.out.println(ResponseBuilder.getOutput());
            return response;

        } catch (Exception e) {
            ResponseBuilder.appendLn("Ошибка выполнения команды '" + request.getNameOfCommand() + "': " + e.getMessage());
            return new Response(ExitCodeCommand.ERROR, ResponseBuilder.getOutput());
        }
    }
}