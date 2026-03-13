package service;

import dao.UserDao;
import dto.UserDto;
import exception.UserNotFoundException;
import exception.UserValidationException;
import mapper.UserMapper;
import model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class UserService {

    private static final Logger logger = LogManager.getLogger(UserService.class);
    private final UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public UserDto createUser(UserDto userDto) {
        User user = UserMapper.toEntity(userDto);
        User savedUser = userDao.save(user);
        return UserMapper.toDto(savedUser);
    }

    public Optional<UserDto> findUserById(Long id) {
        Optional<User> userOpt = userDao.findById(id);
        return userOpt.map(UserMapper::toDto);
    }

    public List<UserDto> findAllUsers() {
        return userDao.findAll().stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    public UserDto updateUser(UserDto userDto) {
        Optional<User> existingUserOpt = userDao.findById(userDto.getId());
        if (existingUserOpt.isEmpty()) {
            throw new UserNotFoundException("User not found with ID: " + userDto.getId());
        }

        User existingUser = existingUserOpt.get();
        UserMapper.updateEntityFromDto(userDto, existingUser);
        User updatedUser = userDao.update(existingUser);
        return UserMapper.toDto(updatedUser);
    }

    public void deleteUser(Long id) {
        userDao.delete(id);
    }
}