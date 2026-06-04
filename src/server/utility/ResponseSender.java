package server.utility;

import common.interaction.Response;
import common.utility.GZIPUtils;
import common.utility.Serializer;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class ResponseSender {

    private static final int MAX_UDP_SIZE = 65000;
    private static final int COMPRESS_THRESHOLD = 8192;

    public static void sendResponse(DatagramChannel channel, SocketAddress clientAddress, Response response) {
        try {
            Thread.sleep(15); // небольшая задержка для стабильности

            byte[] data = Serializer.serialize(response);

            // === GZIP СЖАТИЕ ТОЛЬКО ДЛЯ БОЛЬШИХ ОТВЕТОВ ===
            if (data.length > COMPRESS_THRESHOLD) {
                data = GZIPUtils.compress(data);
                System.out.println("→ Ответ сжат GZIP (" + data.length + " байт | было " + Serializer.serialize(response).length + ")");
            }

            // Простая отправка (без чанков)
            if (data.length > MAX_UDP_SIZE) {
                System.out.println("Предупреждение: Ответ слишком большой (" + data.length + " байт), может быть потерян");
            }

            channel.send(ByteBuffer.wrap(data), clientAddress);
            System.out.println("Отправлен ответ (" + data.length + " байт)");

        } catch (Exception e) {
            System.out.println("Ошибка отправки ответа: " + e.getMessage());
            e.printStackTrace();
        }
    }
}