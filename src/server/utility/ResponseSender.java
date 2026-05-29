package server.utility;

import common.interaction.Response;
import common.utility.Serializer;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class ResponseSender {

    private static final int BUFFER_SIZE = 524288; // 512 KB

    public static void sendResponse(DatagramChannel channel, SocketAddress clientAddress, Response response) {
        if (clientAddress == null || response == null) {
            System.err.println("❌ ResponseSender: null address or response");
            return;
        }

        try {
            byte[] data = Serializer.serialize(response);

            System.out.println("📦 Сериализовано " + data.length + " байт для отправки");

            ByteBuffer buffer = ByteBuffer.wrap(data);
            int sent = channel.send(buffer, clientAddress);

            System.out.println("✅ ОТПРАВЛЕНО клиенту! (" + sent + " байт) | " +
                    response.getMessage().substring(0, Math.min(80, response.getMessage().length())));

        } catch (IOException e) {
            System.err.println("❌ Ошибка отправки UDP: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Ошибка сериализации: " + e.getMessage());
            e.printStackTrace();
        }
    }
}