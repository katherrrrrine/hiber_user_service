package service;

import dao.UserDao;
import dto.UserDto;
import exception.UserNotFoundException;
import exception.UserValidationException;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserDao userDao;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userDao);
    }

    @Test
    void createUser_ValidUser_ShouldReturnCreatedUser() {
        UserDto inputDto = new UserDto();
        inputDto.setName("John Doe");
        inputDto.setEmail("john@example.com");
        inputDto.setAge(25);

        User userEntity = new User("John Doe", "john@example.com", 25);
        userEntity.setId(1L);

        when(userDao.save(any(User.class))).thenReturn(userEntity);

        UserDto result = userService.createUser(inputDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
        assertEquals("john@example.com", result.getEmail());
        assertEquals(25, result.getAge());

        verify(userDao, times(1)).save(any(User.class));
    }

    @Test
    void createUser_DaoThrowsException_ShouldPropagateException() {
        UserDto inputDto = new UserDto();
        inputDto.setName("John Doe");
        inputDto.setEmail("john@example.com");
        inputDto.setAge(25);

        when(userDao.save(any(User.class))).thenThrow(new UserValidationException("Email already exists"));

        assertThrows(UserValidationException.class, () -> userService.createUser(inputDto));
        verify(userDao, times(1)).save(any(User.class));
    }

    @Test
    void findUserById_ExistingId_ShouldReturnUser() {
        Long userId = 1L;
        User user = new User("John Doe", "john@example.com", 25);
        user.setId(userId);

        when(userDao.findById(userId)).thenReturn(Optional.of(user));

        Optional<UserDto> result = userService.findUserById(userId);

        assertTrue(result.isPresent());
        assertEquals(userId, result.get().getId());
        assertEquals("John Doe", result.get().getName());

        verify(userDao, times(1)).findById(userId);
    }

    @Test
    void findUserById_NonExistingId_ShouldReturnEmpty() {
        Long userId = 999L;
        when(userDao.findById(userId)).thenReturn(Optional.empty());

        Optional<UserDto> result = userService.findUserById(userId);

        assertTrue(result.isEmpty());
        verify(userDao, times(1)).findById(userId);
    }

    @Test
    void findAllUsers_ShouldReturnListOfUsers() {
        User user1 = new User("John", "john@email.com", 25);
        user1.setId(1L);
        User user2 = new User("Jane", "jane@email.com", 30);
        user2.setId(2L);
        User user3 = new User("Bob", "bob@email.com", 35);
        user3.setId(3L);

        when(userDao.findAll()).thenReturn(Arrays.asList(user1, user2, user3));

        List<UserDto> results = userService.findAllUsers();

        assertEquals(3, results.size());
        assertEquals("John", results.get(0).getName());
        assertEquals("Jane", results.get(1).getName());
        assertEquals("Bob", results.get(2).getName());

        verify(userDao, times(1)).findAll();
    }

    @Test
    void findAllUsers_EmptyDatabase_ShouldReturnEmptyList() {
        when(userDao.findAll()).thenReturn(List.of());

        List<UserDto> results = userService.findAllUsers();

        assertTrue(results.isEmpty());
        verify(userDao, times(1)).findAll();
    }

    @Test
    void updateUser_ExistingUser_ShouldReturnUpdatedUser() {
        Long userId = 1L;
        UserDto updateDto = new UserDto();
        updateDto.setId(userId);
        updateDto.setName("Updated Name");
        updateDto.setEmail("updated@email.com");
        updateDto.setAge(35);

        User existingUser = new User("Old Name", "old@email.com", 25);
        existingUser.setId(userId);

        User updatedUser = new User("Updated Name", "updated@email.com", 35);
        updatedUser.setId(userId);

        when(userDao.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userDao.update(any(User.class))).thenReturn(updatedUser);

        UserDto result = userService.updateUser(updateDto);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("Updated Name", result.getName());
        assertEquals("updated@email.com", result.getEmail());
        assertEquals(35, result.getAge());

        verify(userDao, times(1)).findById(userId);
        verify(userDao, times(1)).update(any(User.class));
    }

    @Test
    void updateUser_NonExistingUser_ShouldThrowException() {
        Long userId = 999L;
        UserDto updateDto = new UserDto();
        updateDto.setId(userId);
        updateDto.setName("Updated Name");

        when(userDao.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.updateUser(updateDto));

        verify(userDao, times(1)).findById(userId);
        verify(userDao, never()).update(any());
    }

    @Test
    void updateUser_PartialUpdate_ShouldUpdateOnlyProvidedFields() {
        Long userId = 1L;
        UserDto updateDto = new UserDto();
        updateDto.setId(userId);
        updateDto.setName("Updated Name");

        User existingUser = new User("Old Name", "old@email.com", 25);
        existingUser.setId(userId);

        User updatedUser = new User("Updated Name", "old@email.com", 25);
        updatedUser.setId(userId);

        when(userDao.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userDao.update(any(User.class))).thenReturn(updatedUser);

        UserDto result = userService.updateUser(updateDto);

        assertEquals("Updated Name", result.getName());
        assertEquals("old@email.com", result.getEmail());
        assertEquals(25, result.getAge());

        verify(userDao, times(1)).update(any(User.class));
    }

    @Test
    void deleteUser_ExistingUser_ShouldCallDaoDelete() {
        Long userId = 1L;
        doNothing().when(userDao).delete(userId);

        userService.deleteUser(userId);

        verify(userDao, times(1)).delete(userId);
    }

    @Test
    void deleteUser_NonExistingUser_ShouldPropagateException() {
        Long userId = 999L;
        doThrow(new UserNotFoundException("User not found")).when(userDao).delete(userId);

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(userId));
        verify(userDao, times(1)).delete(userId);
    }
}