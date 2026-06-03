package client;

import common.ExitCodeCommand;
import common.commands.CommandRequest;
import common.interaction.Response;
import common.utility.Serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.StreamCorruptedException;
import java.net.InetSocketAddress;
import java.net.PortUnreachableException;
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

    private static final int BUFFER_SIZE = 65536;
    private static final int TIMEOUT_MS = 3000;
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 300;

    private static final byte[] END_MARKER = {0x0A, 0x0B, 0x0C, 0x0D};

    public UDPClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void connect() throws IOException {
        if (channel != null && channel.isOpen()) channel.close();

        channel = DatagramChannel.open();
        channel.configureBlocking(false);
        channel.connect(new InetSocketAddress(host, port));

        if (selector != null) selector.close();
        selector = Selector.open();
        channel.register(selector, SelectionKey.OP_READ);

        System.out.println("Клиент подключён к " + host + ":" + port);
    }

    public Response sendRequest(CommandRequest request) {
        int attempts = 0;

        while (attempts < MAX_RETRIES) {
            try {
                if (channel == null || !channel.isConnected()) {
                    connect();
                }

                byte[] data = Serializer.serialize(request);
                System.out.println("-> [" + (attempts + 1) + "/" + MAX_RETRIES + "] "
                        + request.getNameOfCommand() + " (" + data.length + " байт)");

                if (data.length > BUFFER_SIZE - 2048) {
                    return sendWithChunks(data);
                } else {
                    channel.write(ByteBuffer.wrap(data));
                    return receiveResponseFast();
                }

            } catch (PortUnreachableException e) {
                attempts++;
                System.out.println("Сервер не отвечает. Попытка " + attempts + "/" + MAX_RETRIES);
            } catch (Exception e) {
                attempts++;
                System.out.println("Ошибка. Попытка " + attempts + "/" + MAX_RETRIES);
            }

            if (attempts < MAX_RETRIES) {
                try { Thread.sleep(RETRY_DELAY_MS); } catch (InterruptedException ignored) {}
            }
        }

        return new Response(ExitCodeCommand.ERROR, "Сервер временно недоступен.");
    }

    private Response sendWithChunks(byte[] data) throws IOException {
        int totalChunks = (data.length + BUFFER_SIZE - 1) / BUFFER_SIZE;
        System.out.println("   -> Чанкирование (" + totalChunks + " пакетов)");

        for (int i = 0; i < totalChunks; i++) {
            int offset = i * BUFFER_SIZE;
            int length = Math.min(BUFFER_SIZE, data.length - offset);

            ByteBuffer chunk = ByteBuffer.allocate(length + 4);
            chunk.putInt(i);
            chunk.put(data, offset, length);
            chunk.flip();
            channel.write(chunk);
        }

        channel.write(ByteBuffer.wrap(END_MARKER));
        return receiveResponseFast();
    }

    private Response receiveResponseFast() throws IOException {
        if (selector.select(TIMEOUT_MS) == 0) {
            System.out.println("Таймаут");
            return null;
        }

        Iterator<SelectionKey> it = selector.selectedKeys().iterator();
        while (it.hasNext()) {
            SelectionKey key = it.next();
            it.remove();

            if (key.isReadable()) {
                ByteBuffer buf = ByteBuffer.allocate(BUFFER_SIZE);
                channel.receive(buf);
                buf.flip();

                if (buf.remaining() == 0) continue;

                byte[] data = new byte[buf.remaining()];
                buf.get(data);

                if (data.length >= END_MARKER.length &&
                        java.util.Arrays.equals(
                                java.util.Arrays.copyOfRange(data, data.length - END_MARKER.length, data.length),
                                END_MARKER)) {

                    if (data.length > END_MARKER.length) {
                        byte[] cleanData = new byte[data.length - END_MARKER.length];
                        System.arraycopy(data, 0, cleanData, 0, cleanData.length);
                        data = cleanData;
                    } else {
                        data = new byte[0];
                    }
                }

                try {
                    Response response = (Response) Serializer.deserialize(data);
                    System.out.println("<- Ответ получен (" + data.length + " байт)");
                    return response;
                } catch (ClassNotFoundException e) {
                    System.err.println("Ошибка десериализации: Несовместимость версий");
                } catch (InvalidClassException e) {
                    System.err.println("Ошибка десериализации: Несовместимость классов");
                } catch (StreamCorruptedException e) {
                    System.err.println("Ошибка десериализации: Данные повреждены");
                } catch (Exception e) {
                    System.err.println("Ошибка десериализации");
                }
            }
        }
        return null;
    }

    public void close() {
        try {
            if (selector != null) selector.close();
            if (channel != null) channel.close();
        } catch (IOException e) {
            System.out.println("Ошибка закрытия клиента");
        }
    }
}