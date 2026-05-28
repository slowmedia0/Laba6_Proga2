package server.utility;


import common.ExitCodeCommand;
import common.models.*;
import common.exceptions.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.FileNameMap;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Класс консольного интерфейса приложения.
 */
public class Console {

    public ExitCodeCommand exitCodeStatus = ExitCodeCommand.CTRL_C;

    private final CommandManger commandManager;
    private final FileManager fileManager;
    private  String loadFileName;      // имя загрузочного файла
    private  byte[] loadFileData;      // содержимое загрузочного файла (для exit/save)
    private File currentLoadFile;     // ← добавил (используется в loadCollection)

    /**
     * Аргументы выполняемых скриптов.
     */
    private ArrayList<File> arguments;
    private CollectionManager collectionManager;
    private boolean flagScript;
    private boolean flagReadCollection;
    /**
     * Список полей объекта, считанных из скрипта.
     */
    private ArrayList<String> fields = new ArrayList<>(7);

    public ExitCodeCommand getExitCodeStatus() {
        return exitCodeStatus;
    }

    public void setExitCodeStatus(ExitCodeCommand exitCodeStatus) {
        this.exitCodeStatus = exitCodeStatus;
    }

    /**
     * Создаёт объект консоли.
     *
     * @param commandManager    менеджер команд
     * @param fileManager       файловый менеджер
     * @param collectionManager менеджер коллекции
     */
    public Console(CommandManger commandManager, FileManager fileManager, CollectionManager collectionManager) {
        this.commandManager = commandManager;
        this.fileManager = fileManager;
        this.collectionManager = collectionManager;
        this.arguments = new ArrayList<>();
    }

    public ArrayList<File> getArguments() {
        return arguments;
    }

    /**
     * Возвращает флаг режима скрипта.
     *
     * @return true, если включён режим скрипта
     */
    public boolean isFlagScript() {
        return flagScript;
    }

    /**
     * Возвращает флаг чтения коллекции из файла.
     *
     * @return true, если идёт чтение коллекции
     */
    public boolean isFlagReadCollection() {
        return flagReadCollection;
    }

    /**
     * Возвращает список полей составного объекта.
     *
     * @return список полей
     */
    public ArrayList<String> getFields() {
        return fields;
    }

    public String getLoadFileName() {
        return loadFileName;
    }

    public byte[] getLoadFileData() {
        return loadFileData;
    }

    public ExitCodeCommand scriptMode(String FileName, byte[] FileData) {
        File file = new File(FileName);
        try {
            Files.write(file.toPath(), FileData);
            System.out.println("Файл успешно перезаписан!");
        } catch (IOException e) {
            System.out.println("Не удалось перезаписать файл!");
        }
        ExitCodeCommand flagSuccessExecute = ExitCodeCommand.OK;
        try {
            int n = -1;
            boolean flagElemCommand = true;
            int index = n;
            arguments.add(file);
            String mnemonics = "";
            String arg = "";
            if (fileManager.readScript(file) == null) {
                throw new NullPointerException("");
            }
            for (var maybeCommand : fileManager.readScript(file)) {
                try {
                    n += 1;
                    ArrayList<String> command = new ArrayList<>(2);
                    for (var i : maybeCommand.trim().split("\\s+", 2)) {
                        command.add(i);
                    }
                    if (command.size() != 0) {
                        if (command.size() == 1) {
                            command.add("");
                        }
                    }
                    if (flagElemCommand == true) {
                        if (command.get(0).equals("execute_script") && arguments.contains(command.get(1))) {
                            File file1 = file;
                            for (int i = 0; i < arguments.size(); i++) {
                                File file2 = arguments.get(i);
                                if (file1.equals(file2)) {
                                    throw new ScriptRecursionException("Не удалось выполнить без ошибок команду " + command.get(0) + " " + command.get(1) + " в скрипте " + file.getName() + " ! Рекурсивный вызов скрипта '" + command.get(1) + "'!");
                                }
                            }
                        } else if (command.get(0).equals("add") || command.get(0).equals("update") || command.get(0).equals("remove_greater")) {
                            flagElemCommand = false;
                            index = n + 7;
                            mnemonics = command.get(0);
                            arg = command.get(1);
                        } else if (launchCommand(command.get(0), command.get(1)).equals(ExitCodeCommand.OK) == false) {
                            if (command.get(0).equals("execute_script")) {
                                System.out.println("Не удалось выполнить без ошибок команду " + command.get(0) + " " + command.get(1) + " в скрипте " + file.getName()+ " !");
                            } else {
                                System.out.println("Не удалось выполнить команду " + command.get(0) + " " + command.get(1) + " в скрипте " + file.getName() + " !");
                            }
                            flagSuccessExecute = ExitCodeCommand.ERROR;
                        } else {
                            System.out.println();
                        }
                    } else {
                        fields.add(maybeCommand);
                        if (n == index) {
                            if (launchCommand(mnemonics, arg).equals(ExitCodeCommand.OK) == false) {
                                System.out.println("Не удалось выполнить команду " + mnemonics + " " + arg + " в скрипте " + file.getName() + " !");
                                flagSuccessExecute = ExitCodeCommand.ERROR;
                            } else {
                                System.out.println();
                            }
                            fields.clear();
                            flagElemCommand = true;
                        }
                    }
                } catch (ScriptRecursionException e) {
                    System.out.println(e.getMessage());
                    flagSuccessExecute = ExitCodeCommand.ERROR;
                }
            }
        } catch (IllegalStateException e) {
            System.out.println(e.getMessage());
            flagSuccessExecute = ExitCodeCommand.ERROR;
        } catch (NullPointerException e) {
            flagSuccessExecute = ExitCodeCommand.ERROR;
        } catch (IndexOutOfBoundsException e) {
            System.out.println("В скрипте нет команд!");
            flagSuccessExecute = ExitCodeCommand.ERROR;
        }
        return flagSuccessExecute;
    }


