package server;

import server.utility.CollectionManager;
import server.utility.CommandManger;
import server.utility.Console;
import server.utility.FileManager;

import java.io.File;

public class App {

    public static void main(String[] args) {
        String filename = args.length > 0 ? args[0] : "collection.xml";

        try {
            CollectionManager collectionManager = new CollectionManager();
            FileManager fileManager = new FileManager();
            CommandManger commandManager = new CommandManger(collectionManager, fileManager);
            Console console = new Console(commandManager, fileManager, collectionManager);

            console.loadCollection(new File(filename));

            UDPServer server = new UDPServer(console, fileManager);
            System.out.println("=== Сервер успешно запущен ===");
            server.start();

        } catch (Exception e) {
            System.err.println("Ошибка запуска сервера: " + e.getMessage());
            e.printStackTrace();
        }
    }
}