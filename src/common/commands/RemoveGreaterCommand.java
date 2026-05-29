package common.commands;

import client.utility.FieldReaderClient;
import common.ExitCodeCommand;
import common.exceptions.WrongAmountOfElementsException;
import common.models.Vehicle;
import server.utility.CollectionManager;

public class RemoveGreaterCommand extends AbstractCommand{
    private CollectionManager collectionManager;
    private String argument;
    private Vehicle vehicle;

    //Для метода createCommand из UserHandler
    public RemoveGreaterCommand(String argument,boolean flag) {
        super("remove_greater","удалить из коллекции все элементы, превышающие заданный");
        this.argument=argument;
        if (flag) {
            try {
                this.vehicle = FieldReaderClient.askVehicleObject();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public RemoveGreaterCommand(CollectionManager collectionManager) {
        super("remove_greater","удалить из коллекции все элементы, превышающие заданный");
        this.collectionManager = collectionManager;
    }

    public RemoveGreaterCommand() {
        super("remove_greater","удалить из коллекции все элементы, превышающие заданный");
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

    @Override
    public String getArgument() {
        return argument;
    }

    public Vehicle getVehicle() {
        return vehicle;
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
            collectionManager.removeGreater(vehicle);
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
        catch (Exception e){
            System.out.println(e.getMessage());
            return ExitCodeCommand.ERROR;
        }
    }

    public ExitCodeCommand validate(){
        try{
            if (!argument.isEmpty()){
                throw new WrongAmountOfElementsException("Преждевременный ввод элемента!");
            }
            return ExitCodeCommand.OK;
        }
        catch (WrongAmountOfElementsException e){
            System.out.println(e.getMessage());
            return ExitCodeCommand.ERROR;
        }
    }
}