package client;

import common.ExitCodeCommand;
import common.commands.CommandRequest;
import common.interaction.Response;
import common.utility.Serializer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

public class UDPClient {

    private final DatagramChannel channel;
    private final Selector selector;
    private final InetSocketAddress serverAddress;

    private static final int BUFFER_SIZE = 524288;
    private static final int TIMEOUT_MS = 15000;

    public UDPClient(String host, int port) throws IOException {
        this.serverAddress = new InetSocketAddress(host, port);

        this.channel = DatagramChannel.open();
        this.channel.configureBlocking(false);

        this.selector = Selector.open();
        this.channel.register(selector, SelectionKey.OP_READ);

        System.out.println("Клиент готов к работе | Сервер: " + host + ":" + port);
    }

    // Временно замени sendRequest на этот метод (для диагностики)
    public Response sendRequest(CommandRequest command) {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(10000);

            byte[] data = Serializer.serialize(command);
            DatagramPacket sendPacket = new DatagramPacket(data, data.length, serverAddress);
            socket.send(sendPacket);

            System.out.println("→ Отправлена: " + command.getNameOfCommand());

            byte[] buffer = new byte[BUFFER_SIZE];
            DatagramPacket recvPacket = new DatagramPacket(buffer, buffer.length);
            socket.receive(recvPacket);

            System.out.println("← Получено " + recvPacket.getLength() + " байт");

            Response response = Serializer.deserialize(recvPacket.getData());

            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(ExitCodeCommand.ERROR, "Ошибка: " + e.getMessage());
        }
    }

    public void close() {
        try {
            selector.close();
            channel.close();
        } catch (IOException e) {
            System.err.println("Ошибка закрытия клиента: " + e.getMessage());
        }
    }
}