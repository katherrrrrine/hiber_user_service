package exception;

public class InvalidNameException extends ValidationException {

    public InvalidNameException(String message) {
        super(message);
    }
}