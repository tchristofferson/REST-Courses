package com.tchristofferson.courses.dao;

import com.tchristofferson.courses.exc.DaoException;
import com.tchristofferson.courses.models.Course;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import static org.junit.jupiter.api.Assertions.*;

class Sql2oCourseDaoTest {

    private Sql2oCourseDao dao;
    private Connection connection;

    @BeforeEach
    void setUp() {
        String connectionString = "jdbc:h2:mem:testing;INIT=RUNSCRIPT from 'classpath:db/init.sql'";
        Sql2o sql2o = new Sql2o(connectionString, "", "");
        dao = new Sql2oCourseDao(sql2o);
        connection = sql2o.open();
    }

    @AfterEach
    void tearDown() {
        connection.close();
    }

    @Test
    public void addingCourseSetsId() throws DaoException {
        Course course = newTestCourse();
        int originalCourseId = course.getId();
        dao.add(course);

        assertNotEquals(originalCourseId, course.getId());
    }

    @Test
    public void addedCoursesAreReturnedFromFindAll() throws DaoException {
        Course course = newTestCourse();
        dao.add(course);

        assertEquals(1, dao.findAll().size());
    }

    @Test
    public void noCoursesReturnsEmptyList() {
        assertEquals(0, dao.findAll().size());
    }

    @Test
    public void existingCoursesCanBeFoundById() throws DaoException {
        Course course = newTestCourse();
        dao.add(course);
        Course foundCourse = dao.findById(course.getId());

        assertEquals(course, foundCourse);
    }

    private Course newTestCourse() {
        return new Course("Test", "http://test.com");
    }
}