package common.exceptions;

/**
 * Исключение, выбрасываемое при ошибке чтения поля.
 */
public class FieldReadException extends Exception {

    /**
     * Создаёт исключение с сообщением.
     *
     * @param message описание ошибки
     */
    public FieldReadException(String message) {
        super(message);
    }

    /**
     * Создаёт исключение с причиной.
     *
     * @param message описание ошибки
     * @param cause причина ошибки
     */
    public FieldReadException(String message, Throwable cause){
        super(message, cause);
    }

    /**
     * Генерирует полное сообщение об ошибке.
     *
     * @return строка с описанием ошибки
     */
    public String generateFullMessage() {
        return generateFullMessage(this);
    }

    /**
     * Генерирует цепочку сообщений об ошибках.
     *
     * @param throwable исключение
     * @return объединённое сообщение
     */
    public static String generateFullMessage(Throwable throwable) {
        if (throwable == null) {
            return " ";
        }

        StringBuilder sb = new StringBuilder();
        Throwable current = throwable;
        boolean isFirst = true;

        while (current != null) {
            String message = current.getMessage();

            if (message != null && !message.trim().isEmpty()) {
                if (!isFirst) {
                    sb.append(" -> ");
                }
                sb.append(message.trim());
                isFirst = false;
            }
            Throwable cause = current.getCause();
            if (cause == current) break;
            current = cause;
        }

        return sb.toString();
    }
}