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
    private static final int BUFFER_SIZE = 262144; 

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
    try {
            this.channel.bind(new InetSocketAddress("0.0.0.0",PORT));
            System.out.println("Сервер успешно запущен на порту " + PORT);
            System.out.println("Ожидаем подключений");
        } catch (IOException e) {
            if (e.getMessage() != null &&
                    (e.getMessage().contains("Address already in use") ||
                            e.getMessage().contains("Cannot assign requested address"))) {
                System.out.println("Порт " + PORT + " уже занят!");
                System.out.println("Завершите предыдущий экземпляр сервера или используйте другой порт.");
                System.exit(0);
            } else {
                throw e;
            }
        }

        this.selector = Selector.open();
        this.channel.register(selector, SelectionKey.OP_READ);
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