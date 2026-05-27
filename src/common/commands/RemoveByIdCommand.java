package common.commands;

import common.ExitCodeCommand;
import common.exceptions.ValidateDataException;
import common.exceptions.WrongAmountOfElementsException;
import server.utility.CollectionManager;
import client.utility.Validator;
import java.util.NoSuchElementException;

public class RemoveByIdCommand extends AbstractCommand{
    private CollectionManager collectionManager;
    private String argument;

    public RemoveByIdCommand(String argument) {
        super("remove_by_id id","удалить элемент из коллекции по его id");
        this.argument=argument;
    }

    public RemoveByIdCommand() {
        super("remove_by_id id","удалить элемент из коллекции по его id");
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
        ExitCodeCommand valid  = validate();
        if (!valid.equals(ExitCodeCommand.OK)){
            return valid;
        }
        try {
            Integer id = Integer.valueOf(argument);
            collectionManager.removeById(id);
            return ExitCodeCommand.OK;
        }
        catch (IndexOutOfBoundsException e){
            System.out.println("Коллекция пуста!");
            return ExitCodeCommand.OK;
        }
        catch (NoSuchElementException e){
            System.out.println(e.getMessage());
            return ExitCodeCommand.CTRL_D;
        }
    }
    public ExitCodeCommand validate(){
        boolean isEmptyCollection = false;
        try {
            if (argument.isEmpty()) {
                throw new NullPointerException("id не может быть null!");
            }
            if (argument.split("\\s+").length>1){
                throw new WrongAmountOfElementsException("Должен быть только один аргумент - поле 'id'!");
            }
            if (collectionManager.getCollection().size()==0){
                isEmptyCollection=true;
                throw new WrongAmountOfElementsException("Коллекция пуста!");
            }
            Integer id = Integer.valueOf(argument);
            if (Validator.validateIdVehicle(id, collectionManager.getCollection()) == false) {
                throw new ValidateDataException("Введенное поле id не валидно!");
            }
            return ExitCodeCommand.OK;
        }
        catch (NullPointerException e) {
            System.out.println(e.getMessage());
            return ExitCodeCommand.ERROR;
        }
        catch (WrongAmountOfElementsException e){
            System.out.println(e.getMessage());
            if (!isEmptyCollection){
                return ExitCodeCommand.ERROR;
            }
            return ExitCodeCommand.OK;
        }
        catch (NumberFormatException e) {
            System.out.println(e.getMessage() + " : Некорректный ввод поля id!");
            return ExitCodeCommand.ERROR;
        }
        catch (ValidateDataException e) {
            System.out.println(e.getMessage());
            return ExitCodeCommand.ERROR;
        }
    }
}