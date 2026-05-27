package server;

import server.utility.Console;
import server.utility.FileManager;
import server.utility.RequestHandler;
import server.utility.ResponseSender;
import common.commands.CommandRequest;
import common.interaction.Response;
import common.utility.Serializer;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

public class UDPServer {

    private static final int PORT = 2222;
    private static final int BUFFER_SIZE = 65536;

    private final DatagramChannel channel;
    private final Selector selector;
    private final RequestHandler requestHandler;
    private final ResponseSender responseSender;

    public UDPServer(Console console, FileManager fileManager) throws IOException {
        channel = DatagramChannel.open();
        channel.configureBlocking(false);
        channel.bind(new java.net.InetSocketAddress(PORT));

        selector = Selector.open();
        channel.register(selector, SelectionKey.OP_READ);

        this.responseSender = new ResponseSender();
        this.requestHandler = new RequestHandler(console, fileManager);

        System.out.println("Сервер запущен на порту " + PORT);
    }

    public void start() {
        try {
            while (true) {
                selector.select();
                Iterator<SelectionKey> it = selector.selectedKeys().iterator();

                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    if (key.isReadable()) {
                        handleRequest();
                    }
                    it.remove();
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка сервера: " + e.getMessage());
        }
    }

    private void handleRequest() {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
            SocketAddress clientAddress = channel.receive(buffer);
            if (clientAddress == null) return;

            buffer.flip();
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);

            CommandRequest request = Serializer.deserialize(data);
            Response response = requestHandler.handle(request);

            responseSender.send(channel, response, clientAddress);

        } catch (Exception e) {
            System.err.println("Ошибка обработки запроса: " + e.getMessage());
        }
    }
}