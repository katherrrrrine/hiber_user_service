package dao;

import dto.UserDto;
import exception.UserDaoException;
import exception.UserNotFoundException;
import exception.UserValidationException;
import mapper.UserMapper;
import model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import util.HibernateUtil;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class UserDaoImpl implements UserDao {

    private static final Logger logger = LogManager.getLogger(UserDaoImpl.class);

    @Override
    public UserDto save(UserDto userDto) {
        if (userDto == null) {
            throw new UserValidationException("User data cannot be null");
        }

        Transaction transaction = null;
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();

            User user = UserMapper.toEntity(userDto);
            session.save(user);
            transaction.commit();

            logger.info("User saved successfully with ID: {}", user.getId());
            return UserMapper.toDto(user);

        } catch (Exception e) {
            if (transaction != null) {
                try {
                    transaction.rollback();
                } catch (Exception rollbackEx) {
                    logger.error("Error during rollback: {}", rollbackEx.getMessage());
                }
            }
            logger.error("Error saving user: {}", e.getMessage());
            throw new UserDaoException("Failed to save user", e);
        } finally {
            if (session != null) {
                try {
                    session.close();
                } catch (Exception closeEx) {
                    logger.error("Error closing session: {}", closeEx.getMessage());
                }
            }
        }
    }

    @Override
    public Optional<UserDto> findById(Long id) {
        if (id == null || id <= 0) {
            throw new UserValidationException("Invalid user ID");
        }

        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            User user = session.get(User.class, id);

            if (user != null) {
                logger.info("User found with ID: {}", id);
                return Optional.of(UserMapper.toDto(user));
            } else {
                logger.info("User with ID {} not found", id);
                return Optional.empty();
            }

        } catch (Exception e) {
            logger.error("Error finding user by ID {}: {}", id, e.getMessage());
            throw new UserDaoException("Failed to find user by ID: " + id, e);
        } finally {
            if (session != null) {
                try {
                    session.close();
                } catch (Exception closeEx) {
                    logger.error("Error closing session: {}", closeEx.getMessage());
                }
            }
        }
    }

    @Override
    public List<UserDto> findAll() {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query<User> query = session.createQuery("FROM User ORDER BY id", User.class);
            List<User> users = query.list();

            logger.info("Found {} users", users.size());

            return users.stream()
                    .map(UserMapper::toDto)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("Error finding all users: {}", e.getMessage());
            throw new UserDaoException("Failed to retrieve users", e);
        } finally {
            if (session != null) {
                try {
                    session.close();
                } catch (Exception closeEx) {
                    logger.error("Error closing session: {}", closeEx.getMessage());
                }
            }
        }
    }

    @Override
    public UserDto update(UserDto userDto) {
        if (userDto == null || userDto.getId() == null) {
            throw new UserValidationException("User data with ID is required for update");
        }

        Transaction transaction = null;
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();

            User existingUser = session.get(User.class, userDto.getId());
            if (existingUser == null) {
                throw new UserNotFoundException("User not found with ID: " + userDto.getId());
            }

            UserMapper.updateEntityFromDto(userDto, existingUser);
            session.update(existingUser);
            transaction.commit();

            logger.info("User updated successfully with ID: {}", existingUser.getId());
            return UserMapper.toDto(existingUser);

        } catch (UserNotFoundException e) {
            if (transaction != null) {
                try {
                    transaction.rollback();
                } catch (Exception rollbackEx) {
                    logger.error("Error during rollback: {}", rollbackEx.getMessage());
                }
            }
            throw e;
        } catch (Exception e) {
            if (transaction != null) {
                try {
                    transaction.rollback();
                } catch (Exception rollbackEx) {
                    logger.error("Error during rollback: {}", rollbackEx.getMessage());
                }
            }
            logger.error("Error updating user: {}", e.getMessage());
            throw new UserDaoException("Failed to update user with ID: " + userDto.getId(), e);
        } finally {
            if (session != null) {
                try {
                    session.close();
                } catch (Exception closeEx) {
                    logger.error("Error closing session: {}", closeEx.getMessage());
                }
            }
        }
    }

    @Override
    public void delete(Long id) {
        if (id == null || id <= 0) {
            throw new UserValidationException("Invalid user ID for deletion");
        }

        Transaction transaction = null;
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();

            User user = session.get(User.class, id);
            if (user == null) {
                throw new UserNotFoundException("User not found with ID: " + id);
            }

            session.delete(user);
            transaction.commit();

            logger.info("User deleted with ID: {}", id);

        } catch (UserNotFoundException e) {
            if (transaction != null) {
                try {
                    transaction.rollback();
                } catch (Exception rollbackEx) {
                    logger.error("Error during rollback: {}", rollbackEx.getMessage());
                }
            }
            throw e;
        } catch (Exception e) {
            if (transaction != null) {
                try {
                    transaction.rollback();
                } catch (Exception rollbackEx) {
                    logger.error("Error during rollback: {}", rollbackEx.getMessage());
                }
            }
            logger.error("Error deleting user with ID {}: {}", id, e.getMessage());
            throw new UserDaoException("Failed to delete user with ID: " + id, e);
        } finally {
            if (session != null) {
                try {
                    session.close();
                } catch (Exception closeEx) {
                    logger.error("Error closing session: {}", closeEx.getMessage());
                }
            }
        }
    }
}