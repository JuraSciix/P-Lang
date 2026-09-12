package plang.translator;

/**
 * Ошибка внутри транслятора.
 */
public class TranslatorException extends RuntimeException{
    public TranslatorException() {
        super();
    }

    public TranslatorException(String message) {
        super(message);
    }

    public TranslatorException(String message, Throwable cause) {
        super(message, cause);
    }

    public TranslatorException(Throwable cause) {
        super(cause);
    }
}
