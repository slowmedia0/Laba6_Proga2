package server.utility;

import common.ExitCodeCommand;
import common.commands.CommandRequest;
import common.interaction.Response;
import common.utility.Serializer;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;

/**
 * Модуль чтения запроса и обработки команд.
 * Десериализует CommandRequest и передаёт его в Console.
 */
public class RequestHandler {

    private final Console console;
    private final ResponseSender responseSender;

    public RequestHandler(Console console, ResponseSender responseSender) {
        this.console = console;
        this.responseSender = responseSender;
    }

    /**
     * Основной метод обработки запроса от клиента
     */
    public void handleRequest(DatagramChannel channel, SocketAddress clientAddress, byte[] data) {
        try {
            CommandRequest commandRequest = Serializer.deserialize(data);

            if (commandRequest == null) {
                sendError(channel, clientAddress, "Не удалось десериализовать запрос");
                return;
            }

            String mnemonics = commandRequest.getNameOfCommand();
            String argument = commandRequest.getArgument() != null ? commandRequest.getArgument() : "";

            // Выполняем команду
            ExitCodeCommand result = console.launchCommand(
                    mnemonics,
                    argument,
                    commandRequest.getVehicle(),
                    commandRequest.getFile()
            );

            // Вывод результата выполнения на сервере
            if (!result.equals(ExitCodeCommand.OK)) {
                if (mnemonics.equals("execute_script")) {
                    System.out.println("Не удалось выполнить без ошибок команду " + mnemonics + " " + argument + "!");
                } else {
                    System.out.println("Не удалось выполнить команду " + mnemonics + " " + argument + "!");
                }
            } else {
                System.out.println("Команда " + mnemonics + " выполнена успешно.");
            }

            // Отправка ответа клиенту
            boolean success = (result == ExitCodeCommand.OK || result == ExitCodeCommand.EXIT);
            Response response = new Response(success,
                    success ? "Команда выполнена успешно" : "Команда выполнена с ошибками");

            responseSender.sendResponse(channel, clientAddress, response);

        } catch (Exception e) {
            System.err.println("Ошибка при обработке запроса: " + e.getMessage());
            sendError(channel, clientAddress, "Внутренняя ошибка сервера");
        }
    }

    private void sendError(DatagramChannel channel, SocketAddress clientAddress, String message) {
        Response errorResponse = new Response(false, message);
        responseSender.sendResponse(channel, clientAddress, errorResponse);
    }
}