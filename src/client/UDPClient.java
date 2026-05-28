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
    private static final int TIMEOUT_MS = 5000;   // можно уменьшить

    public UDPClient(String host, int port) throws IOException {
        this.serverAddress = new InetSocketAddress(host, port);

        this.channel = DatagramChannel.open();
        this.channel.configureBlocking(false);

        this.selector = Selector.open();
        this.channel.register(selector, SelectionKey.OP_READ);

        System.out.println("✅ Клиент подключён к " + host + ":" + port);
    }

    public Response sendRequest(CommandRequest command) {
        if (command == null) {
            return new Response(ExitCodeCommand.ERROR, "Не удалось создать запрос");
        }

        try {
            // === ОТПРАВКА ===
            byte[] data = Serializer.serialize(command);
            ByteBuffer sendBuffer = ByteBuffer.wrap(data);

            int bytesSent = channel.send(sendBuffer, serverAddress);
            System.out.println("→ Отправлена команда: " + command.getNameOfCommand() + " (" + bytesSent + " байт)");

            // === ОЖИДАНИЕ ОТВЕТА ===
            if (selector.select(TIMEOUT_MS) == 0) {
                return new Response(ExitCodeCommand.ERROR,
                        "Сервер не отвечает (таймаут " + TIMEOUT_MS + " мс)");
            }

            // === ПОЛУЧЕНИЕ ОТВЕТА ===
            ByteBuffer receiveBuffer = ByteBuffer.allocate(BUFFER_SIZE);
            SocketAddress sender = channel.receive(receiveBuffer);

            if (sender == null) {
                return new Response(ExitCodeCommand.ERROR, "Не удалось получить ответ от сервера");
            }

            receiveBuffer.flip();
            byte[] responseData = new byte[receiveBuffer.remaining()];
            receiveBuffer.get(responseData);

            Response response = Serializer.deserialize(responseData);
            return response;

        } catch (Exception e) {
            System.err.println("❌ Ошибка связи с сервером: " + e.getMessage());
            e.printStackTrace();           // ← Очень полезно для отладки
            return new Response(ExitCodeCommand.ERROR, "Ошибка соединения: " + e.getClass().getSimpleName());
        }
    }

    public void close() {
        try {
            selector.close();
            channel.close();
            System.out.println("Клиент: соединение закрыто.");
        } catch (IOException e) {
            System.err.println("Ошибка при закрытии клиента: " + e.getMessage());
        }
    }
}