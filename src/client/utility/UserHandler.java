package client.utility;

import client.UDPClient;
import common.ExitCodeCommand;
import common.commands.*;
import common.exceptions.CommandNotExist;
import common.exceptions.ScriptRecursionException;
import common.exceptions.ValidateDataException;
import common.interaction.Response;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class UserHandler {
    public ExitCodeCommand ExitCodeCommandStatus = ExitCodeCommand.CTRL_C;
    private UDPClient udpClient;
    private Scanner userScanner;
    private  String loadFileName;
    private  byte[] loadFileData;
    private ArrayList<String> arguments;
    private boolean flagScript;
    private boolean flagReadCollection;
    private ArrayList<String> fields = new ArrayList<>(7);
    private FileManagerClient fileManagerClient;

    public ExitCodeCommand getExitCodeCommandStatus() {
        return ExitCodeCommandStatus;
    }

    public void setExitCodeCommandStatus(ExitCodeCommand ExitCodeCommandStatus) {
        this.ExitCodeCommandStatus = ExitCodeCommandStatus;
    }

    public UserHandler(UDPClient udpClient, Scanner userScanner, FileManagerClient fileManagerClient) {
        this.udpClient = udpClient;
        this.userScanner = userScanner;
        this.fileManagerClient = fileManagerClient;
        this.arguments=new ArrayList<>();
    }
    private Response sendAndCheck(CommandRequest request) {
        Response response = udpClient.sendRequest(request);
        if (response == null || !response.isSuccess()) {
            if (response != null && response.getMessage() != null) {
                System.out.println(response.getMessage());
            } else {
                System.out.println("Сервер временно недоступен.");
            }
            System.out.println("Клиент завершает работу.");
            udpClient.close();
            System.exit(1);
        }
        return response;
    }

    public UDPClient getUdpClient() {
        return udpClient;
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

    public boolean isFlagReadCollection() {
        return flagReadCollection;
    }

    public boolean isFlagScript() {
        return flagScript;
    }

    public ArrayList<String> getArguments() {
        return arguments;
    }

    public ExitCodeCommand scriptMode(String argument){
        ExitCodeCommand flagSuccessExecute = ExitCodeCommand.OK;
        try {
            int n=-1;
            boolean flagElemCommand = true;
            int index=n;
            arguments.add(argument);
            String mnemonics = "";
            String arg = "";
            if (fileManagerClient.readScript(argument)==null){
                throw new NullPointerException("");
            }
            for (var maybeCommand: fileManagerClient.readScript(argument)){
                try {
                    n += 1;
                    ArrayList<String> command = new ArrayList<>(2);
                    for (var i : maybeCommand.trim().split("\\s+", 2)) {
                        command.add(i);
                    }
                    if (command.size() ==1) {
                        command.add("");
                    }

                    if (flagElemCommand == true) {
                        if (command.get(0).equals("execute_script") && arguments.contains(command.get(1))) {
                            File file1 = new File(argument);
                            for (int i = 0; i < arguments.size(); i++) {
                                File file2 = new File(arguments.get(i));
                                if (file1.getAbsolutePath().equals(file2.getAbsolutePath())) {
                                    String errorMsg = "Не удалось выполнить без ошибок команду " + command.get(0) + " " + command.get(1)
                                            + " в скрипте " + argument + " ! Рекурсивный вызов скрипта '" + command.get(1) + "'!";
                                    System.out.println(errorMsg);
                                    throw new ScriptRecursionException(errorMsg);
                                }
                            }
                        } else if (command.get(0).equals("add") || command.get(0).equals("update") || command.get(0).equals("remove_greater")) {
                            flagElemCommand = false;
                            index = n + 7;
                            mnemonics = command.get(0);
                            arg = command.get(1);
                        } else if (executeCommandFromScript(command.get(0), command.get(1)).equals(ExitCodeCommand.OK) == false) {
                            if (command.get(0).equals("execute_script")) {
                                System.out.println("Не удалось выполнить без ошибок команду " + command.get(0) + " " + command.get(1) + " в скрипте " + argument + " !");
                            } else {
                                System.out.println("Не удалось выполнить команду " + command.get(0) + " " + command.get(1) + " в скрипте " + argument + " !");
                            }
                            flagSuccessExecute = ExitCodeCommand.ERROR;
                        } else {
                            System.out.println();
                        }
                    } else {
                        fields.add(maybeCommand);
                        if (n == index) {
                            ExitCodeCommand result = executeCommandWithVehicle(mnemonics, arg, new ArrayList<>(fields));

                            if (result != ExitCodeCommand.OK) {
                                System.out.println("Не удалось выполнить команду " + mnemonics + " " + arg + " в скрипте " + argument + " !");
                                flagSuccessExecute = ExitCodeCommand.ERROR;
                            } else {
                                System.out.println();
                            }

                            fields.clear();
                            flagElemCommand = true;
                        }
                    }
                }
                catch (ScriptRecursionException e){
                    flagSuccessExecute=ExitCodeCommand.ERROR;
                }
            }
        }
        catch (IllegalStateException | NullPointerException | IndexOutOfBoundsException e){
            System.out.println(e.getMessage() != null ? e.getMessage() : "Ошибка при выполнении скрипта");
            flagSuccessExecute= ExitCodeCommand.ERROR;
        }
        return flagSuccessExecute;
    }

    private ExitCodeCommand executeCommandWithVehicle(String mnemonics, String argument, ArrayList<String> objectFields) {
        Command commandObject = createCommand(mnemonics, argument);
        if (commandObject == null) {
            return ExitCodeCommand.ERROR;
        }

        try {
            if (commandObject instanceof AddCommand) {
                ((AddCommand) commandObject).setVehicle(FieldReaderClient.askVehicleObject());
            } else if (commandObject instanceof UpdateIdCommand) {
                ((UpdateIdCommand) commandObject).setVehicle(FieldReaderClient.askVehicleObject());
            } else if (commandObject instanceof RemoveGreaterCommand) {
                ((RemoveGreaterCommand) commandObject).setVehicle(FieldReaderClient.askVehicleObject());
            }
        } catch (Exception e) {
            System.out.println("Ошибка создания объекта Vehicle: " + e.getMessage());
            return ExitCodeCommand.ERROR;
        }

        try {
            if (!commandObject.validate().equals(ExitCodeCommand.OK)) {
                throw new ValidateDataException("Команда '" + mnemonics + "' " + argument + " не валидна!");
            }
        }
        catch (ValidateDataException e){
            System.out.println(e.getMessage());
            return ExitCodeCommand.ERROR;
        }

        CommandRequest request = createCommandRequest(commandObject);
        Response response = sendAndCheck(request);   // ← минимальное изменение

        if (response != null && response.getMessage() != null && !response.getMessage().isEmpty()) {
            System.out.println("   " + response.getMessage());
        }

        return (response != null && response.isSuccess()) ? ExitCodeCommand.OK : ExitCodeCommand.ERROR;
    }

    public void interactiveMode(String nameOfLoadFile) {
        String nameOfFile=nameOfLoadFile;
        while (ValidatorClient.validateNameOfFile(nameOfFile, FileManagerClient.ModeOfFileManager.READ_COLLECTION)==false){
            nameOfFile= FieldReaderClient.askFile();
        }
        this.loadFileName=nameOfFile;
        try{
            File file = new File(nameOfFile);
            this.loadFileData = Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            System.out.println("Ошибка чтения загрузочного файла: " + e.getMessage());
            this.loadFileData = new byte[0];
        }

        sendLoadFileToServer();
        flagReadCollection=false;

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

                Command commandObject = createCommand(command.get(0), command.get(1));

                if (commandObject == null) {continue;}

                try {
                    if (!commandObject.validate().equals(ExitCodeCommand.OK)) {
                        throw new ValidateDataException("Команда '" + command.get(0) + "' " + command.get(1) + " не валидна!");
                    }
                }
                catch (ValidateDataException e){
                    System.out.println(e.getMessage());
                    continue;
                }

                if ("execute_script".equalsIgnoreCase(commandObject.getNameOfCommand())) {
                    flagScript=true;
                    ExecuteScriptCommand executeScriptCommand = (ExecuteScriptCommand) commandObject;
                    executeScriptCommand.setUserHandler(this);
                    if (!executeScriptCommand.execute().equals(ExitCodeCommand.OK)){
                        System.out.println("Не удалось выполнить без ошибок команду " + command.get(0) + " " + command.get(1) +  " !");
                    }
                    continue;
                }

                CommandRequest request = createCommandRequest(commandObject);
                Response response = sendAndCheck(request);   // ← минимальное изменение

                if ("exit".equalsIgnoreCase(commandObject.getNameOfCommand())) {
                    handleExitResponse(response);
                    return;
                }

                if (response != null && response.getMessage() != null && !response.getMessage().isEmpty()) {
                    System.out.println(response.getMessage());
                }
            }
        }
        catch(NoSuchElementException e){
            System.out.println(e.getMessage());
            ExitCodeCommandStatus= ExitCodeCommand.CTRL_D;
            handleExitResponse(udpClient.sendRequest(createCommandRequest(createCommand("exit",""))));
            System.exit(0);
        }
    }

    private ExitCodeCommand executeCommandFromScript(String mnemonics, String argument) {
        Command commandObject = createCommand(mnemonics, argument);
        if (commandObject == null) {
            return ExitCodeCommand.ERROR;
        }

        try {
            if (!commandObject.validate().equals(ExitCodeCommand.OK)) {
                throw new ValidateDataException("Команда '" + mnemonics + "' " + argument + " не валидна!");
            }
        } catch (ValidateDataException e) {
            System.out.println(e.getMessage());
            return ExitCodeCommand.ERROR;
        }

        if ("execute_script".equalsIgnoreCase(mnemonics)) {
            ExecuteScriptCommand esc = (ExecuteScriptCommand) commandObject;
            flagScript=true;
            esc.setUserHandler(this);
            String path = esc.getFileName() != null ? esc.getFileName() : argument;
            return scriptMode(path);
        }

        CommandRequest request = createCommandRequest(commandObject);
        Response response = sendAndCheck(request);   // ← минимальное изменение

        if ("exit".equalsIgnoreCase(mnemonics)) {
            handleExitResponse(response);
            return ExitCodeCommand.EXIT;
        }

        if (response != null && response.getMessage() != null && !response.getMessage().isEmpty()) {
            System.out.println("   " + response.getMessage());
        }

        return (response != null && response.isSuccess()) ? ExitCodeCommand.OK : ExitCodeCommand.ERROR;
    }

    private void sendLoadFileToServer() {
        try {
            System.out.println("Отправка загрузочного файла на сервер: " + loadFileName);
            CommandRequest loadRequest = new CommandRequest("load_file", loadFileName, loadFileName, loadFileData);
            Response response = sendAndCheck(loadRequest);   // ← минимальное изменение

            if (response != null && response.isSuccess()) {
                System.out.println("Загрузочный файл успешно доставлен на сервер.");
            }
        } catch (Exception e) {
            System.out.println("Ошибка отправки загрузочного файла: " + e.getMessage());
        }
    }

    public Command createCommand(String mnemonics, String argument){
        try {
            switch (mnemonics) {
                case "help": return new HelpCommand(argument);
                case "info": return new InfoCommand(argument);
                case "show": return new ShowCommand(argument);
                case "add": return new AddCommand(argument, !flagScript);
                case "update": return new UpdateIdCommand(argument, !flagScript);
                case "remove_by_id": return new RemoveByIdCommand(argument);
                case "clear": return new ClearCommand(argument);
                case "exit": return new ExitCommand(argument);
                case "execute_script": return new ExecuteScriptCommand(argument);
                case "remove_greater": return new RemoveGreaterCommand(argument, !flagScript);
                case "reorder": return new ReorderCommand(argument);
                case "sort": return new SortCommand(argument);
                case "sum_of_engine_power": return new SumOfEnginePowerCommand(argument);
                case "print_field_ascending_number_of_wheels": return new PrintFieldAscendingNumberOfWheelsCommand(argument);
                case "print_field_descending_number_of_wheels": return new PrintFieldDescendingNumberOfWheelsCommand(argument);
                default:
                    if (!((mnemonics + argument).trim().isEmpty())) {
                        throw new CommandNotExist("Команда " + mnemonics + " не существует!");
                    }
                    return null;
            }
        } catch (CommandNotExist e){
            System.out.println(e.getMessage());
            return null;
        }
    }

    public CommandRequest createCommandRequest(Command command){
        switch (command.getNameOfCommand()) {
            case "help", "remove_by_id", "exit", "info", "show", "clear", "reorder", "sort",
                 "sum_of_engine_power", "print_field_ascending_number_of_wheels",
                 "print_field_descending_number_of_wheels":
                return new CommandRequest(command.getNameOfCommand(),command.getArgument());
            case "add":
                return new CommandRequest(command.getNameOfCommand(),command.getArgument(),((AddCommand) command).getVehicle());
            case "update":
                return new CommandRequest(command.getNameOfCommand(),command.getArgument(),((UpdateIdCommand) command).getVehicle());
            case "remove_greater":
                return new CommandRequest(command.getNameOfCommand(),command.getArgument(),((RemoveGreaterCommand) command).getVehicle());
            default:
                return null;
        }
    }

    public void handleExitResponse(Response response) {
        if (response == null) {
            System.out.println("Сервер не вернул ответ при выходе.");
            udpClient.close();
            System.exit(0);
            return;
        }

        System.out.println(response.getMessage());

        if (response.getFileData() != null && response.getFileData().length > 0 && loadFileName != null) {
            try {
                Files.write(new File(loadFileName).toPath(), response.getFileData());
                System.out.println("Обновленную коллекцию можете увидеть в файле: " + loadFileName);
            } catch (IOException e) {
                System.out.println("Не удалось сохранить файл на клиенте: " + e.getMessage());
            }
        }
        System.out.println("Клиент завершает работу.");
        udpClient.close();
        ExitCodeCommandStatus = ExitCodeCommand.EXIT;
        System.exit(0);
    }
}