package server.utility;

import common.ExitCodeCommand;
import common.exceptions.*;
import common.models.*;
import common.utility.ResponseBuilder;


import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Класс для чтения и проверки полей объектов на сервере.
 * Весь вывод теперь идёт через ResponseBuilder.
 */
public class FieldReaderServer {

    private static final Scanner userScanner = new Scanner(System.in);
    private static Console console;

    public static void setConsole(Console console) {
        FieldReaderServer.console = console;
    }

    // ==================== Вспомогательные методы вывода ====================

    private static void printIfNotScript(String message) {
        if (!console.isFlagScript() && !console.isFlagReadCollection()) {
            ResponseBuilder.append(message);
        }
    }

    private static void printErrorIfNotScript(String message) {
        if (!console.isFlagScript() && !console.isFlagReadCollection()) {
            ResponseBuilder.appendLn(message);
        }
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
            System.exit(0);
            return null;
        } catch (WrongAmountOfElementsException e) {
            System.out.println(e.getMessage());
            System.out.println("Повторите попытку ввода");
            return askFile();
        }
    }

    // ==================== Основные методы ====================

    public static Integer readFieldId(String argument, CollectionManager collectionManager) throws FieldReadException {
        try {
            String data = argument;
            Integer id;

            if (data.trim().split("\\s+").length > 1) {
                throw new WrongAmountOfElementsException("Для поля 'id' указано более одного аргумента!");
            }

            if (data.isEmpty()) {
                id = null;
            } else if (data.trim().isEmpty()) {
                throw new NumberFormatException("Для поля 'id' была введена последовательность, состоящая из 'пустых символов'!");
            } else if (data.equals("NULL")) {
                id = collectionManager.generateId(collectionManager.getArrayId());
            } else {
                BigDecimal a = new BigDecimal(data.replace(",", ".").trim().replaceAll("\\.0+$", ""));
                if (a.remainder(BigDecimal.ONE) != BigDecimal.ZERO) {
                    throw new NumberFormatException("Поле 'id' не может быть дробным числом!");
                }
                BigInteger b = new BigInteger(data);
                if (b.compareTo(BigInteger.ONE) < 0 || b.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0) {
                    throw new ValueOutOfBoundsException("Поле 'id' должно находиться в диапазоне: 1 <= id <= " + Integer.MAX_VALUE);
                }
                id = Integer.valueOf(data);
            }

            ValidatorServer.validateIdVehicle(id);
            collectionManager.getArrayId().add(id);
            return id;

        } catch (WrongAmountOfElementsException | NumberFormatException | ValidateDataException | ValueOutOfBoundsException e) {
            printErrorIfNotScript("Не удалось считать поле 'id'!");
            throw new FieldReadException("Не удалось считать поле 'id'!", e);
        }
    }

    public static Vehicle askVehicleObject() throws ValidateDataException, NotExistException, FieldReadException {
        try {
            printIfNotScript("Для того чтобы заполнить объект типа Vehicle, выполните следующее:");
            return new Vehicle(
                    FieldReaderServer.readFieldName(null),
                    FieldReaderServer.askCoordinates(null, null),
                    FieldReaderServer.readFieldEnginePower(null),
                    FieldReaderServer.readFieldNumberOfWheels(null),
                    FieldReaderServer.readFieldType(null),
                    FieldReaderServer.readFieldFuelType(null)
            );
        } catch (FieldReadException e) {
            printErrorIfNotScript("Не удалось инициализировать поля объекта типа Vehicle!");
            throw new FieldReadException("Не удалось инициализировать поля объекта типа Vehicle!");
        }
    }

    public static String readFieldName(String argument) throws ValidateDataException, FieldReadException {
        try {
            String name;
            if (console.isFlagReadCollection()) {
                name = argument;
                if (name.equals("NULL")) {
                    throw new NotExistException("Отсутствует тэг 'name'! Поле 'name' не инициализировано!");
                }
            } else {
                name = console.getFields().get(0);
            }

            int maxLenOfName = 1500;
            if (name.length() > maxLenOfName) {
                throw new ValueOutOfBoundsException("Максимальная длина поля 'name' = " + maxLenOfName);
            }

            ValidatorServer.validateNameVehicle(name);
            return name;

        } catch (ValueOutOfBoundsException | NotExistException | ValidateDataException e) {
            printErrorIfNotScript("Не удалось считать поле 'name'!");
            throw new FieldReadException("Не удалось считать поле 'name'!", e);
        }
    }

    public static Coordinates askCoordinates(String argX, String argY) throws ValidateDataException, FieldReadException {
        try {
            Long x = readFieldX(argX);
            Double y = readFieldY(argY);
            Coordinates coordinates = new Coordinates(x, y);
            ValidatorServer.validateCoordinatesVehicle(coordinates);
            return coordinates;
        } catch (FieldReadException e) {
            printErrorIfNotScript("Не удалось считать поле 'coordinates'!");
            throw new FieldReadException("Не удалось считать поле 'coordinates'!", e);
        }
    }

    public static Long readFieldX(String argument) throws FieldReadException {
        try {
            String data;
            Long x;
            if (console.isFlagReadCollection()) {
                data = argument;
                if (data.equals("NULL")) {
                    throw new NotExistException("Отсутствует тэг 'x'! Поле 'x' не инициализировано!");
                }
            } else {
                data = console.getFields().get(1);
            }

            if (data.trim().split("\\s+").length > 1) {
                throw new WrongAmountOfElementsException("Для поля 'x' указано более одного аргумента!");
            }

            if (data.isEmpty() || data.trim().isEmpty()) {
                throw new NumberFormatException("Для поля 'x' была введена пустая последовательность!");
            }

            BigDecimal a = new BigDecimal(data.replace(",", ".").trim().replaceAll("\\.0+$", ""));
            if (a.remainder(BigDecimal.ONE) != BigDecimal.ZERO) {
                throw new NumberFormatException("Поле 'x' не может быть дробным числом!");
            }

            BigInteger b = new BigInteger(data);
            if (b.compareTo(BigInteger.valueOf(Integer.MIN_VALUE)) < 0 ||
                    b.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0) {
                throw new ValueOutOfBoundsException("Поле 'x' должно находиться в диапазоне: " + Integer.MIN_VALUE + " <= x <= " + Integer.MAX_VALUE);
            }

            x = Long.valueOf(data);
            ValidatorServer.validateXCoordinates(x);
            return x;

        } catch (Exception e) {
            printErrorIfNotScript("Не удалось считать поле 'x'!");
            throw new FieldReadException("Не удалось считать поле 'x'!", e);
        }
    }

    public static Double readFieldY(String argument) throws ValidateDataException, FieldReadException {
        try {
            String data;
            Double y;
            if (console.isFlagReadCollection()) {
                data = argument;
                if (data.equals("NULL")) {
                    throw new NotExistException("Отсутствует тэг 'y'! Поле 'y' не инициализировано!");
                }
            } else {
                data = console.getFields().get(2);
            }

            if (data.trim().split("\\s+").length > 1) {
                throw new WrongAmountOfElementsException("Для поля 'y' указано более одного аргумента!");
            }

            if (data.isEmpty() || data.trim().isEmpty()) {
                throw new NumberFormatException("Для поля 'y' была введена пустая последовательность!");
            }

            BigDecimal b = new BigDecimal(data.replace(",", ".").trim());
            BigDecimal endOfBounds = BigDecimal.valueOf(414);

            if (b.compareTo(endOfBounds) > 0) {
                throw new ValueOutOfBoundsException("Поле 'y' должно быть <= 414");
            }

            y = Double.valueOf(data);
            ValidatorServer.validateYCoordinates(y);
            return y;

        } catch (Exception e) {
            printErrorIfNotScript("Не удалось считать поле 'y'!");
            throw new FieldReadException("Не удалось считать поле 'y'!", e);
        }
    }

    public static LocalDate readFieldCreationDate(String argument) throws ValidateDataException, FieldReadException {
        try {
            String data = argument;
            LocalDate creationDate;

            if (data.trim().split("\\s+").length > 1) {
                throw new WrongAmountOfElementsException("Для поля 'creationDate' указано более одного аргумента!");
            }

            if (data.isEmpty() || data.trim().isEmpty()) {
                creationDate = LocalDate.now();
            } else if (data.equals("NULL")) {
                creationDate = LocalDate.now();
            } else {
                creationDate = LocalDate.parse(data);
            }

            ValidatorServer.validateCreationDateVehicle(creationDate);
            return creationDate;

        } catch (Exception e) {
            printErrorIfNotScript("Не удалось считать поле 'creationDate'!");
            throw new FieldReadException("Не удалось считать поле 'creationDate'!", e);
        }
    }

    public static float readFieldEnginePower(String argument) throws ValidateDataException, FieldReadException {
        try {
            String data;
            if (console.isFlagReadCollection()) {
                data = argument;
                if (data.equals("NULL")) {
                    throw new NotExistException("Отсутствует тэг 'enginePower'!");
                }
            } else {
                data = console.getFields().get(3);
            }

            if (data.trim().split("\\s+").length > 1) {
                throw new WrongAmountOfElementsException("Для поля 'enginePower' указано более одного аргумента!");
            }

            float enginePower = Float.parseFloat(data.replace(",", ".").trim());
            return enginePower;

        } catch (Exception e) {
            printErrorIfNotScript("Не удалось считать поле 'enginePower'!");
            throw new FieldReadException("Не удалось считать поле 'enginePower'!", e);
        }
    }

    public static Long readFieldNumberOfWheels(String argument) throws ValidateDataException, FieldReadException {
        try {
            String data;
            if (console.isFlagReadCollection()) {
                data = argument;
                if (data.equals("NULL")) {
                    throw new NotExistException("Отсутствует тэг 'numberOfWheels'!");
                }
            } else {
                data = console.getFields().get(4);
            }

            if (data.trim().split("\\s+").length > 1) {
                throw new WrongAmountOfElementsException("Для поля 'numberOfWheels' указано более одного аргумента!");
            }

            Long numberOfWheels = Long.valueOf(data);
            ValidatorServer.validateNumberOfWheelsVehicle(numberOfWheels);
            return numberOfWheels;

        } catch (Exception e) {
            printErrorIfNotScript("Не удалось считать поле 'numberOfWheels'!");
            throw new FieldReadException("Не удалось считать поле 'numberOfWheels'!", e);
        }
    }

    public static VehicleType readFieldType(String argument) throws NotExistException, ValidateDataException, FieldReadException {
        try {
            String data;
            if (console.isFlagReadCollection()) {
                data = argument;
                if (data.equals("NULL")) {
                    throw new NotExistException("Отсутствует тэг 'type'!");
                }
            } else {
                data = console.getFields().get(5);
            }

            VehicleType type = VehicleType.valueOf(data.trim());
            ValidatorServer.validateTypeVehicle(type);
            return type;

        } catch (Exception e) {
            printErrorIfNotScript("Не удалось считать поле 'type'!");
            throw new FieldReadException("Не удалось считать поле 'type'!", e);
        }
    }

    public static FuelType readFieldFuelType(String argument) throws NotExistException, FieldReadException {
        try {
            String data;
            if (console.isFlagReadCollection()) {
                data = argument;
                if (data.equals("NULL")) {
                    throw new NotExistException("Отсутствует тэг 'fuelType'!");
                }
            } else {
                data = console.getFields().get(6);
            }

            FuelType fuelType = FuelType.valueOf(data.trim());
            return fuelType;

        } catch (Exception e) {
            printErrorIfNotScript("Не удалось считать поле 'fuelType'!");
            throw new FieldReadException("Не удалось считать поле 'fuelType'!", e);
        }
    }

    public static Integer askPort(String argument)  {
        try {
            System.out.println("Введите целое число для поля 'port'");
            if (!userScanner.hasNextLine()) {
                throw new NoSuchElementException("Вы использовали Ctrl+D. Ввод прерван.");
            }
            String data = userScanner.nextLine();
            Integer port;
            if (data.trim().split("\\s+").length > 1) {
                throw new WrongAmountOfElementsException("Для поля 'port' указано более одного аргумента!");
            }
            if (data.isEmpty()) {
                throw new NumberFormatException("Поле 'port' не может быть null!");
            } else if (data.trim().isEmpty()) {
                throw new NumberFormatException("Для поля 'port' была введена последовательность, состоящая из 'пустых символов' (табуляция, пробелы и т.п.)!");
            } else {
                BigDecimal a;
                try {
                    data = data.replace(",", ".").trim();
                    data = data.replaceAll("\\.0+$", "");
                    a = new BigDecimal(data);
                } catch (NumberFormatException e) {
                    throw new NumberFormatException("");
                }
                if (a.remainder(BigDecimal.ONE) != BigDecimal.ZERO) {
                    throw new NumberFormatException("Поле 'port' не может быть дробным числом!");
                }
                BigInteger b = new BigInteger(data);
                BigInteger startOfBounds = BigInteger.valueOf(1024);
                BigInteger endOfBounds = BigInteger.valueOf(65535);
                if (b.compareTo(startOfBounds) < 0 || b.compareTo(endOfBounds) > 0) {
                    throw new ValueOutOfBoundsException("Поле 'port' должно находиться в диапазоне: " + startOfBounds + "<=port<=" + endOfBounds);
                }
                port = Integer.valueOf(data);
            }
            return port;
        } catch (NoSuchElementException e) {
            System.out.println(e.getMessage());
            System.exit(0);
            return null;
        } catch (WrongAmountOfElementsException | NumberFormatException e) {
            System.out.println("Не удалось считать поле 'port'! Поле 'port' должно быть целым числом!" + " " + e.getMessage());
            System.out.println("Повторите попытку ввода");
            return askPort(null);
        } catch (ValueOutOfBoundsException e) {
            System.out.println("Не удалось считать поле 'port'!" + " " + e.getMessage());
            System.out.println("Повторите попытку ввода");
            return askPort(null);
        }
    }

    public static Integer readPort(String argument)  {
        try {
            String data = argument;
            Integer port;
            if (data.trim().split("\\s+").length > 1) {
                throw new WrongAmountOfElementsException("Для поля 'port' указано более одного аргумента!");
            }
            if (data.isEmpty()) {
                throw new NumberFormatException("Поле 'port' не может быть null!");
            } else if (data.trim().isEmpty()) {
                throw new NumberFormatException("Для поля 'port' была введена последовательность, состоящая из 'пустых символов' (табуляция, пробелы и т.п.)!");
            } else {
                BigDecimal a;
                try {
                    data = data.replace(",", ".").trim();
                    data = data.replaceAll("\\.0+$", "");
                    a = new BigDecimal(data);
                } catch (NumberFormatException e) {
                    throw new NumberFormatException("");
                }
                if (a.remainder(BigDecimal.ONE) != BigDecimal.ZERO) {
                    throw new NumberFormatException("Поле 'port' не может быть дробным числом!");
                }
                BigInteger b = new BigInteger(data);
                BigInteger startOfBounds = BigInteger.valueOf(1024);
                BigInteger endOfBounds = BigInteger.valueOf(65535);
                if (b.compareTo(startOfBounds) < 0 || b.compareTo(endOfBounds) > 0) {
                    throw new ValueOutOfBoundsException("Поле 'port' должно находиться в диапазоне: " + startOfBounds + "<=port<=" + endOfBounds);
                }
                port = Integer.valueOf(data);
            }
            return port;
        } catch (WrongAmountOfElementsException | NumberFormatException e) {
            System.out.println("Не удалось считать поле 'port'! Поле 'port' должно быть целым числом!" + " " + e.getMessage());
            return askPort(null);
        } catch (ValueOutOfBoundsException e) {
            System.out.println("Не удалось считать поле 'port'!" + " " + e.getMessage());
            return askPort(null);
        }
    }
}