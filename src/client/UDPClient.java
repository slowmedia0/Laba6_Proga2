package client;

import common.ExitCodeCommand;
import common.commands.CommandRequest;
import common.interaction.ChunkedResponse;
import common.interaction.Response;
import common.utility.Serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.HashMap;
import java.util.Map;

public class UDPClient {

    private final DatagramChannel channel;
    private final Selector selector;
    private final SocketAddress serverAddress;

    private static final int BUFFER_SIZE = 262144; // 256 KB
    private static final int TIMEOUT_MS = 60000;

    public UDPClient(String host, int port) throws IOException {
        this.serverAddress = new java.net.InetSocketAddress(host, port);

        this.channel = DatagramChannel.open();
        this.channel.configureBlocking(false);

        this.selector = Selector.open();
        this.channel.register(selector, SelectionKey.OP_READ);

        System.out.println("✅ Клиент запущен (Неблокирующий режим + Selector)");
    }

    public Response sendRequest(CommandRequest command) {
        if (command == null) {
            return new Response(ExitCodeCommand.ERROR, "Пустой запрос");
        }

        try {
            byte[] data = Serializer.serialize(command);
            channel.send(ByteBuffer.wrap(data), serverAddress);

            System.out.println("→ Отправлена: " + command.getNameOfCommand() + " (" + data.length + " байт)");

            Map<Integer, byte[]> chunks = new HashMap<>();
            int totalChunks = -1;
            long startTime = System.currentTimeMillis();

            while (System.currentTimeMillis() - startTime < TIMEOUT_MS) {

                if (selector.select(2500) > 0) {

                    ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
                    SocketAddress from = channel.receive(buffer);

                    if (from != null) {
                        buffer.flip();
                        byte[] packet = new byte[buffer.remaining()];
                        buffer.get(packet);

                        System.out.println("   ← Получен пакет (" + packet.length + " байт)");

                        try {
                            Object obj = Serializer.deserialize(packet);

                            if (obj instanceof Response) {
                                System.out.println("   УСПЕХ: получен Response");
                                return (Response) obj;
                            }
                            else if (obj instanceof ChunkedResponse) {
                                ChunkedResponse cr = (ChunkedResponse) obj;
                                chunks.put(cr.getChunkIndex(), cr.getData());
                                if (totalChunks == -1) totalChunks = cr.getTotalChunks();

                                System.out.println("   Чанк " + (cr.getChunkIndex() + 1) + "/" + totalChunks);

                                if (chunks.size() == totalChunks) {
                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    for (int i = 0; i < totalChunks; i++) {
                                        baos.write(chunks.get(i));
                                    }
                                    Response response = Serializer.deserialize(baos.toByteArray());
                                    System.out.println("   УСПЕХ: ответ собран (" + baos.size() + " байт)");
                                    return response;
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("   Ошибка десериализации: " + e.getMessage());
                        }
                    }
                }
            }

            return new Response(ExitCodeCommand.ERROR, "Сервер не ответил (таймаут)");

        } catch (Exception e) {
            e.printStackTrace();
            return new Response(ExitCodeCommand.ERROR, "Ошибка соединения: " + e.getMessage());
        }
    }

    public void close() {
        try {
            selector.close();
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}