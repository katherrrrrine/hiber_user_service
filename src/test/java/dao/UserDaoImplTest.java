package dao;

import exception.UserNotFoundException;
import exception.UserValidationException;
import model.User;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import util.HibernateUtil;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserDaoImplTest {

    @Container
    private static final PostgreSQLContainer<?> postgresContainer =
            new PostgreSQLContainer<>("postgres:15")
                    .withDatabaseName("test_user_service_db")
                    .withUsername("test")
                    .withPassword("test");

    private UserDaoImpl userDao;

    @BeforeEach
    void setUp() {
        System.setProperty("hibernate.connection.url", postgresContainer.getJdbcUrl());
        System.setProperty("hibernate.connection.username", postgresContainer.getUsername());
        System.setProperty("hibernate.connection.password", postgresContainer.getPassword());
        System.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
        System.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        System.setProperty("hibernate.hbm2ddl.auto", "create-drop");
        System.setProperty("hibernate.show_sql", "true");
        System.setProperty("hibernate.format_sql", "true");

        userDao = new UserDaoImpl();
        cleanDatabase();
    }

    private void cleanDatabase() {
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            var transaction = session.beginTransaction();
            session.createQuery("DELETE FROM User").executeUpdate();
            transaction.commit();
        } catch (Exception e) {
            System.out.println("Clean database warning: " + e.getMessage());
        }
    }

    @Test
    void save_ValidUser_ShouldPersistUser() {
        User user = new User("John Doe", "john@example.com", 25);

        User savedUser = userDao.save(user);

        assertNotNull(savedUser.getId());
        assertEquals("John Doe", savedUser.getName());
        assertEquals("john@example.com", savedUser.getEmail());
        assertEquals(25, savedUser.getAge());
        assertNotNull(savedUser.getCreatedAt());
    }

    @Test
    void save_NullUser_ShouldThrowException() {
        assertThrows(UserValidationException.class, () -> userDao.save(null));
    }

    @Test
    void save_UserWithNullName_ShouldThrowException() {
        User user = new User(null, "john@example.com", 25);

        assertThrows(Exception.class, () -> userDao.save(user));
    }

    @Test
    void findById_ExistingId_ShouldReturnUser() {
        User user = new User("John Doe", "john@example.com", 25);
        User savedUser = userDao.save(user);

        Optional<User> found = userDao.findById(savedUser.getId());

        assertTrue(found.isPresent());
        assertEquals(savedUser.getId(), found.get().getId());
        assertEquals(savedUser.getName(), found.get().getName());
        assertEquals(savedUser.getEmail(), found.get().getEmail());
        assertEquals(savedUser.getAge(), found.get().getAge());
    }

    @Test
    void findById_NonExistingId_ShouldReturnEmpty() {
        Optional<User> found = userDao.findById(999L);
        assertTrue(found.isEmpty());
    }

    @Test
    void findById_InvalidId_ShouldThrowException() {
        assertThrows(UserValidationException.class, () -> userDao.findById(null));
        assertThrows(UserValidationException.class, () -> userDao.findById(-1L));
        assertThrows(UserValidationException.class, () -> userDao.findById(0L));
    }

    @Test
    void findAll_ShouldReturnAllUsers() {
        userDao.save(new User("John", "john@email.com", 25));
        userDao.save(new User("Jane", "jane@email.com", 30));
        userDao.save(new User("Bob", "bob@email.com", 35));

        List<User> users = userDao.findAll();

        assertEquals(3, users.size());
        assertTrue(users.stream().anyMatch(u -> u.getEmail().equals("john@email.com")));
        assertTrue(users.stream().anyMatch(u -> u.getEmail().equals("jane@email.com")));
        assertTrue(users.stream().anyMatch(u -> u.getEmail().equals("bob@email.com")));
    }

    @Test
    void findAll_EmptyDatabase_ShouldReturnEmptyList() {
        List<User> users = userDao.findAll();
        assertTrue(users.isEmpty());
    }

    @Test
    void update_ExistingUser_ShouldUpdateFields() {
        User user = new User("John", "john@email.com", 25);
        User savedUser = userDao.save(user);

        savedUser.setName("John Updated");
        savedUser.setEmail("john.updated@email.com");
        savedUser.setAge(30);

        User updatedUser = userDao.update(savedUser);

        assertEquals("John Updated", updatedUser.getName());
        assertEquals("john.updated@email.com", updatedUser.getEmail());
        assertEquals(30, updatedUser.getAge());

        Optional<User> found = userDao.findById(savedUser.getId());
        assertTrue(found.isPresent());
        assertEquals("John Updated", found.get().getName());
        assertEquals("john.updated@email.com", found.get().getEmail());
        assertEquals(30, found.get().getAge());
    }

    @Test
    void update_NonExistingUser_ShouldThrowException() {
        User nonExistingUser = new User("Ghost", "ghost@email.com", 99);
        nonExistingUser.setId(999L);

        assertThrows(UserNotFoundException.class,
                () -> userDao.update(nonExistingUser));
    }

    @Test
    void update_NullUser_ShouldThrowException() {
        assertThrows(UserValidationException.class, () -> userDao.update(null));
    }

    @Test
    void update_UserWithNullId_ShouldThrowException() {
        User user = new User("John", "john@email.com", 25);

        assertThrows(UserValidationException.class, () -> userDao.update(user));
    }

    @Test
    void delete_ExistingUser_ShouldRemoveUser() {
        User user = new User("John", "john@email.com", 25);
        User savedUser = userDao.save(user);

        userDao.delete(savedUser.getId());

        Optional<User> found = userDao.findById(savedUser.getId());
        assertTrue(found.isEmpty());
    }

    @Test
    void delete_NonExistingUser_ShouldThrowException() {
        assertThrows(UserNotFoundException.class, () -> userDao.delete(999L));
    }

    @Test
    void delete_InvalidId_ShouldThrowException() {
        assertThrows(UserValidationException.class, () -> userDao.delete(null));
        assertThrows(UserValidationException.class, () -> userDao.delete(-1L));
        assertThrows(UserValidationException.class, () -> userDao.delete(0L));
    }

    @Test
    void uniqueEmailConstraint_ShouldNotAllowDuplicateEmails() {
        User user1 = new User("John", "same@email.com", 25);
        userDao.save(user1);

        User user2 = new User("Jane", "same@email.com", 30);

        Exception exception = assertThrows(Exception.class, () -> userDao.save(user2));
        assertTrue(exception.getCause() instanceof ConstraintViolationException);
    }

    @Test
    void saveAndRetrieve_MultipleUsers_ShouldMaintainDataIntegrity() {
        User user1 = new User("John", "john1@test.com", 25);
        User user2 = new User("Jane", "jane2@test.com", 30);
        User user3 = new User("Bob", "bob3@test.com", 35);

        User saved1 = userDao.save(user1);
        User saved2 = userDao.save(user2);
        User saved3 = userDao.save(user3);

        assertNotEquals(saved1.getId(), saved2.getId());
        assertNotEquals(saved2.getId(), saved3.getId());

        List<User> allUsers = userDao.findAll();
        assertEquals(3, allUsers.size());
    }

    @Test
    void update_ShouldNotChangeCreatedAt() {
        User user = new User("John", "john@email.com", 25);
        User savedUser = userDao.save(user);

        var createdAt = savedUser.getCreatedAt();

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
        }

        savedUser.setName("John Updated");
        savedUser.setAge(30);
        User updatedUser = userDao.update(savedUser);

        assertTrue(Math.abs(createdAt.getNano() - updatedUser.getCreatedAt().getNano()) < 1000000,
                "CreatedAt should not change more than 1 millisecond");
    }
}