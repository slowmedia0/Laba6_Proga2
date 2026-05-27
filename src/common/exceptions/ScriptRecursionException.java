package common.exceptions;

/**
 * Исключение, выбрасываемое при обнаружении рекурсивного вызова скриптов.
 */
public class ScriptRecursionException extends Exception {

    /**
     * Создаёт исключение с указанным сообщением.
     *
     * @param message описание ошибки
     */
    public ScriptRecursionException(String message) {
        super(message);
    }
}