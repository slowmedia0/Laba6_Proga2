package common.commands;

import common.ExitCodeCommand;
import common.exceptions.WrongAmountOfElementsException;
import server.utility.CollectionManager;
import common.utility.ResponseBuilder;

public class ShowCommand extends AbstractCommand {
    private CollectionManager collectionManager;
    private String argument;

    
    public ShowCommand(String argument) {
        super("show", "вывести в стандартный поток вывода все элементы коллекции в строковом представлении");
        this.argument = argument;
    }

    public ShowCommand() {
        super("show", "вывести в стандартный поток вывода все элементы коллекции в строковом представлении");
    }

    public ShowCommand(CollectionManager collectionManager) {
        super("show","вывести в стандартный поток вывода все элементы коллекции в строковом представлении");
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

    public ExitCodeCommand execute() {
        ExitCodeCommand valid = validate();
        if (!valid.equals(ExitCodeCommand.OK)) {
            return valid;
        }
        try {
            if (collectionManager.getCollection().size() == 0) {
                throw new WrongAmountOfElementsException("Коллекция пуста!");
            }
            collectionManager.showElementsOfCollection();
            return ExitCodeCommand.OK;
        } catch (IndexOutOfBoundsException e) {
            ResponseBuilder.appendLn("Коллекция пуста!");
            return ExitCodeCommand.OK;
        }
        catch (WrongAmountOfElementsException e) {
            ResponseBuilder.appendLn(e.getMessage());
            return ExitCodeCommand.OK;
        }
    }

    public ExitCodeCommand validate() {
        try {
            if (!argument.isEmpty()) {
                throw new WrongAmountOfElementsException(getNameOfCommand() + " не принимает аргументов!");
            }
            return ExitCodeCommand.OK;
        } catch (WrongAmountOfElementsException e) {
            System.out.println(e.getMessage());
            ResponseBuilder.appendLn(e.getMessage());
            return ExitCodeCommand.ERROR;
        }
    }
}