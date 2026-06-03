package server;


import common.commands.*;
import server.utility.*;
import server.utility.FileManager;

public class App {

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Завершение работы сервера");
        }));

        if (args.length != 1) {
            System.out.println("Необходимо указать один аргумент!");
            System.out.println("Использование: java -jar server.jar <port>");
            System.out.println("Пример корректного использования: java -jar server.jar 2222");
            System.exit(1);
        }




        Integer port = FieldReaderServer.readPort(args[0]);



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


            UDPServer server = new UDPServer(console, fileManager,port);
            System.out.println("Сервер успешно запущен");

            server.start();

        } catch (Exception e) {
            System.out.println("Ошибка запуска сервера: " + e.getMessage());
            e.printStackTrace();
        }
    }
}