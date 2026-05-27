package common.exceptions;

/**
 * Исключение, выбрасываемое при выходе значения за допустимые границы.
 */
public class ValueOutOfBoundsException extends Exception {

    /**
     * Создаёт исключение с указанным сообщением.
     *
     * @param message описание ошибки
     */
    public ValueOutOfBoundsException(String message) {
        super(message);
    }
}