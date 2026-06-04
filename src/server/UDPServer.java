package server;

import server.utility.Console;
import server.utility.FileManager;
import server.utility.RequestHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

/**
 * Модуль приёма подключений (неблокирующий режим)
 */
public class UDPServer {

    private static int PORT;
    private static final int BUFFER_SIZE = 262144; // увеличен

    private final DatagramChannel channel;
    private final Selector selector;

    private final Console console;
    private final FileManager fileManager;

    public UDPServer(Console console, FileManager fileManager, int port) throws IOException {
        this.console = console;
        this.fileManager = fileManager;
        this.PORT = port;

        this.channel = DatagramChannel.open();
        this.channel.configureBlocking(false);
        this.channel.bind(new InetSocketAddress(PORT));

        this.selector = Selector.open();
        this.channel.register(selector, SelectionKey.OP_READ);

        System.out.println("Сервер успешно запущен на порту " + PORT);
        System.out.println("Ожидаем подключений");
    }

    public void start() {
        try {
            while (true) {
                selector.select();

                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();

                    if (key.isReadable()) {
                        RequestHandler.handleRequest(channel, selector, console, fileManager);
                    }

                    keyIterator.remove();
                }
            }
        } catch (IOException e) {
            System.out.println("Критическая ошибка сервера: " + e.getMessage());
        }
    }

    public void stop() {
        try {
            if (selector != null) selector.close();
            if (channel != null) channel.close();
            System.out.println("Работа сервер остановлена");
        } catch (IOException e) {
            System.out.println("Ошибка остановки работы сервера: " + e.getMessage());
        }
    }
}