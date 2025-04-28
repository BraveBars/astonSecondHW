package org.example.dao;

import org.example.model.User;
import org.example.util.HibernateFactoryUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers     // включаем поддержку Testcontainers в JUnit Jupiter
class UserDaoTest {

    @Container     // определяем статический контейнер PostgreSQL, который будет автоматически поднят перед тестами
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    private static UserDAO userDAO;

    @BeforeAll
    static void setup() {
        // проксируем Hibernate-настройки на параметры нашего контейнера
        System.setProperty("hibernate.connection.url", postgres.getJdbcUrl());
        System.setProperty("hibernate.connection.username", postgres.getUsername());
        System.setProperty("hibernate.connection.password", postgres.getPassword());
        System.setProperty("hibernate.hbm2ddl.auto", "update");

        // инициализируем DAO — в нём при первом вызове откроется сессия Hibernate
        userDAO = new UserDAOImpl();
    }

    @AfterAll
    static void tearDown() {
        // закрываем SessionFactory, чтобы контейнеру не мешали висящие соединения
        HibernateFactoryUtil.shutdown();
    }

    //метод для быстрого создания тестового пользователя с одинаковыми полями,
    // что бы в каждом тесте не создавать отдельно
    private User createTestUser(String name) {
        User user = new User();
        user.setName(name);
        user.setEmail(name.toLowerCase() + "@test.com");
        user.setAge(25);
        return user;
    }

    @Test
    void testCreateAndGetById() {
        User user = createTestUser("John");

        //сохраняем в БД через DAO
        userDAO.create(user);
        //достаём его обратно по сгенерированному ID
        User found = userDAO.getById(user.getId());

        //проверяем, что пользователь сохранился (не null) и имя/дата создания корректны
        assertNotNull(found);
        assertEquals("John", found.getName());
        assertNotNull(found.getCreatedAt());
    }

    @Test
    void testGetAll() {
        User user1 = createTestUser("Alice");
        User user2 = createTestUser("Bob");

        userDAO.create(user1);
        userDAO.create(user2);

        List<User> users = userDAO.getAll();
        assertTrue(users.size() >= 2);
    }

    @Test
    void testUpdate() {
        User user = createTestUser("BeforeUpdate");
        userDAO.create(user);

        user.setName("AfterUpdate");
        userDAO.update(user);

        User updated = userDAO.getById(user.getId());
        assertEquals("AfterUpdate", updated.getName());
    }

    @Test
    void testDelete() {
        User user = createTestUser("ToDelete");
        userDAO.create(user);

        userDAO.delete(user.getId());

        assertNull(userDAO.getById(user.getId()));
    }
}