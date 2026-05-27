package common.commands;

import client.utility.FieldReaderClient;
import client.utility.Validator;
import common.ExitCodeCommand;
import common.exceptions.WrongAmountOfElementsException;
import server.utility.Console;
import server.utility.FileManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ExecuteScriptCommand extends AbstractCommand{
    private Console console;
    private String argument;
    private  String FileName;
    private  byte[] FileData;

    public ExecuteScriptCommand(String argument) {
        super("execute_script file_name","считать и исполнить скрипт из указанного файла. В скрипте содержатся команды в таком же виде, в котором их вводит пользователь в интерактивном режиме.");
        this.argument=argument;
        String nameOfFile=argument;
        while (Validator.validateNameOfFile(nameOfFile, FileManager.ModeOfFileManager.READ_SCRIPT)==false){
            nameOfFile=FieldReaderClient.askFile();
        }
        this.FileName=nameOfFile;
        try{
            File file = new File(nameOfFile);
            this.FileData = Files.readAllBytes(file.toPath());
            System.out.println("Скрипт-файл прочитан: " + nameOfFile + " (" + FileData.length + " байт)");
        } catch (IOException e) {
            System.err.println("Ошибка чтения скрипта-файла: " + e.getMessage());
            this.FileData = new byte[0];
        }
    }

    public ExecuteScriptCommand() {
        super("execute_script file_name","считать и исполнить скрипт из указанного файла. В скрипте содержатся команды в таком же виде, в котором их вводит пользователь в интерактивном режиме.");
    }

    public String getFileName() {
        return FileName;
    }

    public byte[] getFileData() {
        return FileData;
    }

    public void setArgument(String argument) {
        this.argument = argument;
    }

    @Override
    public String getArgument() {
        return argument;
    }

    public void setConsole(Console console) {
        this.console = console;
    }


    public ExitCodeCommand execute(){
        ExitCodeCommand valid  = validate();
        if (!valid.equals(ExitCodeCommand.OK)){
            return valid;
        }
        return console.scriptMode(FileName,FileData);
    }

    public ExitCodeCommand validate(){
        try{
            if (argument.isEmpty()){
                throw new IllegalArgumentException("Пустой аргумент!");
            }
            if (argument.split("\\s+").length>1){
                throw new WrongAmountOfElementsException("Должен быть только один аргумент - файл-скрипт!");
            }
            if (argument.contains("\\")){
                argument.replace("\\","\\\\");
            }
            return ExitCodeCommand.OK;
        } catch (WrongAmountOfElementsException e){
            System.out.println(e.getMessage());
            return ExitCodeCommand.ERROR;
        }
        catch (IllegalArgumentException e){
            System.out.println(e.getMessage());
            return ExitCodeCommand.ERROR;
        }
    }
}