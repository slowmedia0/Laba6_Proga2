package common.exceptions;

/**
 * Исключение, выбрасываемое при отсутствии требуемого объекта или значения.
 */
public class NotExistException extends Exception {

    /**
     * Создаёт исключение с указанным сообщением.
     *
     * @param message описание ошибки
     */
    public NotExistException(String message) {
        super(message);
    }
}