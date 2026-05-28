package server.utility;

import common.ExitCodeCommand;
import common.interaction.Response;
import common.utility.Serializer;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class ResponseSender {

    private static final int BUFFER_SIZE = 65536;

    /**
     * Отправка ответа клиенту
     */
    public static void sendResponse(DatagramChannel channel, SocketAddress clientAddress, Response response) {
        try {
            if (response == null) {
                response = new Response(ExitCodeCommand.ERROR, "Внутренняя ошибка сервера");
            }

            byte[] data = Serializer.serialize(response);
            ByteBuffer buffer = ByteBuffer.wrap(data);

            channel.send(buffer, clientAddress);

            System.out.println("→ Ответ отправлен клиенту " + clientAddress);

        } catch (IOException e) {
            System.err.println("Ошибка отправки ответа клиенту: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Критическая ошибка сериализации ответа: " + e.getMessage());
        }
    }
}