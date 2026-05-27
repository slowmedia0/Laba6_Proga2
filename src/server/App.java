package server;

import server.utility.*;
import server.utility.CommandManger;
import common.commands.*;

import java.io.File;

/**
 * Главный класс серверного приложения (Лабораторная №6)
 */
public class App {

    public static void main(String[] args) {

        if (args.length != 2) {
            System.out.println("Ошибка: необходимо указать 2 аргумента!");
            System.out.println("Использование: java -jar server.jar <port>");
            System.out.println("Пример: java -jar server.jar 8080 input_data.xml");
            System.exit(1);
        }

        int port;
        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.err.println("Ошибка: порт должен быть числом!");
            System.exit(1);
            return;
        }

        System.out.println("=== Запуск сервера ===");
        System.out.println("Порт: " + port);
        System.out.println("============================\n");

        try {
            // Создание основных компонентов
            CollectionManager collectionManager = new CollectionManager();
            FileManager fileManager = new FileManager(collectionManager);

            // Инициализация всех команд
            CommandManger commandManager = initializeCommands(collectionManager, fileManager);

            // Создание консоли и обработчиков
            ResponseSender responseSender = new ResponseSender();
            Console console = new Console(commandManager, fileManager, collectionManager);


            // Создание обработчика запросов
            RequestHandler requestHandler = new RequestHandler(console, responseSender);

            // Запуск UDP-сервера
            UDPServer udpServer = new UDPServer(port, requestHandler, responseSender);
            udpServer.start();

            // Загрузка коллекции из файла
            File loadFile = new File(filename);
            console.loadCollection(loadFile);

        } catch (Exception e) {
            System.err.println("Критическая ошибка при запуске сервера: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Инициализация всех доступных команд
     */
    private static CommandManger initializeCommands(CollectionManager collectionManager, FileManager fileManager) {
        // Здесь создаются все команды (адаптируйте под ваши классы команд)
        HelpCommand helpCommand = new HelpCommand();
        InfoCommand infoCommand = new InfoCommand();
        infoCommand.setCollectionManager(collectionManager);
        ShowCommand showCommand = new ShowCommand();
        showCommand.setCollectionManager(collectionManager);
        AddCommand addCommand = new AddCommand();
        addCommand.setCollectionManager(collectionManager);
        UpdateIdCommand updateIdCommand = new UpdateIdCommand();
        updateIdCommand.setCollectionManager(collectionManager);
        RemoveByIdCommand removeByIdCommand = new RemoveByIdCommand();
        removeByIdCommand.setCollectionManager(collectionManager);
        ClearCommand clearCommand = new ClearCommand();
        clearCommand.setCollectionManager(collectionManager);
        ExecuteScriptCommand executeScriptCommand = new ExecuteScriptCommand();
        ExitCommand exitCommand = new ExitCommand();
        RemoveGreaterCommand removeGreaterCommand = new RemoveGreaterCommand();
        removeGreaterCommand.setCollectionManager(collectionManager);
        ReorderCommand reorderCommand = new ReorderCommand();
        reorderCommand.setCollectionManager(collectionManager);
        SortCommand sortCommand = new SortCommand();
        sortCommand.setCollectionManager(collectionManager);
        SumOfEnginePowerCommand sumCommand = new SumOfEnginePowerCommand();
        sumCommand.setCollectionManager(collectionManager);
        PrintFieldAscendingNumberOfWheelsCommand printAscCommand = new PrintFieldAscendingNumberOfWheelsCommand();
        printAscCommand.setCollectionManager(collectionManager);
        PrintFieldDescendingNumberOfWheelsCommand printDescCommand = new PrintFieldDescendingNumberOfWheelsCommand();
        printDescCommand.setCollectionManager(collectionManager);
        return new CommandManger(
                helpCommand, infoCommand, showCommand, addCommand, updateIdCommand,
                removeByIdCommand, clearCommand, executeScriptCommand,
                exitCommand, removeGreaterCommand, reorderCommand, sortCommand,
                sumCommand, printAscCommand, printDescCommand
        );
    }
}