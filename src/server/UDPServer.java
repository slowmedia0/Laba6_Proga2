package server;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import server.utility.Console;
import server.utility.FileManager;
import server.utility.RequestHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;


public class UDPServer {

   // private static final Logger logger = LoggerFactory.getLogger(UDPServer.class);

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

          //  logger.info("Сервер успешно запущен на порту {}", PORT);
//logger.info("Ожидаем подключений");

        } catch (IOException e) {
            if (e.getMessage() != null &&
                    (e.getMessage().contains("Address already in use") ||
                            e.getMessage().contains("Cannot assign requested address"))) {
                System.out.println("Порт " + PORT + " уже занят!");
                System.out.println("Завершите предыдущий экземпляр сервера или используйте другой порт.");
             //   logger.error("Порт {} уже занят!", PORT);
                System.exit(0);
            } else {
             //   logger.error("Ошибка при привязке сокета", e);
                throw e;
            }
        }

        this.selector = Selector.open();
        this.channel.register(selector, SelectionKey.OP_READ);
       // logger.info("Selector зарегистрирован в режиме OP_READ");
    }

    public void start() {
        try {
         //   logger.info("Сервер начинает основной цикл приёма запросов");
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
      //      logger.error("Критическая ошибка сервера: {}", e.getMessage(), e);
        } finally {
         //   logger.info("Сервер завершает работу");
        }
    }

    public void stop() {
        try {
         //   logger.info("Останавливаем сервер...");
            if (selector != null) selector.close();
            if (channel != null) channel.close();
            System.out.println("Работа сервер остановлена");
         //   logger.info("Работа сервер остановлена");
        } catch (IOException e) {
            System.out.println("Ошибка остановки работы сервера: " + e.getMessage());
          //  logger.error("Ошибка остановки работы сервера: {}", e.getMessage(), e);
        }
    }
}