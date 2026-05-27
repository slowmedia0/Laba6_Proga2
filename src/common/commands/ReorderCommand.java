package common.commands;

import common.ExitCodeCommand;
import common.exceptions.WrongAmountOfElementsException;
import server.utility.CollectionManager;

public class ReorderCommand extends AbstractCommand{
    private CollectionManager collectionManager;
    private String argument;

    public ReorderCommand(String argument) {
        super("reorder","отсортировать коллекцию в порядке, обратном нынешнему");
        this.argument = argument;
    }

    public ReorderCommand() {
        super("reorder","отсортировать коллекцию в порядке, обратном нынешнему");
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
        collectionManager.reorderCollection();
        return ExitCodeCommand.OK;
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
        } catch (WrongAmountOfElementsException e){
            System.out.println(e.getMessage());
            if (!isEmptyCollection){
                return ExitCodeCommand.ERROR;
            }
            return ExitCodeCommand.OK;
        }
    }
}