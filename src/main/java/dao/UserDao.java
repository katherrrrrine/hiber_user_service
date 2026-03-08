package dao;

import dto.UserDto;
import java.util.List;
import java.util.Optional;

public interface UserDao {
    UserDto save(UserDto userDto);
    Optional<UserDto> findById(Long id);
    List<UserDto> findAll();
    UserDto update(UserDto userDto);
    void delete(Long id);
}