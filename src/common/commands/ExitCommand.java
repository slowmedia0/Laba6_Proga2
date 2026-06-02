package common.commands;

import client.utility.UserHandler;
import common.ExitCodeCommand;
import common.exceptions.WrongAmountOfElementsException;
import common.interaction.Response;
import server.utility.FileManager;

public class ExitCommand extends AbstractCommand{
    private FileManager fileManager;
    private UserHandler userHandler;
    private String argument;
    private  String FileName;
    private  String FileData;


    //Для метода createCommand из UserHandler
    public ExitCommand(String argument) {
        super("exit","завершить программу (без сохранения в файл)");
        this.argument=argument;
    }

    public ExitCommand() {
        super("exit","завершить программу (без сохранения в файл)");
    }


    public void setArgument(String argument) {
        this.argument = argument;
    }

    public void setFileManager(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    public void setUserHandler(UserHandler userHandler) {
        this.userHandler = userHandler;
    }

    public String getArgument() {
        return argument;
    }


    public ExitCodeCommand execute(){
        ExitCodeCommand valid  = validate();
        if (!valid.equals(ExitCodeCommand.OK)){
            return valid;
        }

        // === Логика сервера ===
        if (fileManager != null) {
            try {
                println("Выполняется сохранение коллекции...");

                SaveCommand saveCommand = new SaveCommand(fileManager);
                ExitCodeCommand saveResult = saveCommand.execute();

                if (!saveResult.equals(ExitCodeCommand.OK)) {
                    println("Предупреждение: не удалось сохранить коллекцию.");
                } else {
                    println("Коллекция успешно сохранена.");
                }

                // Подготавливаем ответ для клиента
                byte[] rawFileData = fileManager.getCollectionAsBytes();
                String fileName = fileManager.getLoadFile().getName();

                String fileDataBase64 = null;
                if (rawFileData != null) {
                    fileDataBase64 = java.util.Base64.getEncoder().encodeToString(rawFileData);
                }

                Response customResponse = new Response(ExitCodeCommand.EXIT,
                        "Клиент успешно отключён.\nКоллекция сохранена на сервере.",
                        "exit", fileName, fileDataBase64);

                return ExitCodeCommand.EXIT;

            } catch (Exception e) {
                println("Ошибка при сохранении коллекции: " + e.getMessage());
                return ExitCodeCommand.ERROR;
            }
        }

        // === Логика клиента ===
        println("Клиент завершает работу...");



        return ExitCodeCommand.EXIT;
    }

    public ExitCodeCommand validate(){
        try{
            if (!argument.isEmpty()){
                throw new WrongAmountOfElementsException(getNameOfCommand() + " не принимает аргументов!");
            }
            return ExitCodeCommand.OK;
        } catch (WrongAmountOfElementsException e){
            System.out.println(e.getMessage());
            return ExitCodeCommand.ERROR;
        }
    }


}