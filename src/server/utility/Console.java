package server.utility;

import common.ExitCodeCommand;
import common.models.*;
import common.exceptions.*;
import common.utility.ResponseBuilder;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

/**
 * Класс консольного интерфейса приложения.
 */
public class Console {

    public ExitCodeCommand exitCodeStatus = ExitCodeCommand.CTRL_C;

    private final CommandManger commandManager;
    private final server.utility.FileManager fileManager;
    private String loadFileName;
    private byte[] loadFileData;
    private File currentLoadFile;

    private boolean flagScript;
    private boolean flagReadCollection;

    private ArrayList<String> fields = new ArrayList<>(7);
    private CollectionManager collectionManager;

    public Console(CommandManger commandManager, server.utility.FileManager fileManager, CollectionManager collectionManager) {
        this.commandManager = commandManager;
        this.fileManager = fileManager;
        this.collectionManager = collectionManager;
    }

    public ExitCodeCommand getExitCodeStatus() {
        return exitCodeStatus;
    }

    public void setExitCodeStatus(ExitCodeCommand exitCodeStatus) {
        this.exitCodeStatus = exitCodeStatus;
    }

    public String getLoadFileName() {
        return loadFileName;
    }

    public byte[] getLoadFileData() {
        return loadFileData;
    }

    public ArrayList<String> getFields() {
        return fields;
    }

    public boolean isFlagScript() {
        return flagScript;
    }

    public boolean isFlagReadCollection() {
        return flagReadCollection;
    }

    /**
     * Загружает коллекцию из файла при запуске сервера.
     */
    public void loadCollection(File loadFile) throws IOException, ParserConfigurationException, SAXException {
        this.currentLoadFile = loadFile;
        flagReadCollection = true;
        flagScript = false;

        ResponseBuilder.clear();

        try {
            ResponseBuilder.appendLn("Загрузка коллекции из файла: " + loadFile.getName());

            collectionManager.setCollection(fileManager.readCollection(loadFile));
            collectionManager.initializeArrayId();
            collectionManager.setCreationDate(java.time.LocalDate.now());

            int size = collectionManager.getCollection().size();

            ResponseBuilder.appendLn("Коллекция успешно загружена на сервере. Количество элементов: " + size);

        } catch (Exception e) {
            ResponseBuilder.appendLn("Ошибка при загрузке коллекции из файла " + loadFile.getName() + ": " + e.getMessage());
            throw e;
        } finally {
            flagReadCollection = false;
        }
    }


    public ExitCodeCommand loadCollectionFromBytes(String fileName, byte[] fileData) {
        ResponseBuilder.clear();

        try {
            this.loadFileName = fileName;
            this.loadFileData = fileData;
            File tempFile = new File(fileName);
            Files.write(tempFile.toPath(), fileData);
            loadCollection(tempFile);

            return ExitCodeCommand.OK;

        } catch (Exception e) {
            ResponseBuilder.appendLn("Ошибка загрузки коллекции из байтов: " + e.getMessage());
            return ExitCodeCommand.ERROR;
        }
    }


