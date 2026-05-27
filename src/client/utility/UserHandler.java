package client.utility;

import client.UDPClient;
import common.ExitCodeCommand;
import common.commands.*;
import common.exceptions.CommandNotExist;
import common.interaction.Response;
import server.utility.FileManager;

import java.io.File;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class UserHandler {
    public ExitCodeCommand exitCodeStatus = ExitCodeCommand.CTRL_C;
    private UDPClient udpClient;
    private Scanner userScanner;


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

    public void interactiveMode(String nameOfLoadFile) {
        //Считаем путь или имя загрузочного файла
        String nameOfFile=nameOfLoadFile;
        while (Validator.validateNameOfFile(nameOfFile, FileManager.ModeOfFileManager.READ_COLLECTION)==false){
            nameOfFile= FieldReaderClient.askFile();
        }

        //Отправляем загрузочного файл на сервер
        sendLoadFileToServer(nameOfFile);


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

                //Формирование объект-команду
                Command commandObject = createCommand(command.get(0), command.get(1));

                //Валидация команды и Отправка команды на сервер
                if (commandObject != null & commandObject.validate().equals(ExitCodeCommand.OK)){
                    udpClient.sendRequest(createCommandRequest(commandObject));
                    if (commandObject.getNameOfCommand().equals("exit")){
                        //Получаем файл от сервера с измененной коллекцией
                        //....

                        if (!commandObject.execute().equals(ExitCodeCommand.EXIT)){
                            System.out.println("Не удалось выполнить команду " + commandObject.getNameOfCommand() + " " + commandObject.getArgument() + "!");
                        }
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
                    return new CommandRequest(command.getNameOfCommand(),command.getArgument(),executeScriptCommand.getFile());
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

    private void sendLoadFileToServer(String fileName) {
        try {
            File file = new File(fileName);
            System.out.println("Отправка загрузочного файла на сервер: " + fileName);
            CommandRequest loadRequest = new CommandRequest("load_file", fileName, file);
            Response response = udpClient.sendRequest(loadRequest);
            if (response != null && response.isSuccess()) {
                System.out.println("Загрузочный-файл успешно отправлен и загружен на сервер.");
            } else {
                System.out.println("Предупреждение: файл отправлен, но сервер вернул ошибку.");
            }

        } catch (Exception e) {
            System.out.println("Ошибка при отправке загрузочного файла на сервер: " + e.getMessage());
        }
    }


}