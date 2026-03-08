package util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HibernateUtil {

    private static final Logger logger = LogManager.getLogger(HibernateUtil.class);
    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        try {
            logger.info("Creating SessionFactory");
            return new Configuration().configure().buildSessionFactory();
        } catch (Throwable ex) {
            logger.error("Initial SessionFactory creation failed: {}", ex.getMessage());
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        logger.info("Shutting down SessionFactory");
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }
}