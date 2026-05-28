package client;

import client.utility.FieldReaderClient;
import client.utility.UserHandler;

import java.io.IOException;
import java.util.Scanner;
import java.util.StringJoiner;

/**
 * Главный класс клиентского приложения
 */
public class App {

    public static void main(String[] args) {

        if (args.length != 3) {
            System.out.println("Ошибка: необходимо указать три аргумента!");
            System.out.println("Использование: java -jar client.jar <host> <port> <filename>");
            System.exit(0);
        }

        String host = args[0].trim();
        int port;
        String filename = args[2].trim();

        try {
            port = Integer.parseInt(args[1].trim());
        } catch (NumberFormatException e) {
            System.out.println("Ошибка: порт должен быть целым числом!");
            System.exit(1);
            return;
        }

        System.out.println("=== Клиент запускается ===");
        System.out.println("Сервер: " + host + ":" + port);
        System.out.println("Загрузочный файл: " + filename);
        System.out.println("================================\n");

        try (Scanner scanner = new Scanner(System.in)) {

            UDPClient udpClient = new UDPClient(host, port);

            UserHandler userHandler = new UserHandler(udpClient, scanner);

            // Настройка FieldReaderClient
            FieldReaderClient.setUserHandler(userHandler);

            System.out.println("Клиент готов к работе.\n");

            // Запуск интерактивного режима
            userHandler.interactiveMode(filename);

        } catch (IOException e) {
            System.err.println("Не удалось подключиться к серверу " + host + ":" + port);
            System.err.println("Ошибка: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Критическая ошибка в клиенте:");
            e.printStackTrace();
        }
    }
}