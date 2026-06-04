package client;

import common.ExitCodeCommand;
import common.commands.CommandRequest;
import common.interaction.Response;
import common.utility.GZIPUtils;
import common.utility.Serializer;

import java.io.IOException;
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

    private static final int BUFFER_SIZE = 262144;
    private static final int TIMEOUT_MS = 5000;
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 300;

    public UDPClient(String host, int port) {
        this.host = host;
        this.port = port;
        System.out.println("Клиент успешно запущен");
    }

    public void connect() throws IOException {
        if (channel != null && channel.isOpen()) channel.close();

        channel = DatagramChannel.open();
        channel.configureBlocking(false);
        channel.connect(new InetSocketAddress(host, port));

        if (selector != null) selector.close();
        selector = Selector.open();
        channel.register(selector, SelectionKey.OP_READ);
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

                channel.write(ByteBuffer.wrap(data));
                return receiveResponseFast();

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

                // Основная логика: сначала пробуем обычный ответ, потом — сжатый
                Response response = tryDeserialize(data);

                if (response == null) {
                    // Пробуем распаковать как GZIP
                    try {
                        byte[] decompressed = GZIPUtils.decompress(data);
                        response = tryDeserialize(decompressed);
                        if (response != null) {
                            System.out.println("Ответ распакован GZIP");
                        }
                    } catch (Exception ignored) {}
                }

                if (response != null) {
                    System.out.println("<- Ответ получен (" + data.length + " байт)");
                    return response;
                }
            }
        }
        return null;
    }

    // Вспомогательный метод
    private Response tryDeserialize(byte[] data) {
        try {
            return (Response) Serializer.deserialize(data);
        } catch (Exception e) {
            return null;
        }
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