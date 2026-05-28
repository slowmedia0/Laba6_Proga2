package server;

import common.commands.*;
import server.utility.CollectionManager;
import server.utility.CommandManger;
import server.utility.Console;
import server.utility.FileManager;

import java.io.File;
import java.util.Scanner;

public class App {

    public static void main(String[] args) {

        try {
            CollectionManager collectionManager = new CollectionManager();
            FileManager fileManager = new FileManager(collectionManager);

            CommandManger commandManger = new CommandManger(
                    new HelpCommand(),
                    new InfoCommand(),
                    new ShowCommand(),
                    new AddCommand(),
                    new UpdateIdCommand(),
                    new RemoveByIdCommand(),
                    new ClearCommand(),
                    new ExecuteScriptCommand(),
                    new ExitCommand(),
                    new RemoveGreaterCommand(),
                    new ReorderCommand(),
                    new SortCommand(),
                    new SumOfEnginePowerCommand(),
                    new PrintFieldAscendingNumberOfWheelsCommand(),
                    new PrintFieldDescendingNumberOfWheelsCommand()
            );

            Console console = new Console(commandManger, fileManager, collectionManager);

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