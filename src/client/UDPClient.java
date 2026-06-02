package client;

import common.commands.CommandRequest;
import common.interaction.Response;
import common.utility.Serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

public class UDPClient {

    private final String host;
    private final int port;
    private DatagramChannel channel;
    private Selector selector;

    private static final int BUFFER_SIZE = 8192;        // размер одного чанка
    private static final int TIMEOUT_MS = 8000;         // увеличил таймаут
    private static final byte[] END_MARKER = {0x0A, 0x0B, 0x0C, 0x0D}; // маркер конца

    public UDPClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void connect() throws IOException {
        channel = DatagramChannel.open();
        channel.configureBlocking(false);
        channel.connect(new InetSocketAddress(host, port));

        selector = Selector.open();
        channel.register(selector, SelectionKey.OP_READ);

        System.out.println("Клиент подключён к " + host + ":" + port);
    }

    /**
     * Отправка запроса с поддержкой чанкирования
     */
    public Response sendRequest(CommandRequest request) {
        try {
            byte[] data = Serializer.serialize(request);

            if (data.length + END_MARKER.length < BUFFER_SIZE - 200) {
                System.out.println("→ Отправлена команда: " + request.getNameOfCommand()
                        + " (" + data.length + " байт)");
                return sendSinglePacket(data);
            } else {
                System.out.println("→ Отправка большого сообщения: " + request.getNameOfCommand()
                        + " (" + data.length + " байт)");
                return sendWithChunks(data);
            }
        } catch (Exception e) {
            System.out.println("   Ошибка при отправке запроса: " + e.getMessage());
            return null;
        }
    }

    private Response sendSinglePacket(byte[] data) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        channel.write(buffer);

        System.out.println("→ Отправлена команда: " + /*request.getNameOfCommand()*/ "(одним пакетом, " + data.length + " байт)");

        return receiveResponse();
    }

    private Response sendWithChunks(byte[] data) throws IOException {
        int totalChunks = (data.length + BUFFER_SIZE - 1) / BUFFER_SIZE;
        System.out.println("→ Отправка большого сообщения (" + data.length + " байт) в " + totalChunks + " чанках");

        for (int i = 0; i < totalChunks; i++) {
            int offset = i * BUFFER_SIZE;
            int length = Math.min(BUFFER_SIZE, data.length - offset);

            ByteBuffer chunk = ByteBuffer.allocate(length + 4);
            chunk.putInt(i);                    // номер чанка
            chunk.put(data, offset, length);
            chunk.flip();

            channel.write(chunk);
        }

        // Отправляем маркер конца
        ByteBuffer endBuffer = ByteBuffer.wrap(END_MARKER);
        channel.write(endBuffer);

        return receiveResponse();
    }

    /**
     * Получение ответа (с поддержкой чанков)
     */
    /**
     * Получение ответа (с поддержкой чанков)
     */
    private Response receiveResponse() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        long startTime = System.currentTimeMillis();

        while (System.currentTimeMillis() - startTime < TIMEOUT_MS) {
            if (selector.select(500) == 0) {
                continue;
            }

            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();

                if (key.isReadable()) {
                    ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
                    SocketAddress addr = channel.receive(buffer);

                    if (addr != null) {
                        buffer.flip();
                        byte[] received = new byte[buffer.remaining()];
                        buffer.get(received);

                        // Проверка маркера конца передачи
                        if (received.length == END_MARKER.length &&
                                java.util.Arrays.equals(received, END_MARKER)) {
                            break;
                        }

                        baos.write(received);
                    }
                }
            }
        }

        byte[] fullData = baos.toByteArray();

        if (fullData.length == 0) {
            System.out.println("   Таймаут: сервер не ответил");
            return null;
        }

        // === ИСПРАВЛЕННАЯ ДЕСЕРИАЛИЗАЦИЯ ===
        try {
            Object deserialized = Serializer.deserialize(fullData);

            if (deserialized instanceof Response) {
                return (Response) deserialized;
            } else if (deserialized == null) {
                System.out.println("   Ошибка: получен null вместо Response");
                return null;
            } else {
                System.out.println("   Ошибка: получен объект неверного типа: "
                        + deserialized.getClass().getName());
                return null;
            }
        } catch (Exception e) {
            System.out.println("   Ошибка десериализации ответа: " + e.getMessage());
            return null;
        }
    }

    public void close() {
        try {
            if (channel != null) channel.close();
            if (selector != null) selector.close();
        } catch (IOException e) {
            System.err.println("Ошибка закрытия клиента: " + e.getMessage());
        }
    }
}