package common.commands;

import common.ExitCodeCommand;
import common.exceptions.WrongAmountOfElementsException;
import server.utility.CollectionManager;


public class PrintFieldAscendingNumberOfWheelsCommand extends AbstractCommand {

    private CollectionManager collectionManager;
    private String argument;

    //Для метода createCommand из UserHandler
    public PrintFieldAscendingNumberOfWheelsCommand(String argument) {
        super("print_field_ascending_number_of_wheels","вывести значения поля numberOfWheels всех элементов в порядке возрастания");
        this.argument=argument;
    }

    public PrintFieldAscendingNumberOfWheelsCommand() {
        super("print_field_ascending_number_of_wheels","вывести значения поля numberOfWheels всех элементов в порядке возрастания");
    }

    public PrintFieldAscendingNumberOfWheelsCommand(CollectionManager collectionManager) {
        super("print_field_ascending_number_of_wheels","вывести значения поля numberOfWheels всех элементов в порядке возрастания");
        this.collectionManager = collectionManager;
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
            if (collectionManager.getCollection().size()==0){
                throw new WrongAmountOfElementsException("Коллекция пуста!");
            }
            collectionManager.printAscendingNumberOfWheels();
            return ExitCodeCommand.OK;
        }
        catch (WrongAmountOfElementsException e){
            System.out.println(e.getMessage());
            return ExitCodeCommand.OK;
        }
        catch (IndexOutOfBoundsException e){
            System.out.println("Коллекция пуста!");
            return ExitCodeCommand.OK;
        }
    }

    public ExitCodeCommand validate(){
        try{
            if (!argument.isEmpty()){
                throw new WrongAmountOfElementsException(getNameOfCommand() + " не принимает аргументов!");
            }
            return ExitCodeCommand.OK;
        }
        catch (WrongAmountOfElementsException e){
            System.out.println(e.getMessage());
            return ExitCodeCommand.ERROR;
        }
    }
}