package mapper;

import dto.UserDto;
import model.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    @Test
    void toDto_ValidUser_ShouldMapCorrectly() {
        User user = new User("John Doe", "john@example.com", 25);
        user.setId(1L);

        UserDto dto = UserMapper.toDto(user);

        assertNotNull(dto);
        assertEquals(user.getId(), dto.getId());
        assertEquals(user.getName(), dto.getName());
        assertEquals(user.getEmail(), dto.getEmail());
        assertEquals(user.getAge(), dto.getAge());
    }

    @Test
    void toDto_NullUser_ShouldReturnNull() {
        assertNull(UserMapper.toDto(null));
    }

    @Test
    void toEntity_ValidDto_ShouldMapCorrectly() {
        UserDto dto = new UserDto();
        dto.setId(1L);
        dto.setName("John Doe");
        dto.setEmail("john@example.com");
        dto.setAge(25);

        User user = UserMapper.toEntity(dto);

        assertNotNull(user);
        assertEquals(dto.getId(), user.getId());
        assertEquals(dto.getName(), user.getName());
        assertEquals(dto.getEmail(), user.getEmail());
        assertEquals(dto.getAge(), user.getAge());
    }

    @Test
    void toEntity_NullDto_ShouldReturnNull() {
        assertNull(UserMapper.toEntity(null));
    }

    @Test
    void updateEntityFromDto_ValidData_ShouldUpdateExistingUser() {
        User existingUser = new User("Old Name", "old@email.com", 20);
        existingUser.setId(1L);

        UserDto updateDto = new UserDto();
        updateDto.setName("New Name");
        updateDto.setEmail("new@email.com");
        updateDto.setAge(25);

        UserMapper.updateEntityFromDto(updateDto, existingUser);

        assertEquals("New Name", existingUser.getName());
        assertEquals("new@email.com", existingUser.getEmail());
        assertEquals(25, existingUser.getAge());
        assertEquals(1L, existingUser.getId());
    }

    @Test
    void updateEntityFromDto_NullParameters_ShouldNotThrowException() {
        User user = new User("John", "john@email.com", 25);

        assertDoesNotThrow(() -> UserMapper.updateEntityFromDto(null, user));
        assertDoesNotThrow(() -> UserMapper.updateEntityFromDto(new UserDto(), null));

        assertEquals("John", user.getName());
    }

    @Test
    void updateEntityFromDto_WithNullFields_ShouldSetThemToNull() {
        User existingUser = new User("Old Name", "old@email.com", 20);
        existingUser.setId(1L);

        UserDto updateDto = new UserDto();
        updateDto.setName("New Name");

        UserMapper.updateEntityFromDto(updateDto, existingUser);

        assertEquals("New Name", existingUser.getName());
        assertNull(existingUser.getEmail());
        assertNull(existingUser.getAge());
    }
}