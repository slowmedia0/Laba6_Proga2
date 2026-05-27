package client;

import client.utility.UserHandler;

import java.io.IOException;
import java.util.Scanner;

public class App {

    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 2222;

    public static void main(String[] args) {
        String host = DEFAULT_HOST;
        int port = DEFAULT_PORT;
        String loadFileName;

        // Обработка аргументов командной строки
        if (args.length!=3){
            System.out.println("Ошибка в аргументах командной строки должно указыаваться сначала загрузочный файл, потом host, потом порт");
        }
        else {
            loadFileName = args[0];
            host = args[1];
            try {
                port = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                System.err.println("Неверный формат порта. Используется порт по умолчанию: " + DEFAULT_PORT);
            }
        }



        try {
            UDPClient udpClient = new UDPClient(host, port);
            Scanner scanner = new Scanner(System.in);

            UserHandler userHandler = new UserHandler(udpClient, scanner);

            System.out.println("=== Клиентская часть лабораторной работы №6 запущена ===");
            userHandler.interactiveMode(loadFileName);

        } catch (IOException e) {
            System.err.println("Не удалось запустить клиент: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Критическая ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }
}