    /**
     * Загружает коллекцию из файла при запуске сервера.
     */
    public void loadCollection(File loadFile) throws IOException, ParserConfigurationException, SAXException {
        this.currentLoadFile = loadFile;
        flagReadCollection = true;
        flagScript = false;

        try {
            System.out.println("Загрузка коллекции из файла: " + loadFile.getName());

            // Чтение коллекции
            collectionManager.setCollection(fileManager.readCollection(loadFile));

            // Инициализация вспомогательных структур
            collectionManager.initializeArrayId();
            collectionManager.setCreationDate(java.time.LocalDate.now());

            int size = collectionManager.getCollection().size();

            System.out.println("Коллекция успешно загружена. Количество элементов: " + size);

        } catch (Exception e) {
            System.err.println("КРИТИЧЕСКАЯ ОШИБКА при загрузке коллекции из файла " + loadFile.getName());
            System.err.println("Ошибка: " + e.getMessage());
            throw e; // пробрасываем исключение дальше
        } finally {
            flagReadCollection = false;
        }
    }

    /**
         * Загрузка коллекции из байтов, пришедших от клиента
         */
       public ExitCodeCommand loadCollectionFromBytes(String fileName, byte[] fileData) {
          try {
                this.loadFileName = fileName;
                this.loadFileData = fileData;
                File tempFile = new File(fileName);
                Files.write(tempFile.toPath(), fileData);
                loadCollection(tempFile);
                System.out.println("Коллекция успешно загружена из данных клиента.");
                return ExitCodeCommand.OK;
            } catch (Exception e) {
                        System.err.println("Ошибка загрузки коллекции из байтов: " + e.getMessage());
                        return ExitCodeCommand.ERROR;
          }
         }

    // Добавь этот метод в класс Console
    public void sortCollectionIfNeeded(String commandName) {
        switch (commandName.toLowerCase()) {
            case "add":
            case "update":
            case "remove_by_id":
            case "remove_greater":
            case "clear":
            case "reorder":
            case "sort":
                collectionManager.sortByName();   // нужно добавить в CollectionManager
                System.out.println("Коллекция отсортирована по имени после команды: " + commandName);
                break;
        }
    }

