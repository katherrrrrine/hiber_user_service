import console.ConsoleHelper;
import dao.UserDaoImpl;
import dto.UserDto;
import exception.UserDaoException;
import exception.UserNotFoundException;
import exception.ValidationException;
import service.UserService;
import util.HibernateUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import validation.UserInputValidator;

import java.util.List;
import java.util.Optional;

public class Main {

    private static final Logger logger = LogManager.getLogger(Main.class);
    private static final UserService userService = new UserService(new UserDaoImpl());
    private static final ConsoleHelper console = new ConsoleHelper();

    public static void main(String[] args) {
        logger.info("Application started");
        console.printInfo("Добро пожаловать в систему управления пользователями!");

        try {
            boolean running = true;
            while (running) {
                printMenu();
                int choice = console.getIntInput("Выберите действие: ");

                try {
                    switch (choice) {
                        case 1:
                            createUser();
                            break;
                        case 2:
                            findUserById();
                            break;
                        case 3:
                            findAllUsers();
                            break;
                        case 4:
                            updateUser();
                            break;
                        case 5:
                            deleteUser();
                            break;
                        case 6:
                            running = false;
                            console.printInfo("Завершение работы.");
                            break;
                        default:
                            console.printError("Неверный выбор. Попробуйте снова.");
                    }
                } catch (UserNotFoundException e) {
                    console.printError("Пользователь не найден: " + e.getMessage());
                    logger.warn("User not found: {}", e.getMessage());
                } catch (ValidationException e) {
                    console.printError("Ошибка валидации: " + e.getMessage());
                    logger.warn("Validation error: {}", e.getMessage());
                } catch (UserDaoException e) {
                    console.printError("Ошибка базы данных. Проверьте логи.");
                    logger.error("Database error: {}", e.getMessage());
                } catch (Exception e) {
                    console.printError("Неизвестная ошибка: " + e.getMessage());
                    logger.error("Unexpected error: ", e);
                }
            }
        } finally {
            HibernateUtil.shutdown();
            console.close();
            logger.info("Application shut down");
        }
    }

    private static void printMenu() {
        System.out.println("USER-SERVICE");
        System.out.println("1. Создать пользователя");
        System.out.println("2. Найти пользователя по ID");
        System.out.println("3. Показать всех пользователей");
        System.out.println("4. Обновить пользователя");
        System.out.println("5. Удалить пользователя");
        System.out.println("6. Выход");
    }

    private static void createUser() {
        console.printInfo("Создание нового пользователя");

        String name = console.getNameInput();
        String email = console.getEmailInput();
        int age = console.getAgeInput();

        UserDto userDto = new UserDto();
        userDto.setName(name);
        userDto.setEmail(email);
        userDto.setAge(age);

        UserDto savedUser = userService.createUser(userDto);
        console.printSuccess("Пользователь успешно создан с ID: " + savedUser.getId());
        logger.info("User created with ID: {}", savedUser.getId());
    }

    private static void findUserById() {
        console.printInfo("Поиск пользователя по ID");

        long id = console.getIdInput("Введите ID пользователя: ");
        Optional<UserDto> userOpt = userService.findUserById(id);

        if (userOpt.isPresent()) {
            UserDto user = userOpt.get();
            printUserDetails(user);
        } else {
            console.printError("Пользователь с ID " + id + " не найден");
        }
    }

    private static void findAllUsers() {
        console.printInfo("Список всех пользователей");

        List<UserDto> users = userService.findAllUsers();

        if (users.isEmpty()) {
            console.printInfo("Пользователей нет");
        } else {
            System.out.println("\n Найдено пользователей: " + users.size());
            for (UserDto user : users) {
                printUserCompact(user);
            }
        }
    }

    private static void updateUser() {
        console.printInfo("Обновление пользователя");

        long id = console.getIdInput("Введите ID пользователя для обновления: ");
        Optional<UserDto> userOpt = userService.findUserById(id);

        if (userOpt.isPresent()) {
            UserDto user = userOpt.get();
            System.out.println("\nТекущие данные:");
            printUserDetails(user);
            System.out.println("\n(Оставьте поле пустым, чтобы не менять)");

            String name = console.getStringInput("Новое имя (" + user.getName() + "): ");
            if (!name.isEmpty()) {
                try {
                    UserInputValidator.validateName(name);
                    user.setName(name);
                } catch (ValidationException e) {
                    console.printError("Имя не изменено: " + e.getMessage());
                }
            }

            String email = console.getStringInput("Новый email (" + user.getEmail() + "): ");
            if (!email.isEmpty()) {
                try {
                    UserInputValidator.validateEmail(email);
                    user.setEmail(email.toLowerCase());
                } catch (ValidationException e) {
                    console.printError("Email не изменен: " + e.getMessage());
                }
            }

            String ageStr = console.getStringInput("Новый возраст (" + user.getAge() + "): ");
            if (!ageStr.isEmpty()) {
                try {
                    UserInputValidator.validateAge(ageStr);
                    user.setAge(Integer.parseInt(ageStr));
                } catch (ValidationException e) {
                    console.printError("Возраст не изменен: " + e.getMessage());
                }
            }

            UserDto updatedUser = userService.updateUser(user);
            console.printSuccess("Пользователь обновлен");
            printUserDetails(updatedUser);

        } else {
            console.printError("Пользователь с ID " + id + " не найден");
        }
    }

    private static void deleteUser() {
        console.printInfo("Удаление пользователя");

        long id = console.getIdInput("Введите ID пользователя для удаления: ");

        Optional<UserDto> userOpt = userService.findUserById(id);
        if (userOpt.isPresent()) {
            UserDto user = userOpt.get();
            System.out.println("Будет удален:");
            printUserCompact(user);

            String confirm = console.getStringInput("Подтвердите удаление (y/n): ");
            if (confirm.equalsIgnoreCase("y") || confirm.equalsIgnoreCase("yes")) {
                userService.deleteUser(id);
                console.printSuccess("Пользователь с ID " + id + " удален");
                logger.info("User deleted with ID: {}", id);
            } else {
                console.printInfo("Удаление отменено");
            }
        } else {
            console.printError("Пользователь с ID " + id + " не найден");
        }
    }

    private static void printUserDetails(UserDto user) {
        System.out.printf("ID:        %-20d \n", user.getId());
        System.out.printf("Имя:       %-20s \n", user.getName());
        System.out.printf("Email:     %-20s \n", user.getEmail());
        System.out.printf("Возраст:   %-20d \n", user.getAge());
    }

    private static void printUserCompact(UserDto user) {
        System.out.printf("  ID: %-3d | %-20s | %-25s | %3d %n",
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getAge());
    }
}