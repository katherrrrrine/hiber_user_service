package validation;

import exception.InvalidAgeException;
import exception.InvalidEmailException;
import exception.InvalidNameException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static org.junit.jupiter.api.Assertions.*;

class UserInputValidationTest {

    @Test
    void validateName_ValidName_ShouldNotThrowException() {
        assertDoesNotThrow(() -> UserInputValidator.validateName("John Doe"));
        assertDoesNotThrow(() -> UserInputValidator.validateName("Анна-Мария"));
        assertDoesNotThrow(() -> UserInputValidator.validateName("  Петр  "));
        assertDoesNotThrow(() -> UserInputValidator.validateName("Jean-Luc"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void validateName_NullOrEmpty_ShouldThrowException(String name) {
        assertThrows(InvalidNameException.class,
                () -> UserInputValidator.validateName(name));
    }

    @ParameterizedTest
    @ValueSource(strings = {"John123", "John@Doe", "John#Doe", "John_Doe"})
    void validateName_InvalidCharacters_ShouldThrowException(String name) {
        assertThrows(InvalidNameException.class,
                () -> UserInputValidator.validateName(name));
    }

    @Test
    void validateEmail_ValidEmail_ShouldNotThrowException() {
        assertDoesNotThrow(() -> UserInputValidator.validateEmail("test@example.com"));
        assertDoesNotThrow(() -> UserInputValidator.validateEmail("user.name@domain.co.uk"));
        assertDoesNotThrow(() -> UserInputValidator.validateEmail("user+tag@example.com"));
        assertDoesNotThrow(() -> UserInputValidator.validateEmail("  TEST@EXAMPLE.COM  "));
        assertDoesNotThrow(() -> UserInputValidator.validateEmail("user123@test-domain.com"));
        assertDoesNotThrow(() -> UserInputValidator.validateEmail("user@subdomain.example.com"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    void validateEmail_NullOrEmpty_ShouldThrowException(String email) {
        assertThrows(InvalidEmailException.class,
                () -> UserInputValidator.validateEmail(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "invalid.email",
            "invalid@",
            "user@domain"
    })
    void validateEmail_InvalidFormat_ShouldThrowException(String email) {
        assertThrows(InvalidEmailException.class,
                () -> UserInputValidator.validateEmail(email));
    }

    @Test
    void validateAge_ValidAge_ShouldNotThrowException() {
        assertDoesNotThrow(() -> UserInputValidator.validateAge("25"));
        assertDoesNotThrow(() -> UserInputValidator.validateAge("9"));
        assertDoesNotThrow(() -> UserInputValidator.validateAge("120"));
        assertDoesNotThrow(() -> UserInputValidator.validateAge(25));
        assertDoesNotThrow(() -> UserInputValidator.validateAge(9));
        assertDoesNotThrow(() -> UserInputValidator.validateAge(120));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    void validateAge_NullOrEmpty_ShouldThrowException(String age) {
        assertThrows(InvalidAgeException.class,
                () -> UserInputValidator.validateAge(age));
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "12.5", "12,5", "12.5.6", "-5", "0"})
    void validateAge_NotInteger_ShouldThrowException(String age) {
        assertThrows(InvalidAgeException.class,
                () -> UserInputValidator.validateAge(age));
    }

    @Test
    void validateAge_OutOfRange_ShouldThrowException() {
        assertThrows(InvalidAgeException.class, () -> UserInputValidator.validateAge("8"));
        assertThrows(InvalidAgeException.class, () -> UserInputValidator.validateAge("121"));
        assertThrows(InvalidAgeException.class, () -> UserInputValidator.validateAge("0"));
        assertThrows(InvalidAgeException.class, () -> UserInputValidator.validateAge("-1"));
    }
}