package validation;

import exception.InvalidAgeException;
import exception.InvalidEmailException;
import exception.InvalidNameException;

public class UserInputValidator {

    private static final int MIN_AGE = 9;
    private static final int MAX_AGE = 120;

    private UserInputValidator() {}

    public static void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidNameException("Имя не может быть пустым");
        }

        String trimmedName = name.trim();

        if (!trimmedName.matches("^[a-zA-Zа-яА-Я\\s-]+$")) {
            throw new InvalidNameException("Имя может содержать только буквы, пробелы и дефис");
        }
    }

    public static void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new InvalidEmailException("Email не может быть пустым");
        }

        String trimmedEmail = email.trim().toLowerCase();

        int atIndex = trimmedEmail.indexOf('@');
        if (atIndex == -1) {
            throw new InvalidEmailException("Email должен содержать символ @");
        }

        String localPart = trimmedEmail.substring(0, atIndex);
        String domain = trimmedEmail.substring(atIndex + 1);

        if (localPart.length() < 1) {
            throw new InvalidEmailException("Слишком короткое имя пользователя в email");
        }

        if (!domain.contains(".")) {
            throw new InvalidEmailException("Домен должен содержать точку");
        }

        if (!trimmedEmail.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            throw new InvalidEmailException("Email содержит недопустимые символы");
        }
    }


    public static void validateAge(String ageStr) {
        if (ageStr == null || ageStr.trim().isEmpty()) {
            throw new InvalidAgeException("Возраст не может быть пустым");
        }

        int age;
        try {
            age = Integer.parseInt(ageStr.trim());
        } catch (NumberFormatException e) {
            throw new InvalidAgeException("Возраст должен быть целым числом");
        }

        validateAge(age);
    }


    public static void validateAge(int age) {
        if (age < MIN_AGE) {
            throw new InvalidAgeException("Возраст должен быть не меньше " + MIN_AGE + " лет");
        }

        if (age > MAX_AGE) {
            throw new InvalidAgeException("Возраст должен быть не больше " + MAX_AGE + " лет");
        }
    }

}