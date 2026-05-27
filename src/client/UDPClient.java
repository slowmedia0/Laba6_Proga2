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

    private static final int BUFFER_SIZE = 65536;
    private static final int TIMEOUT_MS = 8000; // 8 секунд

    public UDPClient(String host, int port) throws IOException {
        this.serverAddress = new InetSocketAddress(host, port);

        this.channel = DatagramChannel.open();
        this.channel.configureBlocking(false);
        this.channel.connect(serverAddress);

        this.selector = Selector.open();
        this.channel.register(selector, SelectionKey.OP_READ);

        System.out.println("✅ Клиент подключён к " + host + ":" + port);
    }

    public Response sendRequest(CommandRequest command) {
        try {
            if (command == null) {
                return new Response(ExitCodeCommand.ERROR, "Не удалось создать запрос");
            }

            // Сериализация и отправка
            byte[] data = Serializer.serialize(command);
            ByteBuffer buffer = ByteBuffer.wrap(data);
            channel.write(buffer);

            System.out.println("→ Отправлена команда: " + command.getNameOfCommand());

            // Ожидание ответа
            if (selector.select(TIMEOUT_MS) == 0) {
                return new Response(ExitCodeCommand.ERROR,
                        "Сервер не отвечает (таймаут " + TIMEOUT_MS + "мс)");
            }

            // Получение ответа
            ByteBuffer responseBuffer = ByteBuffer.allocate(BUFFER_SIZE);
            SocketAddress sender = channel.receive(responseBuffer);

            if (sender == null) {
                return new Response(ExitCodeCommand.ERROR, "Не удалось получить ответ от сервера");
            }

            responseBuffer.flip();
            byte[] responseBytes = new byte[responseBuffer.remaining()];
            responseBuffer.get(responseBytes);

            Response response = Serializer.deserialize(responseBytes);

            // Выводим сообщение сервера (если есть)
            if (response.getMessage() != null && !response.getMessage().isEmpty()) {
                System.out.println(response.getMessage());
            }

            return response;

        } catch (Exception e) {
            System.err.println("❌ Ошибка связи с сервером: " + e.getMessage());
            return new Response(ExitCodeCommand.ERROR, "Ошибка соединения: " + e.getMessage());
        }
    }

    public void close() {
        try {
            selector.close();
            channel.close();
            System.out.println("Соединение закрыто.");
        } catch (IOException e) {
            System.err.println("Ошибка при закрытии соединения: " + e.getMessage());
        }
    }
}