package com.tchristofferson.courses.dao;

import com.tchristofferson.courses.exc.DaoException;
import com.tchristofferson.courses.models.Course;
import com.tchristofferson.courses.models.Review;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class Sql2oReviewDaoTest {

    private Connection connection;
    private Sql2oCourseDao courseDao;
    private Sql2oReviewDao reviewDao;
    private Course course;

    @BeforeEach
    public void setUp() throws DaoException {
        String connectionString = "jdbc:h2:mem:testing;INIT=RUNSCRIPT from 'classpath:db/init.sql'";
        Sql2o sql2o = new Sql2o(connectionString, "", "");
        connection = sql2o.open();
        courseDao = new Sql2oCourseDao(sql2o);
        reviewDao = new Sql2oReviewDao(sql2o);
        course = new Course("Test", "http://test.com");
        courseDao.add(course);
    }

    @AfterEach
    public void tearDown() {
        connection.close();
    }

    @Test
    public void addingReviewSetsNewId() throws DaoException {
        Review review = new Review(course.getId(), 5, "Test comment");
        int originalId = review.getId();
        reviewDao.add(review);

        assertNotEquals(originalId, review.getId());
    }

    @Test
    public void multipleReviewsAreFoundWhenTheyExistForACourse() throws DaoException {
        reviewDao.add(new Review(course.getId(), 5, "Test comment 1"));
        reviewDao.add(new Review(course.getId(), 1, "Test comment 2"));
        List<Review> reviews = reviewDao.findByCourseId(course.getId());

        assertEquals(2, reviews.size());
    }

    @Test
    public void addingAReviewToANonExistingCourseFails() {
        Review review = new Review(42, 5, "Test comment");
        assertThrows(DaoException.class, () -> reviewDao.add(review));
    }
}