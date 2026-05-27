package server;

import server.utility.RequestHandler;
import server.utility.ResponseSender;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * Модуль приёма подключений и запросов от клиента (UDP Server).
 * Работает в однопоточном режиме, как требует лабораторная работа №6.
 */
public class UDPServer {

    private static final int BUFFER_SIZE = 65536;

    private final int port;
    private final RequestHandler requestHandler;
    private final ResponseSender responseSender;

    public UDPServer(int port, RequestHandler requestHandler, ResponseSender responseSender) {
        this.port = port;
        this.requestHandler = requestHandler;
        this.responseSender = responseSender;
    }

    /**
     * Запуск UDP-сервера
     */
    public void start() {
        System.out.println("Запуск UDP-сервера на порту " + port + "...");

        try (DatagramChannel channel = DatagramChannel.open()) {
            channel.bind(new InetSocketAddress(port));
            channel.configureBlocking(true);   // Однопоточный режим, как требуется в лабе

            System.out.println("Сервер успешно запущен на порту " + port);
            System.out.println("Ожидание команд от клиентов...");

            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

            while (true) {
                buffer.clear();

                SocketAddress clientAddress = channel.receive(buffer);

                if (clientAddress != null) {
                    buffer.flip();
                    byte[] data = new byte[buffer.remaining()];
                    buffer.get(data);

                    // Обработка запроса
                    requestHandler.handleRequest(channel, clientAddress, data);
                }
            }

        } catch (IOException e) {
            System.err.println("Критическая ошибка работы сервера: " + e.getMessage());
            e.printStackTrace();
        }
    }
}