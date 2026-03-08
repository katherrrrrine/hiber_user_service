package exception;

public class InvalidEmailException extends ValidationException {

    public InvalidEmailException(String message) {
        super(message);
    }
}