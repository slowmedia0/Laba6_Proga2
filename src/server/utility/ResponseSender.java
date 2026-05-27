package server.utility;

import common.interaction.Response;
import common.utility.Serializer;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * Модуль отправки ответов клиенту.
 * Соответствует требованиям лабораторной №6.
 */
public class ResponseSender {

    private static final int BUFFER_SIZE = 65536;

    /**
     * Отправляет ответ клиенту по UDP
     *
     * @param channel       DatagramChannel для отправки
     * @param clientAddress адрес клиента
     * @param response      объект ответа
     */
    public void sendResponse(DatagramChannel channel, SocketAddress clientAddress, Response response) {
        try {
            if (response == null) {
                response = new Response(false, "Сервер не смог сформировать ответ");
            }

            // Сериализация ответа в байты
            byte[] data = Serializer.serialize(response);
            ByteBuffer buffer = ByteBuffer.wrap(data);

            // Отправка ответа
            channel.send(buffer, clientAddress);

            System.out.println("Ответ отправлен клиенту " + clientAddress + " | Успех: " + response.isSuccess());

            if (response.getMessage() != null && !response.getMessage().isEmpty()) {
                System.out.println("Сообщение ответа: " + response.getMessage());
            }

        } catch (IOException e) {
            System.err.println("Ошибка отправки ответа клиенту " + clientAddress + ": " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Неожиданная ошибка при обработке ответа: " + e.getMessage());
        }
    }
}