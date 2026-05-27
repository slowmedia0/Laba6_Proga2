package common.commands;

import client.utility.FieldReaderClient;
import common.ExitCodeCommand;
import common.exceptions.NotExistException;
import common.exceptions.ValidateDataException;
import common.exceptions.WrongAmountOfElementsException;
import common.models.Vehicle;
import server.utility.CollectionManager;
import client.utility.Validator;

public class UpdateIdCommand extends AbstractCommand{
    private String argument;
    private Vehicle vehicle;
    private CollectionManager collectionManager;


    public UpdateIdCommand(String argument){
        super("update id","обновить значение элемента коллекции, id которого равен заданному");
        this.argument=argument;
        try {
            this.vehicle= FieldReaderClient.askVehicleObject();
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    public void setCollectionManager(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    public void setArgument(String argument) {
        this.argument = argument;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public UpdateIdCommand(){
        super("update id","обновить значение элемента коллекции, id которого равен заданному");
    }

    @Override
    public String getArgument() {
        return argument;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public ExitCodeCommand execute() {
        ExitCodeCommand valid = validate();
        if (!valid.equals(ExitCodeCommand.OK)) {
            return valid;
        }
        try {
            Integer id = Integer.valueOf(argument);
            collectionManager.updateElementById(id, vehicle);
            return ExitCodeCommand.OK;
        }
        catch (IndexOutOfBoundsException e){
            System.out.println("Коллекция пуста!");
            return ExitCodeCommand.OK;
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            return ExitCodeCommand.ERROR;
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
            Integer id = Integer.valueOf(argument);
            if (Validator.validateIdVehicle(id, collectionManager.getCollection()) == false) {
                throw new ValidateDataException("Некорректный ввод поля id!");
            }
            if (collectionManager.getCollection().size()==0){
                isEmptyCollection = true;
                throw new WrongAmountOfElementsException("Коллекция пуста!");
            }
            if (collectionManager.existId(id)==false){
                throw new NotExistException("В коллекции нет объектов с таким же id!");
            }
            return ExitCodeCommand.OK;
        } catch (NullPointerException e) {
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
            System.out.println(e.getMessage());
            return ExitCodeCommand.ERROR;
        } catch (ValidateDataException e) {
            System.out.println(e.getMessage());
            return ExitCodeCommand.ERROR;
        }
        catch (NotExistException e){
            System.out.println(e.getMessage());
            return ExitCodeCommand.ERROR;
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            return ExitCodeCommand.ERROR;
        }
    }
}