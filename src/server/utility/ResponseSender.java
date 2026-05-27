package server.utility;

import common.interaction.Response;
import common.utility.Serializer;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class ResponseSender {

    public void send(DatagramChannel channel, Response response, SocketAddress clientAddress) {
        try {
            byte[] data = Serializer.serialize(response);
            ByteBuffer buffer = ByteBuffer.wrap(data);
            channel.send(buffer, clientAddress);
        } catch (IOException e) {
            System.err.println("Ошибка отправки ответа: " + e.getMessage());
        }
    }
}