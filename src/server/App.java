package server;

import client.utility.FieldReaderClient;
import common.commands.*;
import server.utility.*;

import java.io.File;
import java.util.Scanner;

public class App {

    public static void main(String[] args) {

        try {
            CollectionManager collectionManager = new CollectionManager();
            FileManager fileManager = new FileManager(collectionManager);

            CommandManger commandManger = new CommandManger(
                    new HelpCommand(),
                    new InfoCommand(collectionManager),
                    new ShowCommand(collectionManager),
                    new AddCommand(collectionManager),
                    new UpdateIdCommand(collectionManager),
                    new RemoveByIdCommand(collectionManager),
                    new ClearCommand(collectionManager),
                    new ExecuteScriptCommand(),
                    new ExitCommand(),
                    new RemoveGreaterCommand(collectionManager),
                    new ReorderCommand(collectionManager),
                    new SortCommand(collectionManager),
                    new SumOfEnginePowerCommand(collectionManager),
                    new PrintFieldAscendingNumberOfWheelsCommand(collectionManager),
                    new PrintFieldDescendingNumberOfWheelsCommand(collectionManager)
            );
            Console console = new Console(commandManger, fileManager, collectionManager);

            commandManger.getExecuteScriptCommand().setConsole(console);
            FieldReaderServer.setConsole(console);

            UDPServer server = new UDPServer(console, fileManager);
            System.out.println("=== Сервер успешно запущен ===");
            System.out.println("Ожидание подключения клиента...");

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nЗавершение сервера. Сохранение коллекции...");
                fileManager.writeCollection();
            }));

            server.start();

        } catch (Exception e) {
            System.err.println("Ошибка запуска сервера: " + e.getMessage());
            e.printStackTrace();
        }
    }
}