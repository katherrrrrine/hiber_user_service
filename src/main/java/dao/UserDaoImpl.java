package dao;

import model.User;
import exception.UserDaoException;
import exception.UserNotFoundException;
import exception.UserValidationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import util.HibernateUtil;

import java.util.List;
import java.util.Optional;

public class UserDaoImpl implements UserDao {

    private static final Logger logger = LogManager.getLogger(UserDaoImpl.class);

    @Override
    public User save(User user) {
        if (user == null) {
            throw new UserValidationException("User entity cannot be null");
        }

        Transaction transaction = null;
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();

            session.save(user);
            transaction.commit();

            logger.info("User saved successfully with ID: {}", user.getId());
            return user;

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
    public Optional<User> findById(Long id) {
        if (id == null || id <= 0) {
            throw new UserValidationException("Invalid user ID");
        }

        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            User user = session.get(User.class, id);

            if (user != null) {
                logger.info("User found with ID: {}", id);
                return Optional.of(user);
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
    public List<User> findAll() {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query<User> query = session.createQuery("FROM User ORDER BY id", User.class);
            List<User> users = query.list();

            logger.info("Found {} users", users.size());
            return users;

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
    public User update(User user) {
        if (user == null || user.getId() == null) {
            throw new UserValidationException("User entity with ID is required for update");
        }

        Transaction transaction = null;
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();

            User existingUser = session.get(User.class, user.getId());
            if (existingUser == null) {
                throw new UserNotFoundException("User not found with ID: " + user.getId());
            }

            // Обновляем поля существующей сущности
            existingUser.setName(user.getName());
            existingUser.setEmail(user.getEmail());
            existingUser.setAge(user.getAge());

            session.update(existingUser);
            transaction.commit();

            logger.info("User updated successfully with ID: {}", existingUser.getId());
            return existingUser;

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
            throw new UserDaoException("Failed to update user with ID: " + user.getId(), e);
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