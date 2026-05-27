package common.commands;

import client.utility.UserHandler;
import common.ExitCodeCommand;
import common.exceptions.WrongAmountOfElementsException;
import server.utility.FileManager;

public class ExitCommand extends AbstractCommand{
    private FileManager fileManager;
    private UserHandler userHandler;
    private String argument;

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
        try{
            userHandler.setExitCodeStatus(ExitCodeCommand.EXIT);
            System.exit(0);
            return ExitCodeCommand.EXIT;
        }  catch (Exception e){
            System.out.println("Неожиданная ошибка: " + e.getMessage());
            return ExitCodeCommand.ERROR;
        }
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