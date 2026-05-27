package common.exceptions;

/**
 * Исключение, выбрасываемое при попытке вызова несуществующей команды.
 */
public class CommandNotExist extends NotExistException {

    /**
     * Создаёт исключение с указанным сообщением.
     *
     * @param message описание ошибки
     */
    public CommandNotExist(String message) {
        super(message);
    }
}