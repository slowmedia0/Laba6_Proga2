package common.commands;

import common.ExitCodeCommand;
import common.exceptions.WrongAmountOfElementsException;
import server.utility.CollectionManager;


public class PrintFieldAscendingNumberOfWheelsCommand extends AbstractCommand {

    private CollectionManager collectionManager;
    private String argument;

    public PrintFieldAscendingNumberOfWheelsCommand(String argument) {
        super("print_field_ascending_number_of_wheels","вывести значения поля numberOfWheels всех элементов в порядке возрастания");
        this.argument=argument;
    }

    public PrintFieldAscendingNumberOfWheelsCommand() {
        super("print_field_ascending_number_of_wheels","вывести значения поля numberOfWheels всех элементов в порядке возрастания");
    }

    public void setCollectionManager(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    public void setArgument(String argument) {
        this.argument = argument;
    }

    @Override
    public String getArgument() {
        return argument;
    }

    public ExitCodeCommand execute(){
        ExitCodeCommand valid  = validate();
        if (!valid.equals(ExitCodeCommand.OK)){
            return valid;
        }
        try{
            collectionManager.printAscendingNumberOfWheels();
            return ExitCodeCommand.OK;
        }
        catch (IndexOutOfBoundsException e){
            System.out.println("Коллекция пуста!");
            return ExitCodeCommand.OK;
        }
    }

    public ExitCodeCommand validate(){
        boolean isEmptyCollection = false;
        try{
            if (!argument.isEmpty()){
                throw new WrongAmountOfElementsException(getNameOfCommand() + " не принимает аргументов!");
            }
            if (collectionManager.getCollection().size()==0){
                isEmptyCollection=true;
                throw new WrongAmountOfElementsException("Коллекция пуста!");
            }
            return ExitCodeCommand.OK;
        }
        catch (WrongAmountOfElementsException e){
            System.out.println(e.getMessage());
            if (!isEmptyCollection){
                return ExitCodeCommand.ERROR;
            }
            return ExitCodeCommand.OK;
        }
    }
}