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

public class UDPClient {

    private final DatagramChannel channel;
    private final Selector selector;
    private final SocketAddress serverAddress;

    private static final int BUFFER_SIZE = 524288;  // увеличил буфер
    private static final int TIMEOUT_MS = 15000;    // 15 секунд

    public UDPClient(String host, int port) throws IOException {
        this.serverAddress = new InetSocketAddress(host, port);

        this.channel = DatagramChannel.open();
        this.channel.configureBlocking(false);

        this.selector = Selector.open();
        this.channel.register(selector, SelectionKey.OP_READ);

        System.out.println("✅ Клиент подключён к " + host + ":" + port);
    }

    public Response sendRequest(CommandRequest command) {
        try {
            byte[] data = Serializer.serialize(command);
            ByteBuffer sendBuffer = ByteBuffer.wrap(data);
            channel.send(sendBuffer, serverAddress);

            System.out.println("→ Отправлена команда: " + command.getNameOfCommand());



            ByteBuffer recvBuffer = ByteBuffer.allocate(524288);
            SocketAddress sender = channel.receive(recvBuffer);

            if (sender == null) {
                return new Response("Не получено данных от сервера");
            }

            recvBuffer.flip();
            byte[] responseBytes = new byte[recvBuffer.remaining()];
            recvBuffer.get(responseBytes);

            System.out.println("← Получено " + responseBytes.length + " байт от сервера");

            Response response = Serializer.deserialize(responseBytes);
            return response;

        } catch (Exception e) {
            e.printStackTrace();
            return new Response("Ошибка связи: " + e.getMessage());
        }
    }

    public void close() {
        try {
            selector.close();
            channel.close();
            System.out.println("Клиент закрыт.");
        } catch (IOException e) {
            System.err.println("Ошибка закрытия: " + e.getMessage());
        }
    }
}