    public ExitCodeCommand launchCommand(String mnemonics, String argument) {
        try {
            switch (mnemonics) {
                case "help": {
                    exitCodeStatus = commandManager.help(argument);
                    return exitCodeStatus;
                }
                case "info": {
                    exitCodeStatus = commandManager.info(argument);
                    return exitCodeStatus;
                }
                case "show": {
                    exitCodeStatus = commandManager.show(argument);
                    return exitCodeStatus;
                }
                case "add": {
                    exitCodeStatus = commandManager.add(argument);
                    return exitCodeStatus;
                }
                case "update": {
                    exitCodeStatus = commandManager.updateById(argument);
                    return exitCodeStatus;
                }
                case "remove_by_id": {
                    exitCodeStatus = commandManager.removeById(argument);
                    return exitCodeStatus;
                }
                case "clear": {
                    exitCodeStatus = commandManager.clear(argument);
                    return exitCodeStatus;
                }
                case "execute_script": {
                    flagScript = true;
                    commandManager.getExecuteScriptCommand().setConsole(this);
                    exitCodeStatus = commandManager.executeScript(argument);
                    return exitCodeStatus;
                }
                case "exit": {
                    boolean saved = fileManager.writeCollection();

                    // Сортируем перед отправкой
                    collectionManager.sortByName();

                    // Подготавливаем данные для клиента
                    this.loadFileData = fileManager.getCollectionAsBytes();
                    this.loadFileName = currentLoadFile.getName();

                    ExitCodeCommand exitCodeCommand = saved ? ExitCodeCommand.OK : ExitCodeCommand.ERROR;

                    System.out.println("Сервер завершает работу. Коллекция " + (saved ? "сохранена." : "не сохранена!"));
                    return exitCodeCommand;
                }
                case "remove_greater": {
                    exitCodeStatus = commandManager.removeGreater(argument);
                    return exitCodeStatus;
                }
                case "reorder": {
                    exitCodeStatus = commandManager.reorder(argument);
                    return exitCodeStatus;
                }
                case "sort": {
                    exitCodeStatus = commandManager.sort(argument);
                    return exitCodeStatus;
                }
                case "sum_of_engine_power": {
                    exitCodeStatus = commandManager.sumOfEnginePower(argument);
                    return exitCodeStatus;
                }
                case "print_field_ascending_number_of_wheels": {
                    exitCodeStatus = commandManager.printFieldAscendingNumberOfWheels(argument);
                    return exitCodeStatus;
                }
                case "print_field_descending_number_of_wheels": {
                    exitCodeStatus = commandManager.printFieldDescendingNumberOfWheels(argument);
                    return exitCodeStatus;
                }
                default: {
                    if ((mnemonics + argument).isEmpty() || (mnemonics + argument).trim().isEmpty()) {
                        exitCodeStatus = ExitCodeCommand.OK;
                        return exitCodeStatus;
                    }
                    throw new CommandNotExist("Команда " + mnemonics + " не существует!");
                }
            }
        } catch (CommandNotExist e) {
            System.out.println(e.getMessage());
            exitCodeStatus = ExitCodeCommand.ERROR;
            return exitCodeStatus;
        }
    }


        public ExitCodeCommand launchCommand (String mnemonics, String argument, Vehicle vehicle,String FileName, byte[] FileData){
            try {
                switch (mnemonics) {
                    case "help": {
                        exitCodeStatus = commandManager.help(argument, null,null,null);
                        return exitCodeStatus;
                    }
                    case "info": {
                        exitCodeStatus = commandManager.info(argument, null,null,null);
                        return exitCodeStatus;
                    }
                    case "show": {
                        exitCodeStatus = commandManager.show(argument, null,null,null);
                        return exitCodeStatus;
                    }
                    case "add": {
                        exitCodeStatus = commandManager.add(argument, vehicle,null,null);
                        return exitCodeStatus;
                    }
                    case "update": {
                        exitCodeStatus = commandManager.updateById(argument, vehicle,null,null);
                        return exitCodeStatus;
                    }
                    case "remove_by_id": {
                        exitCodeStatus = commandManager.removeById(argument, null,null,null);
                        return exitCodeStatus;
                    }
                    case "clear": {
                        exitCodeStatus = commandManager.clear(argument, null,null,null);
                        return exitCodeStatus;
                    }
                    case "execute_script": {
                        flagScript = true;
                        commandManager.getExecuteScriptCommand().setConsole(this);
                        exitCodeStatus = commandManager.executeScript(argument, null,FileName,FileData);
                        return exitCodeStatus;
                    }
                    case "exit": {
                        exitCodeStatus = commandManager.exit(argument, null,null,null);
                        return exitCodeStatus;
                    }
                    case "remove_greater": {
                        exitCodeStatus = commandManager.removeGreater(argument, vehicle,null,null);
                        return exitCodeStatus;
                    }
                    case "reorder": {
                        exitCodeStatus = commandManager.reorder(argument, null,null,null);
                        return exitCodeStatus;
                    }
                    case "sort": {
                        exitCodeStatus = commandManager.sort(argument, null,null,null);
                        return exitCodeStatus;
                    }
                    case "sum_of_engine_power": {
                        exitCodeStatus = commandManager.sumOfEnginePower(argument, null,null,null);
                        return exitCodeStatus;
                    }
                    case "print_field_ascending_number_of_wheels": {
                        exitCodeStatus = commandManager.printFieldAscendingNumberOfWheels(argument, null,null,null);
                        return exitCodeStatus;
                    }
                    case "print_field_descending_number_of_wheels": {
                        exitCodeStatus = commandManager.printFieldDescendingNumberOfWheels(argument, null,null,null);
                        return exitCodeStatus;
                    }
                    default: {
                        if ((mnemonics + argument).isEmpty() || (mnemonics + argument).trim().isEmpty()) {
                            exitCodeStatus = ExitCodeCommand.OK;
                            return exitCodeStatus;
                        }
                        throw new CommandNotExist("Команда " + mnemonics + " не существует!");
                    }
                }
            } catch (CommandNotExist e) {
                System.out.println(e.getMessage());
                exitCodeStatus = ExitCodeCommand.ERROR;
                return exitCodeStatus;
            }

        }
}