package client;

import client.utility.FieldReaderClient;
import client.utility.FileManagerClient;
import client.utility.UserHandler;
import common.ExitCodeCommand;
import common.exceptions.ValueOutOfBoundsException;

import java.io.IOException;
import java.util.Scanner;
import java.util.StringJoiner;

/**
 * Главный класс клиентского приложения
 */
public class App {
    public static void main(String[] args) {

        /*
        if (args.length != 3) {
            System.out.println("Необходимо указать три аргумента!");
            System.out.println("Использование: java -jar client.jar <host> <port> <filename>");
            System.out.println("Пример корректного использования: java -jar client.jar localhost 2222 input_data.xml");
            System.exit(1);
        }
        String host = FieldReaderClient.readHost(args[0]);
        Integer port = FieldReaderClient.readPort(args[1]);
        String filename = args[2];
        */





        String host = "helios.cs.ifmo.ru";
        int port = 4000;
        String filename = "src\\input_data.xml";


        System.out.println("Клиент запускается");
        System.out.println("Сервер: " + host + ":" + port);
        System.out.println("Загрузочный файл: " + filename);

        try (Scanner scanner = new Scanner(System.in)) {

            UDPClient udpClient = new UDPClient(host, port);
            udpClient.connect();
            FileManagerClient fileManagerClient = new FileManagerClient();

            UserHandler userHandler = new UserHandler(udpClient, scanner,fileManagerClient);

            FieldReaderClient.setUserHandler(userHandler);


            userHandler.interactiveMode(filename);

            Thread mainThread = Thread.currentThread();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (userHandler.getExitCodeCommandStatus().equals(ExitCodeCommand.CTRL_C)) {
                    System.out.println("Вы использовали Ctrl+C.");
                }
                userHandler.handleExitResponse(udpClient.sendRequest(userHandler.createCommandRequest(userHandler.createCommand("exit",""))));
            }));

        } catch (Exception e) {
            System.err.println("Критическая ошибка в клиенте:");
            e.printStackTrace();
        }

    }
}