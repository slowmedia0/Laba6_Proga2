package server.utility;

import common.interaction.ChunkedResponse;
import common.interaction.Response;
import common.utility.Serializer;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class ResponseSender {

    private static final int MAX_UDP_SIZE = 65000;
    private static final int CHUNK_SIZE = 6500;

    public static void sendResponse(DatagramChannel channel, SocketAddress clientAddress, Response response) {
        try {
            Thread.sleep(15); // критически важная задержка

            byte[] data = Serializer.serialize(response);

            if (data.length <= MAX_UDP_SIZE) {
                channel.send(ByteBuffer.wrap(data), clientAddress);
                System.out.println("Отправлен ответ (" + data.length + " байт)");
            } else {
                int totalChunks = (data.length + CHUNK_SIZE - 1) / CHUNK_SIZE;

                for (int i = 0; i < totalChunks; i++) {
                    int offset = i * CHUNK_SIZE;
                    int length = Math.min(CHUNK_SIZE, data.length - offset);

                    byte[] chunkData = new byte[length];
                    System.arraycopy(data, offset, chunkData, 0, length);

                    ChunkedResponse chunk = new ChunkedResponse(totalChunks, i, chunkData);
                    byte[] serialized = Serializer.serialize(chunk);

                    channel.send(ByteBuffer.wrap(serialized), clientAddress);
                    Thread.sleep(5);
                }
                System.out.println("Отправлено " + totalChunks + " чанков (" + data.length + " байт)");
            }

        } catch (Exception e) {
            System.err.println("Ошибка отправки ответа: " + e.getMessage());
            e.printStackTrace();
        }
    }
}