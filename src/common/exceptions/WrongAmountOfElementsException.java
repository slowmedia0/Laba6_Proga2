package common.exceptions;

/**
 * Исключение, выбрасываемое при неправильном количестве аргументов команды.
 */
public class WrongAmountOfElementsException extends Exception {

    /**
     * Создаёт исключение с указанным сообщением.
     *
     * @param message описание ошибки
     */
    public WrongAmountOfElementsException(String message) {
        super(message);
    }
}