package server;

import common.commands.*;
import server.utility.*;
import server.utility.FileManager;

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

            commandManger.getExitCommand().setFileManager(fileManager);
            FieldReaderServer.setConsole(console);


            UDPServer server = new UDPServer(console, fileManager);
            System.out.println("=== Сервер успешно запущен ===");
            System.out.println("Ожидание подключения клиента...");

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nЗавершение сервера.");
            }));

            server.start();

        } catch (Exception e) {
            System.err.println("Ошибка запуска сервера: " + e.getMessage());
            e.printStackTrace();
        }
    }
}