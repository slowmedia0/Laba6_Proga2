package server;

import server.utility.Console;
import server.utility.FileManager;
import server.utility.RequestHandler;

import java.io.IOException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

public class UDPServer {

    private static final int PORT = 2222;
    private static final int BUFFER_SIZE = 65536;

    private final DatagramChannel channel;
    private final Selector selector;
    private final Console console;
    private final FileManager fileManager;

    public UDPServer(Console console, FileManager fileManager) throws IOException {
        this.console = console;
        this.fileManager = fileManager;

        channel = DatagramChannel.open();
        channel.configureBlocking(false);
        channel.bind(new java.net.InetSocketAddress(PORT));

        selector = Selector.open();
        channel.register(selector, SelectionKey.OP_READ);

        System.out.println("✅ Сервер запущен на порту " + PORT);
        System.out.println("Ожидание подключений от клиента...");
    }

    public void start() {
        try {
            while (true) {
                if (selector.select() == 0) continue;

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
            System.err.println("Ошибка работы сервера: " + e.getMessage());
        } finally {
            close();
        }
    }

    private void handleRequest() {
        RequestHandler.handleRequest(channel, selector, console, fileManager);
    }

    private void close() {
        try {
            selector.close();
            channel.close();
            System.out.println("Сервер завершил работу.");
        } catch (IOException e) {
            System.err.println("Ошибка закрытия сервера: " + e.getMessage());
        }
    }
}