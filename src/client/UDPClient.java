package client;

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

/**
 * UDPClient — неблокирующий клиент по протоколу UDP.
 */
public class UDPClient {

    private final DatagramChannel channel;
    private final Selector selector;
    private final SocketAddress serverAddress;

    private static final int BUFFER_SIZE = 65536;
    private static final int TIMEOUT_MS = 10000; // 10 секунд

    public UDPClient(String host, int port) throws IOException {
        this.serverAddress = new InetSocketAddress(host, port);

        this.channel = DatagramChannel.open();
        this.channel.configureBlocking(false);
        this.channel.connect(serverAddress);

        this.selector = Selector.open();
        this.channel.register(selector, SelectionKey.OP_READ);

        System.out.println("Клиент успешно подключён к серверу " + host + ":" + port);
    }

    /**
     * Отправляет CommandRequest на сервер и возвращает Response
     */
    public Response sendRequest(CommandRequest command) {
        try {
            if (command == null) {
                return new Response(false, "Не удалось сформировать запрос-команду!");
            }

            // Сериализация
            byte[] data = Serializer.serialize(command);
            ByteBuffer buffer = ByteBuffer.wrap(data);

            // Отправка
            channel.write(buffer);
            System.out.println("→ Отправлена команда: " + command.getNameOfCommand());

            // Ожидание ответа
            if (selector.select(TIMEOUT_MS) == 0) {
                return new Response(false, "Таймаут ожидания ответа от сервера (" + TIMEOUT_MS + " мс)");
            }

            // Чтение ответа
            ByteBuffer responseBuffer = ByteBuffer.allocate(BUFFER_SIZE);
            SocketAddress sender = channel.receive(responseBuffer);

            if (sender == null) {
                return new Response(false, "Не удалось получить ответ от сервера");
            }

            responseBuffer.flip();
            byte[] responseBytes = new byte[responseBuffer.remaining()];
            responseBuffer.get(responseBytes);

            // Десериализация
            Response response = Serializer.deserialize(responseBytes);

            System.out.println("← Получен ответ от сервера: " +
                    (response.isSuccess() ? "Успех" : "Ошибка"));

            if (response.getMessage() != null) {
                System.out.println("Сообщение: " + response.getMessage());
            }

            return response;

        } catch (Exception e) {
            System.err.println("Ошибка связи с сервером: " + e.getMessage());
            return new Response(false, "Ошибка связи: " + e.getMessage());
        }
    }

    /**
     * Закрытие клиента
     */
    public void close() {
        try {
            if (selector != null) selector.close();
            if (channel != null) channel.close();
            System.out.println("Клиент закрыт.");
        } catch (IOException e) {
            System.err.println("Ошибка при закрытии клиента: " + e.getMessage());
        }
    }
}