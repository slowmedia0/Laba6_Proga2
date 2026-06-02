package client;

import common.ExitCodeCommand;
import common.commands.CommandRequest;
import common.interaction.Response;
import common.utility.Serializer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

public class UDPClient {

    private final DatagramChannel channel;
    private final Selector selector;
    private final SocketAddress serverAddress;

    private static final int BUFFER_SIZE = 1048576; // 1 MB
    private static final int TIMEOUT_MS = 30000;    // 30 секунд

    public UDPClient(String host, int port) throws IOException {
        this.serverAddress = new InetSocketAddress(host, port);

        this.channel = DatagramChannel.open();
        this.channel.configureBlocking(false);           // Требование ТЗ

        this.selector = Selector.open();
        this.channel.register(selector, SelectionKey.OP_READ);

        System.out.println("✅ Клиент запущен (DatagramChannel + Selector, неблокирующий)");
    }

    public Response sendRequest(CommandRequest command) {
        try {
            byte[] data = Serializer.serialize(command);
            ByteBuffer sendBuffer = ByteBuffer.wrap(data);
            channel.send(sendBuffer, serverAddress);

            System.out.println("→ Отправлена: " + command.getNameOfCommand());

            // Улучшенная обработка Selector
            long deadline = System.currentTimeMillis() + TIMEOUT_MS;

            while (System.currentTimeMillis() < deadline) {
                if (selector.select(2000) > 0) {   // проверяем каждые 2 секунды
                    ByteBuffer responseBuffer = ByteBuffer.allocate(BUFFER_SIZE);
                    SocketAddress sender = channel.receive(responseBuffer);

                    if (sender != null) {
                        responseBuffer.flip();
                        byte[] responseBytes = new byte[responseBuffer.remaining()];
                        responseBuffer.get(responseBytes);

                        System.out.println("← Получено " + responseBytes.length + " байт от сервера");

                        Response response = Serializer.deserialize(responseBytes);
                        return response;
                    }
                }
            }

            return new Response(ExitCodeCommand.ERROR, "Сервер не отвечает (таймаут " + TIMEOUT_MS + "мс)");

        } catch (Exception e) {
            e.printStackTrace();
            return new Response(ExitCodeCommand.ERROR, "Ошибка связи: " + e.getMessage());
        }
    }

    public void close() {
        try {
            selector.close();
            channel.close();
            System.out.println("Клиент завершает работу.");
        } catch (IOException e) {
            System.err.println("Ошибка при закрытии клиента: " + e.getMessage());
        }
    }
}