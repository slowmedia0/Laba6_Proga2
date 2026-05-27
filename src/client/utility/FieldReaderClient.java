package client.utility;

import common.ExitCodeCommand;
import common.exceptions.*;
import common.models.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class FieldReaderClient {
    private static UserHandler userHandler;
    private static final Scanner userScanner = new Scanner(System.in);

    public static void setUserHandler(UserHandler userHandler) {
        FieldReaderClient.userHandler = userHandler;
    }


    public static String askFile(){
        try {
            System.out.println("Введите имя файла или его путь");
            if (!userScanner.hasNextLine()) {
                throw new NoSuchElementException("Вы использовали Ctrl+D. Ввод прерван.");
            }
            String data = userScanner.nextLine().trim();
            if (data.trim().split("\\s+").length > 1) {
                throw new WrongAmountOfElementsException("Вы указали больше одного файла, а надо один!");
            }
            return data;
        } catch (NoSuchElementException e) {
            System.out.println(e.getMessage());
            userHandler.setExitCodeStatus(ExitCodeCommand.CTRL_D);
            System.exit(0);
            return null;
        } catch (WrongAmountOfElementsException e) {
            System.out.println(e.getMessage());
            System.out.println("Повторите попытку ввода");
            return askFile();
        }
    }



    public static Vehicle askVehicleObject() throws ValidateDataException, NotExistException, FieldReadException {
        try {
            System.out.println("Для того чтобы заполнить объект типа Vehicle, выполните следующее:");
            return new Vehicle(FieldReaderClient.readFieldName(null), FieldReaderClient.askCoordinates(null, null), FieldReaderClient.readFieldEnginePower(null), FieldReaderClient.readFieldNumberOfWheels(null), FieldReaderClient.readFieldType(null), FieldReaderClient.readFieldFuelType(null));
        } catch (FieldReadException e) {
            throw new FieldReadException("Не удалось инициализировать поля объекта типа Vehicle!");
        }
    }


    public static String readFieldName(String argument) throws ValidateDataException, FieldReadException {
       try {
                System.out.println("Введите строку для поля 'name'");
                if (!userScanner.hasNextLine()) {
                    throw new NoSuchElementException("Вы использовали Ctrl+D. Ввод прерван.");
                }
                String name = userScanner.nextLine();
                int maxLenOfName = 1500;
                if (name.length()>maxLenOfName){
                    throw new ValueOutOfBoundsException("Максимальная длина поля 'name' = " + maxLenOfName);
                }
                Validator.validateNameVehicle(name);
                return name;
            } catch (NoSuchElementException e) {
                System.out.println(e.getMessage());
                userHandler.setExitCodeStatus(ExitCodeCommand.CTRL_D);
                System.exit(0);
                return null;
            }
            catch (ValueOutOfBoundsException e){
                System.out.println(e.getMessage());
                System.out.println("Повторите попытку ввода");
                return readFieldName(null);
            }
            catch (ValidateDataException e) {
                System.out.println(e.generateFullMessage());
                System.out.println("Повторите попытку ввода");
                return readFieldName(null);
            }
    }
    
    public static Coordinates askCoordinates(String argX, String argY) throws ValidateDataException, FieldReadException {
        try {
                System.out.println("Для того чтобы заполнить поле 'coordinates', выполните следующее:");
                Long x = readFieldX("");
                Double y = readFieldY("");
                return new Coordinates(x, y);
            }
            catch (FieldReadException e){
                System.out.println(e.generateFullMessage());
                System.out.println("Повторите попытку ввода");
                return askCoordinates(null,null);
            }
    }
    
    public static Long readFieldX(String argument) throws FieldReadException {
      try {
                System.out.println("Введите целое число для поля 'x'");
                if (!userScanner.hasNextLine()) {
                    throw new NoSuchElementException("Вы использовали Ctrl+D. Ввод прерван.");
                }
                String data = userScanner.nextLine();
                Long x;
                if (data.trim().split("\\s+").length>1){
                    throw new WrongAmountOfElementsException("Для поля 'x' указано более одного аргумента!");
                }
                if (data.isEmpty()) {
                    x = null;
                } else if (data.trim().isEmpty()) {
                    throw new NumberFormatException("Для поля 'x' была введена последовательность, состоящая из 'пустых символов' (табуляция, пробелы и т.п.)!");
                } else {
                    BigDecimal a;
                    try {
                        data=data.replace(",",".").trim();
                        data=data.replaceAll("\\.0+$","");
                        a = new BigDecimal(data);
                    }
                    catch (NumberFormatException e){
                        throw new NumberFormatException("");
                    }
                    if (a.remainder(BigDecimal.ONE) != BigDecimal.ZERO) {
                        throw new NumberFormatException("Поле 'x' не может быть дробным числом!");
                    }
                    BigInteger b = new BigInteger(data);
                    BigInteger startOfBounds = BigInteger.valueOf(Integer.MIN_VALUE);
                    BigInteger endOfBounds = BigInteger.valueOf(Integer.MAX_VALUE);
                    if (b.compareTo(startOfBounds) < 0 || b.compareTo(endOfBounds) > 0) {
                        throw new ValueOutOfBoundsException("Поле 'x' должно находиться в диапазоне: " + startOfBounds + "<=x<=" + endOfBounds);
                    }
                    x = Long.valueOf(data);
                }
                Validator.validateXCoordinates(x);
                return x;
            } catch (NoSuchElementException e) {
                System.out.println(e.getMessage());
                userHandler.setExitCodeStatus(ExitCodeCommand.CTRL_D);
                System.exit(0);
                return null;
            } catch (WrongAmountOfElementsException | NumberFormatException | ValidateDataException e) {
                System.out.println(new FieldReadException("Не удалось считать поле 'x'! Поле 'x' должно быть целым числом!", e).generateFullMessage());
                System.out.println("Повторите попытку ввода");
                return readFieldX(null);
            } catch (ValueOutOfBoundsException e) {
                System.out.println(new FieldReadException("Не удалось считать поле 'x'!", e).generateFullMessage());
                System.out.println("Повторите попытку ввода");
                return readFieldX(null);
            }
    }

  
    public static Double readFieldY(String argument) throws ValidateDataException, FieldReadException {
       try {
                System.out.println("Введите число для поля 'y'");
                if (!userScanner.hasNextLine()) {
                    throw new NoSuchElementException("Вы использовали Ctrl+D. Ввод прерван.");
                }
                String data = userScanner.nextLine();
                Double y;
                if (data.trim().split("\\s+").length>1){
                    throw new WrongAmountOfElementsException("Для поля 'y' указано более одного аргумента!");
                }
                if (data.isEmpty()) {
                    y = null;
                } else if (data.trim().isEmpty()) {
                    throw new NumberFormatException("Для поля 'y' была введена последовательность, состоящая из 'пустых символов' (табуляция, пробелы и т.п.)!");
                } else {
                    BigDecimal b;
                    try {
                        data = data.replace(",", ".").trim();
                        b = new BigDecimal(data);
                    }
                    catch (NumberFormatException e){
                        throw new NumberFormatException("");
                    }
                    BigDecimal startOfBounds = BigDecimal.valueOf(Double.MIN_VALUE);
                    BigDecimal endOfBounds = BigDecimal.valueOf(414);
                    if (b.compareTo(startOfBounds) < 0 || b.compareTo(endOfBounds) > 0) {
                        throw new ValueOutOfBoundsException("Поле 'y' должно находиться в диапазоне: " + startOfBounds + "<=id<=" + endOfBounds);
                    }
                    y = Double.valueOf(data);
                    BigDecimal visualY = new BigDecimal(String.valueOf(y));
                    visualY = visualY.stripTrailingZeros();
                    if (b.stripTrailingZeros().compareTo(visualY)!=0){
                        System.out.println("Предупреждаем, что число потеряло точность! Вот какое число в действительности считалось для поля 'y': " + y);
                        System.out.println("Если вы хотите изменить число, то введите единицу. Если - нет, то введите 0");
                        String dataNew = userScanner.nextLine().trim();
                        while (!dataNew.equals("0") & !dataNew.equals("1")){
                            System.out.println("Если вы хотите изменить число, то введите единицу. Если - нет, то введите 0");
                            dataNew = userScanner.nextLine().trim();
                        }
                        if (dataNew.trim().equals("1")){
                            return readFieldY(null);
                        }
                    }
                }
                Validator.validateYCoordinates(y);
                return y;
            } catch (NoSuchElementException e) {
                System.out.println(e.getMessage());
                userHandler.setExitCodeStatus(ExitCodeCommand.CTRL_D);
                System.exit(0);
                return null;
            } catch (WrongAmountOfElementsException | NumberFormatException | ValidateDataException e) {
                System.out.println(new FieldReadException("Не удалось считать поле 'y'! Поле 'y' должно быть числом!",e).generateFullMessage());
                System.out.println("Повторите попытку ввода");
                return readFieldY(null);
            }
            catch (ValueOutOfBoundsException e ){
                System.out.println(new FieldReadException("Не удалось считать поле 'y'!",e).generateFullMessage());
                System.out.println("Повторите попытку ввода");
                return readFieldY(null);
            }
    }


    public static LocalDate readFieldCreationDate(String argument) throws ValidateDataException, FieldReadException {
        try {
            String data = argument;
            LocalDate creationDate;
            if (data.trim().split("\\s+").length>1){
                throw new WrongAmountOfElementsException("Для поля 'creationDate' указано более одного аргумента!");
            }
            if (data.isEmpty()) {
                creationDate = null;
            } else if (data.trim().isEmpty()) {
                throw new NumberFormatException("Для поля 'creationDate' была введена последовательность, состоящая из 'пустых символов' (табуляция, пробелы и т.п.)!");
            } else if (data.equals("NULL")) {
                creationDate = LocalDate.now();
            } else {
                try {
                    creationDate = LocalDate.parse(argument);
                }
                catch (DateTimeParseException e){
                    throw new IllegalArgumentException("");
                }
            }
            Validator.validateCreationDateVehicle(creationDate);
            return creationDate;
        }
        catch (WrongAmountOfElementsException | IllegalArgumentException | ValidateDataException e){
            throw new FieldReadException("Не удалось считать поле 'creationDate'! Поле 'creationDate' должно представлять собой дату в формате: yyyy-MM-dd !",e);
        }
    }

  
    public static float readFieldEnginePower(String argument) throws ValidateDataException, FieldReadException {
       try {
                System.out.println("Введите число для поля 'enginePower'");
                if (!userScanner.hasNextLine()) {
                    throw new NoSuchElementException("Вы использовали Ctrl+D. Ввод прерван.");
                }
                String data = userScanner.nextLine();
                float enginePower;
                if (data.trim().split("\\s+").length>1){
                    throw new WrongAmountOfElementsException("Для поля 'enginePower' указано более одного аргумента!");
                }
                if (data.isEmpty()) {
                    throw new IllegalArgumentException("Поле 'enginePower' не может быть пустой строкой!");
                }
                else if (data.trim().isEmpty()){
                    throw new NumberFormatException("Для поля 'enginePower' была введена последовательность, состоящая из 'пустых символов' (табуляция, пробелы и т.п.)!");
                }
                else {
                    BigDecimal b;
                    try {
                        data = data.replace(",",".").trim();
                        b = new BigDecimal(data);
                    }
                    catch (NumberFormatException e){
                        throw new NumberFormatException("");
                    }
                    BigDecimal startOfBounds = BigDecimal.ZERO;
                    BigDecimal endOfBounds = BigDecimal.valueOf(Float.MAX_VALUE);
                    if (b.compareTo(startOfBounds) < 0 || b.compareTo(endOfBounds) > 0){
                        throw new ValueOutOfBoundsException("Поле 'enginePower' должно находиться в диапазоне: " + startOfBounds + "<=enginePower<=" + endOfBounds);
                    }
                    /*
                    enginePower = Float.parseFloat(data);
                    if (b.compareTo(BigDecimal.valueOf(enginePower))!=0){
                        System.out.println("Предупреждаем, что число потеряло точность! Вот какое число в действительности считалось для поля 'enginePower': " + enginePower);
                    }
                    */

                    enginePower = Float.parseFloat(data);
                    BigDecimal visualEnginePower = new BigDecimal(String.valueOf(enginePower));
                    visualEnginePower = visualEnginePower.stripTrailingZeros();
                    if (b.stripTrailingZeros().compareTo(visualEnginePower)!=0){
                        System.out.println("Предупреждаем, что число потеряло точность! Вот какое число в действительности считалось для поля 'enginePower': " + enginePower);
                        System.out.println("Если вы хотите изменить число, то введите единицу. Если - нет, то введите 0");
                        String dataNew = userScanner.nextLine().trim();
                        while (!dataNew.equals("0") & !dataNew.equals("1")){
                            System.out.println("Если вы хотите изменить число, то введите единицу. Если - нет, то введите 0");
                            dataNew= userScanner.nextLine().trim();
                        }
                        if (dataNew.equals("1")){
                            return readFieldEnginePower(null);
                        }
                    }
                }
                return enginePower;
            }catch (NoSuchElementException e) {
                System.out.println(e.getMessage());
                userHandler.setExitCodeStatus(ExitCodeCommand.CTRL_D);

                System.exit(0);
                return 0;
            } catch (ValueOutOfBoundsException e) {
                System.out.println(new FieldReadException("Не удалось считать поле 'enginePower'!",e).generateFullMessage());
                System.out.println("Повторите попытку ввода");
                return readFieldEnginePower(null);
            }
            catch (WrongAmountOfElementsException | NumberFormatException e) {
                System.out.println(new FieldReadException("Не удалось считать поле 'enginePower'! Поле 'enginePower' должно быть числом!",e).generateFullMessage());
                System.out.println("Повторите попытку ввода");
                return readFieldEnginePower(null);
            }
            catch (IllegalArgumentException e) {
                System.out.println(new FieldReadException("Не удалось считать поле 'enginePower'! Поле 'enginePower' должно быть числом!",e).generateFullMessage());
                System.out.println("Повторите попытку ввода");
                return readFieldEnginePower(null);
            }
    }

  
    public static Long readFieldNumberOfWheels(String argument) throws ValidateDataException, FieldReadException {
      try {
                System.out.println("Введите целое число для поля 'numberOfWheels'");
                if (!userScanner.hasNextLine()) {
                    throw new NoSuchElementException("Вы использовали Ctrl+D. Ввод прерван.");
                }
                String data = userScanner.nextLine();
                Long numberOfWheels;
                if (data.trim().split("\\s+").length>1){
                    throw new WrongAmountOfElementsException("Для поля 'numberOfWheels' указано более одного аргумента!");
                }
                if (data.isEmpty()) {
                    numberOfWheels = null;
                }
                else if (data.trim().isEmpty()){
                    throw new NumberFormatException("Для поля 'numberOfWheels' была введена последовательность, состоящая из 'пустых символов' (табуляция, пробелы и т.п.)!");
                }
                else {
                    BigDecimal a;
                    try {
                        data = data.replace(",",".").trim();
                        data=data.replaceAll("\\.0+$","");
                        a = new BigDecimal(data);
                    }
                    catch (NumberFormatException e){
                        throw new NumberFormatException("");
                    }
                    if (a.remainder(BigDecimal.ONE)!=BigDecimal.ZERO){
                        throw new NumberFormatException("Поле 'numberOfWheels' не может быть дробным числом!");
                    }
                    BigInteger b = new BigInteger(data);
                    BigInteger startOfBounds = BigInteger.ZERO;
                    BigInteger endOfBounds = BigInteger.valueOf(Integer.MAX_VALUE);
                    if (b.compareTo(startOfBounds) < 0 || b.compareTo(endOfBounds) > 0){
                        throw new ValueOutOfBoundsException("Поле 'numberOfWheels' должно находиться в диапазоне: " + startOfBounds + "<=numberOfWheels<=" + endOfBounds);
                    }
                    numberOfWheels = Long.valueOf(data);
                }
                Validator.validateNumberOfWheelsVehicle(numberOfWheels);
                return numberOfWheels;
            } catch (NoSuchElementException e) {
                System.out.println(e.getMessage());
                userHandler.setExitCodeStatus(ExitCodeCommand.CTRL_D);

                System.exit(0);
                return null;
            } catch (ValueOutOfBoundsException e) {
                System.out.println(new FieldReadException("Не удалось считать поле 'numberOfWheels'!",e).generateFullMessage());
                System.out.println("Повторите попытку ввода");
                return readFieldNumberOfWheels(null);
            }
            catch (WrongAmountOfElementsException | NumberFormatException | ValidateDataException e) {
                System.out.println(new FieldReadException("Не удалось считать поле 'numberOfWheels'! Поле 'numberOfWheels' должно быть целым числом!",e).generateFullMessage());
                System.out.println("Повторите попытку ввода");
                return readFieldNumberOfWheels(null);
            }
    }

    public static VehicleType readFieldType(String argument) throws NotExistException, ValidateDataException, FieldReadException, EnumConstantNotPresentException {
            System.out.println("Введите одно из значений поля 'type': ");
            System.out.println(Arrays.toString(VehicleType.class.getEnumConstants()));
            System.out.println();
            try {
                if (!userScanner.hasNextLine()) {
                    throw new NoSuchElementException("Вы использовали Ctrl+D. Ввод прерван.");
                }
                String data = userScanner.nextLine();
                VehicleType type;
                if (data.trim().split("\\s+").length>1){
                    throw new WrongAmountOfElementsException("Для поля 'type' указано более одного аргумента!");
                }
                if (data.isEmpty()) {
                    type = null;
                } else if (data.trim().isEmpty()) {
                    throw new IllegalArgumentException("Для поля 'type' была введена последовательность, состоящая из 'пустых символов' (табуляция, пробелы и т.п.)!");
                } else {
                    try {
                        type = VehicleType.valueOf(data.trim());
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("Несуществующая константа для поля 'type'!");
                    }
                }
                Validator.validateTypeVehicle(type);
                return type;
            } catch (NoSuchElementException e) {
                System.out.println(e.getMessage());
                userHandler.setExitCodeStatus(ExitCodeCommand.CTRL_D);

                System.exit(0);
                return null;
            } catch (WrongAmountOfElementsException  | IllegalArgumentException e) {
                System.out.println(e.getMessage());
                System.out.println("Повторите попытку ввода");
                return readFieldType("");
            } catch (ValidateDataException e) {
                System.out.println(e.generateFullMessage());
                System.out.println("Повторите попытку ввода");
                return readFieldType("");
            }
    }


    public static FuelType readFieldFuelType(String argument) throws NotExistException, FieldReadException {
        System.out.println("Введите одно из значений поля 'fuelType': ");
        System.out.println(Arrays.toString(FuelType.class.getEnumConstants()));
        System.out.println();
        try {
            if (!userScanner.hasNextLine()) {
                throw new NoSuchElementException("Вы использовали Ctrl+D. Ввод прерван.");
            }
            String data = userScanner.nextLine();
            FuelType fuelType;
            if (data.trim().split("\\s+").length > 1) {
                throw new WrongAmountOfElementsException("Для поля 'fuelType' указано более одного аргумента!");
            }
            if (data.isEmpty()) {
                fuelType = null;
            } else if (data.trim().isEmpty()) {
                throw new IllegalArgumentException("Для поля 'fuelType' была введена последовательность, состоящая из 'пустых символов' (табуляция, пробелы и т.п.)!");
            } else {
                try {
                    fuelType = FuelType.valueOf(data.trim());
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Несуществующая константа для поля 'fuelType'!");
                }
            }
            return fuelType;
        } catch (NoSuchElementException e) {
            System.out.println(e.getMessage());
            userHandler.setExitCodeStatus(ExitCodeCommand.CTRL_D);

            System.exit(0);
            return null;
        } catch (WrongAmountOfElementsException | IllegalArgumentException e) {
            System.out.println(e.getMessage());
            System.out.println("Повторите попытку ввода");
            return readFieldFuelType("");
        }
    }
}