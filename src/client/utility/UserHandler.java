package client.utility;

import client.UDPClient;
import common.ExitCodeCommand;
import common.commands.*;
import common.exceptions.CommandNotExist;
import common.interaction.Response;
import server.utility.FileManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class UserHandler {
    public ExitCodeCommand exitCodeStatus = ExitCodeCommand.CTRL_C;
    private UDPClient udpClient;
    private Scanner userScanner;
    private  String loadFileName;      // имя загрузочного файла
    private  byte[] loadFileData;      // содержимое загрузочного файла (для exit/save)


    public ExitCodeCommand getExitCodeStatus() {
        return exitCodeStatus;
    }

    public void setExitCodeStatus(ExitCodeCommand exitCodeStatus) {
        this.exitCodeStatus = exitCodeStatus;
    }

    public UserHandler(UDPClient udpClient, Scanner userScanner) {
        this.udpClient = udpClient;
        this.userScanner = userScanner;
    }

    public String getLoadFileName() {
        return loadFileName;
    }

    public byte[] getLoadFileData() {
        return loadFileData;
    }

    public void interactiveMode(String nameOfLoadFile) {
        //Считаем путь или имя загрузочного файла
        String nameOfFile=nameOfLoadFile;
        while (Validator.validateNameOfFile(nameOfFile, FileManager.ModeOfFileManager.READ_COLLECTION)==false){
            nameOfFile= FieldReaderClient.askFile();
        }
        this.loadFileName=nameOfFile;
        try{
            File file = new File(nameOfFile);
            this.loadFileData = Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            System.err.println("Ошибка чтения загрузочного файла: " + e.getMessage());
            this.loadFileData = new byte[0]; // пустой массив в случае ошибки
        }


        //Отправляем загрузочного файл на сервер
        sendLoadFileToServer();


        //Считывание команд с терминала пользователя
        try {
            while (true) {
                System.out.println("Введите команду");
                ArrayList<String> command = new ArrayList<>(2);
                if (!userScanner.hasNextLine()) {
                    throw new NoSuchElementException("Вы использовали Ctrl+D. Ввод прерван.");
                }
                for (var i : userScanner.nextLine().trim().split("\\s+", 2)) {
                    command.add(i);
                }
                if (command.size() ==1) {
                    command.add("");
                }


                // Создание объекта команды
                Command commandObject = createCommand(command.get(0), command.get(1));

                if (commandObject == null) continue;

                // Валидация команды
                if (!commandObject.validate().equals(ExitCodeCommand.OK)) {
                    System.out.println("Команда не валидна");
                    continue;
                }

                // Создание запроса и отправка на сервер
                CommandRequest request = createCommandRequest(commandObject);
                Response response = udpClient.sendRequest(request);

                if ("exit".equalsIgnoreCase(commandObject.getNameOfCommand())) {
                    handleExitResponse(response);
                    return;
                }

                // Вывод ответа сервера (для остальных команд)
                if (response != null) {
                    if (response.getMessage() != null && !response.getMessage().isEmpty()) {
                        System.out.println(response.getMessage());
                    }
                }

            }
        }
        catch(NoSuchElementException e){
            System.out.println(e.getMessage());
            exitCodeStatus= ExitCodeCommand.CTRL_D;
            System.exit(0);
        }
    }

    public Command createCommand(String mnemonics, String argument){
        try {
            switch (mnemonics) {
                case "help": {
                    return new HelpCommand(argument);
                }
                case "info": {
                    return new InfoCommand(argument);
                }
                case "show": {
                    return new ShowCommand(argument);
                }
                case "add": {
                    return new AddCommand(argument);
                }
                case "update": {
                    return new UpdateIdCommand(argument);
                }
                case "remove_by_id": {
                    return new RemoveByIdCommand(argument);
                }
                case "clear": {
                    return new ClearCommand(argument);
                }
                case "execute_script": {
                    return new ExecuteScriptCommand(argument);
                }
                case "exit": {
                    return new ExitCommand(argument);
                }
                case "remove_greater": {
                    return new RemoveGreaterCommand(argument);
                }
                case "reorder": {
                    return new ReorderCommand(argument);
                }
                case "sort": {
                    return new SortCommand(argument);
                }
                case "sum_of_engine_power": {
                    return new SumOfEnginePowerCommand(argument);
                }
                case "print_field_ascending_number_of_wheels": {
                    return new PrintFieldAscendingNumberOfWheelsCommand(argument);
                }
                case "print_field_descending_number_of_wheels": {
                    return new PrintFieldDescendingNumberOfWheelsCommand(argument);
                }
                default: {
                    if (!((mnemonics + argument).isEmpty() || (mnemonics + argument).trim().isEmpty())) {
                        throw new CommandNotExist("Команда " + mnemonics + " не существует!");
                    }
                    return null;
                }
            }
        }
        catch (CommandNotExist e){
            System.out.println(e.getMessage());
            return null;
        }
    }

    public CommandRequest createCommandRequest(Command command){
            switch (command.getNameOfCommand()) {
                case "help", "remove_by_id", "info", "show", "clear", "reorder", "sort",
                     "sum_of_engine_power", "print_field_ascending_number_of_wheels",
                     "print_field_descending_number_of_wheels": {
                    return new CommandRequest(command.getNameOfCommand(),command.getArgument());
                }
                case "execute_script": {
                    ExecuteScriptCommand executeScriptCommand = (ExecuteScriptCommand) command;
                    return new CommandRequest(command.getNameOfCommand(),command.getArgument(),executeScriptCommand.getFileName(),executeScriptCommand.getFileData());
                }
                case "add": {
                    AddCommand addCommand = (AddCommand) command;
                    return new CommandRequest(command.getNameOfCommand(),command.getArgument(),addCommand.getVehicle());
                }
                case "update": {
                    UpdateIdCommand updateIdCommand = (UpdateIdCommand) command;
                    return new CommandRequest(command.getNameOfCommand(),command.getArgument(),updateIdCommand.getVehicle());
                }
                case "remove_greater": {
                    RemoveGreaterCommand removeGreaterCommand = (RemoveGreaterCommand) command;
                    return new CommandRequest(command.getNameOfCommand(),command.getArgument(),removeGreaterCommand.getVehicle());
                }
                default: {
                    return null;
                }
            }
    }

    private void sendLoadFileToServer() {
        try {
            System.out.println("Отправка загрузочного файла на сервер: " + loadFileName);
            CommandRequest loadRequest = new CommandRequest("load_file", loadFileName, loadFileName, loadFileData);
            Response response = udpClient.sendRequest(loadRequest);
            if (response != null && response.isSuccess()) {
                System.out.println("Загрузочный файл успешно отправлен и обработан сервером.");
            } else {
                System.out.println("Предупреждение: файл отправлен, но сервер вернул ошибку.");
            }

        } catch (Exception e) {
            System.out.println("Ошибка при отправке загрузочного файла: " + e.getMessage());
        }
    }

    /**
     * Обработка ответа на команду exit
     */
    private void handleExitResponse(Response response) {
        if (response != null) {
            System.out.println(response.getMessage());

            if (response.getFileData() != null && response.getFileName() != null) {
                try {
                    // Перезаписываем локальный загрузочный файл
                    Files.write(new File(loadFileName).toPath(), response.getFileData());
                    System.out.println("Файл коллекции успешно обновлён: " + loadFileName);
                } catch (IOException e) {
                    System.out.println("Не удалось сохранить файл на клиенте: " + e.getMessage());
                }
            }
        } else {
            System.out.println("Сервер не ответил при выходе.");
        }

        System.out.println("Клиент завершает работу.");
        udpClient.close();
        exitCodeStatus = ExitCodeCommand.EXIT;
        System.exit(0);
    }

}