    public ExitCodeCommand launchCommand(String mnemonics, String argument) {
        try {
            switch (mnemonics) {
                case "help":
                    exitCodeStatus = commandManager.help(argument);
                    return exitCodeStatus;
                case "info":
                    exitCodeStatus = commandManager.info(argument);
                    return exitCodeStatus;
                case "show":
                    exitCodeStatus = commandManager.show(argument);
                    return exitCodeStatus;
                case "add":
                    exitCodeStatus = commandManager.add(argument);
                    return exitCodeStatus;
                case "update":
                    exitCodeStatus = commandManager.updateById(argument);
                    return exitCodeStatus;
                case "remove_by_id":
                    exitCodeStatus = commandManager.removeById(argument);
                    return exitCodeStatus;
                case "clear":
                    exitCodeStatus = commandManager.clear(argument);
                    return exitCodeStatus;
                case "exit":
                    exitCodeStatus = commandManager.exit(argument);
                    return exitCodeStatus;
                case "remove_greater":
                    exitCodeStatus = commandManager.removeGreater(argument);
                    return exitCodeStatus;
                case "reorder":
                    exitCodeStatus = commandManager.reorder(argument);
                    return exitCodeStatus;
                case "sort":
                    exitCodeStatus = commandManager.sort(argument);
                    return exitCodeStatus;
                case "sum_of_engine_power":
                    exitCodeStatus = commandManager.sumOfEnginePower(argument);
                    return exitCodeStatus;
                case "print_field_ascending_number_of_wheels":
                    exitCodeStatus = commandManager.printFieldAscendingNumberOfWheels(argument);
                    return exitCodeStatus;
                case "print_field_descending_number_of_wheels":
                    exitCodeStatus = commandManager.printFieldDescendingNumberOfWheels(argument);
                    return exitCodeStatus;
                default:
                    if ((mnemonics + argument).isEmpty() || (mnemonics + argument).trim().isEmpty()) {
                        exitCodeStatus = ExitCodeCommand.OK;
                        return exitCodeStatus;
                    }
                    throw new CommandNotExist("Команда " + mnemonics + " не существует!");
            }
        } catch (CommandNotExist e) {
            ResponseBuilder.appendLn(e.getMessage());
            exitCodeStatus = ExitCodeCommand.ERROR;
            return exitCodeStatus;
        }
    }

    public ExitCodeCommand launchCommand(String mnemonics, String argument, Vehicle vehicle, String FileName, byte[] FileData) {
        try {
            switch (mnemonics) {
                case "help":
                    exitCodeStatus = commandManager.help(argument, null, null, null);
                    return exitCodeStatus;
                case "info":
                    exitCodeStatus = commandManager.info(argument, null, null, null);
                    return exitCodeStatus;
                case "show":
                    exitCodeStatus = commandManager.show(argument, null, null, null);
                    return exitCodeStatus;
                case "add":
                    exitCodeStatus = commandManager.add(argument, vehicle, null, null);
                    return exitCodeStatus;
                case "update":
                    exitCodeStatus = commandManager.updateById(argument, vehicle, null, null);
                    return exitCodeStatus;
                case "remove_by_id":
                    exitCodeStatus = commandManager.removeById(argument, null, null, null);
                    return exitCodeStatus;
                case "clear":
                    exitCodeStatus = commandManager.clear(argument, null, null, null);
                    return exitCodeStatus;
                case "exit":
                    exitCodeStatus = commandManager.exit(argument, null, null, null);
                    return exitCodeStatus;
                case "remove_greater":
                    exitCodeStatus = commandManager.removeGreater(argument, vehicle, null, null);
                    return exitCodeStatus;
                case "reorder":
                    exitCodeStatus = commandManager.reorder(argument, null, null, null);
                    return exitCodeStatus;
                case "sort":
                    exitCodeStatus = commandManager.sort(argument, null, null, null);
                    return exitCodeStatus;
                case "sum_of_engine_power":
                    exitCodeStatus = commandManager.sumOfEnginePower(argument, null, null, null);
                    return exitCodeStatus;
                case "print_field_ascending_number_of_wheels":
                    exitCodeStatus = commandManager.printFieldAscendingNumberOfWheels(argument, null, null, null);
                    return exitCodeStatus;
                case "print_field_descending_number_of_wheels":
                    exitCodeStatus = commandManager.printFieldDescendingNumberOfWheels(argument, null, null, null);
                    return exitCodeStatus;
                default:
                    if ((mnemonics + argument).isEmpty() || (mnemonics + argument).trim().isEmpty()) {
                        exitCodeStatus = ExitCodeCommand.OK;
                        return exitCodeStatus;
                    }
                    throw new CommandNotExist("Команда " + mnemonics + " не существует!");
            }
        } catch (CommandNotExist e) {
            ResponseBuilder.appendLn(e.getMessage());
            exitCodeStatus = ExitCodeCommand.ERROR;
            return exitCodeStatus;
        }
    }
}