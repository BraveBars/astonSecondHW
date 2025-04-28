package org.example.util;

import org.example.model.User;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.service.ServiceRegistry;

import java.util.Properties;

public class HibernateFactoryUtil {
    //убрал final и статическую инициализацию, теперь фабрику можно пересоздавать с новыми настройками
    private static SessionFactory sessionFactory;


    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            try {
                Configuration configuration = new Configuration();

                // конфигурация через код вместо properties-файла(проперти можно удалить)
                Properties settings = new Properties();

                // используем System.getProperty с fallback-значениямиб т.к. это
                // позволяет переопределять настройки из тестов
                settings.put(Environment.DRIVER,
                        System.getProperty("hibernate.connection.driver_class", "org.postgresql.Driver"));
                settings.put(Environment.URL,
                        System.getProperty("hibernate.connection.url", "jdbc:postgresql://localhost:5432/postgres"));
                settings.put(Environment.USER,
                        System.getProperty("hibernate.connection.username", "postgres"));
                settings.put(Environment.PASS,
                        System.getProperty("hibernate.connection.password", "1"));

                settings.put(Environment.DIALECT, "org.hibernate.dialect.PostgreSQLDialect");
                settings.put(Environment.HBM2DDL_AUTO, "update");
                settings.put(Environment.SHOW_SQL, "true");

                configuration.setProperties(settings);
                configuration.addAnnotatedClass(User.class);

                ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                        .applySettings(configuration.getProperties())
                        .build();

                sessionFactory = configuration.buildSessionFactory(serviceRegistry);
            } catch (Throwable ex) {
                throw new ExceptionInInitializerError(
                        "Initial SessionFactory creation failed: " + ex);
            }
        }
        return sessionFactory;
    }


    //добавлена очистка sessionFactory после закрытия
    public static void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
            sessionFactory = null;
        }
    }
}