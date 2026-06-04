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
    static UserHandler userHandler;
    public static void main(String[] args) {
/*
        if (args.length != 2) {
            System.out.println("Необходимо указать два аргумента!");
            System.out.println("Использование: java -jar client.jar <host> <port>");
            System.out.println("Пример корректного использования: java -jar client.jar localhost 2222 input_data.xml");
            System.exit(1);
        }
        String host = FieldReaderClient.readHost(args[0]);
        Integer port = FieldReaderClient.readPort(args[1]);

 */
        String host = "localhost";
        int port = 2222;



        System.out.println("Клиент запускается");

        try (Scanner scanner = new Scanner(System.in)) {

            UDPClient udpClient = new UDPClient(host, port);
            udpClient.connect();
            FileManagerClient fileManagerClient = new FileManagerClient();

            userHandler = new UserHandler(udpClient, scanner,fileManagerClient);

            FieldReaderClient.setUserHandler(userHandler);


            userHandler.interactiveMode();

            Thread mainThread = Thread.currentThread();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (userHandler!=null) {
                    if (userHandler.getExitCodeCommandStatus().equals(ExitCodeCommand.CTRL_C)) {
                        System.out.println("Вы использовали Ctrl+C.");
                    }
                    userHandler.handleExitResponse(udpClient.sendRequest(userHandler.createCommandRequest(userHandler.createCommand("exit", ""))));
                }
                else {
                    System.out.println("Клиент завершает работу");
                }
            }));

        } catch (Exception e) {
            System.err.println("Критическая ошибка в клиенте:");
            e.printStackTrace();
        }

    }
}