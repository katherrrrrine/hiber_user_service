package console;

import exception.ValidationException;
import exception.InvalidAgeException;
import exception.InvalidEmailException;
import exception.InvalidNameException;
import validation.UserInputValidator;

import java.util.Scanner;


public class ConsoleHelper {
    private final Scanner scanner;

    public ConsoleHelper() {
        this.scanner = new Scanner(System.in);
    }

    public void close() {
        scanner.close();
    }

    public int getIntInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                int value = Integer.parseInt(scanner.nextLine().trim());
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите целое число");
            }
        }
    }

    public String getStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public String getNameInput() {
        while (true) {
            try {
                String name = getStringInput("Введите имя: ");
                UserInputValidator.validateName(name);
                return name.trim();
            } catch (InvalidNameException e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
    }

    public String getEmailInput() {
        while (true) {
            try {
                String email = getStringInput("Введите email: ");
                UserInputValidator.validateEmail(email);
                return email.trim().toLowerCase();
            } catch (InvalidEmailException e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
    }

    public int getAgeInput() {
        while (true) {
            try {
                String ageStr = getStringInput("Введите возраст: ");
                UserInputValidator.validateAge(ageStr);
                return Integer.parseInt(ageStr);
            } catch (InvalidAgeException e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
    }

    public long getIdInput(String prompt) {
        while (true) {
            try {
                int id = getIntInput(prompt);
                return id;
            } catch (ValidationException e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
    }

    public void printSuccess(String message) {
        System.out.println(message);
    }

    public void printError(String message) {
        System.out.println(message);
    }

    public void printInfo(String message) {
        System.out.println(message);
    }
}