package server;


import client.utility.FieldReaderClient;
import client.utility.FileManagerClient;
import client.utility.ValidatorClient;
import common.ExitCodeCommand;
import common.commands.*;
import server.utility.*;
import server.utility.FileManager;

import java.io.File;

public class App {
    static Console console;
    public static void main(String[] args) {
        System.out.println("Использование: java -jar server.jar <port> <filename>");
        System.out.println("Пример корректного использования: java -jar server.jar 2222 input_data.xml");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (console!=null && !console.isFlagReadCollection()) {
                console.launchCommand("exit", "");
            }
            System.out.println("Завершение работы сервера");
        }));

        /*
        if (args.length != 2) {
            System.out.println("Необходимо указать два аргумента!");
            System.exit(1);
        }

        Integer port = FieldReaderServer.readPort(args[0]);
        String nameOfFile = args[1];
        while (ValidatorClient.validateNameOfFile(nameOfFile, FileManagerClient.ModeOfFileManager.READ_COLLECTION)==false){
            nameOfFile= FieldReaderServer.askFile();
        }

         */

        Integer port = 2222;
        String nameOfFile = "src\\input_data.xml";

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
            console = new Console(commandManger, fileManager, collectionManager);
            commandManger.getExitCommand().setFileManager(fileManager);
            FieldReaderServer.setConsole(console);

            console.loadCollection(new File(nameOfFile));

            UDPServer server = new UDPServer(console, fileManager,port);

            server.start();

        } catch (Exception e) {
            System.out.println("Ошибка запуска сервера: " + e.getMessage());
            e.printStackTrace();
        }
    }
}