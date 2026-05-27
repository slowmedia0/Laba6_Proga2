package common.commands;

import common.ExitCodeCommand;
import common.exceptions.WrongAmountOfElementsException;
import server.utility.CollectionManager;


public class SumOfEnginePowerCommand extends AbstractCommand{
    private CollectionManager collectionManager;
    private String argument;

    public SumOfEnginePowerCommand(String argument) {
        super("sum_of_engine_power","вывести сумму значений поля enginePower для всех элементов коллекции");
        this.argument=argument;
    }

    public SumOfEnginePowerCommand() {
        super("sum_of_engine_power","вывести сумму значений поля enginePower для всех элементов коллекции");
    }

    public void setCollectionManager(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public String getArgument() {
        return argument;
    }

    public void setArgument(String argument) {
        this.argument = argument;
    }

    public ExitCodeCommand execute(){
        ExitCodeCommand valid = validate();
        if (!valid.equals(ExitCodeCommand.OK)) {
            return valid;
        }
        try{
            collectionManager.sumEnginePower();
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