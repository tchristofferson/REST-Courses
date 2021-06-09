package com.tchristofferson.courses;

import com.google.gson.Gson;
import com.tchristofferson.courses.dao.Sql2oCourseDao;
import com.tchristofferson.courses.dao.Sql2oReviewDao;
import com.tchristofferson.courses.exc.DaoException;
import com.tchristofferson.courses.models.Course;
import com.tchristofferson.courses.models.Review;
import com.tchristofferson.testing.ApiClient;
import com.tchristofferson.testing.ApiResponse;
import org.junit.jupiter.api.*;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import spark.Spark;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ApiTest {

    public static final String PORT = "4568";
    public static final String TEST_DATASOURCE = "jdbc:h2:mem:testing";
    private Connection connection;
    private ApiClient client;
    private Gson gson;
    private Sql2oCourseDao courseDao;
    private Sql2oReviewDao reviewDao;

    @BeforeAll
    public static void startServer() {
        String[] args = {PORT, TEST_DATASOURCE};
        Api.main(args);
    }

    @AfterAll
    public static void stopServer() {
        Spark.stop();
    }

    @BeforeEach
    public void setUp() {
        Sql2o sql2o = new Sql2o(TEST_DATASOURCE + ";INIT=RUNSCRIPT from 'classpath:db/init.sql'", "", "");
        courseDao = new Sql2oCourseDao(sql2o);
        reviewDao = new Sql2oReviewDao(sql2o);
        connection = sql2o.open();
        client = new ApiClient("http://localhost:" + PORT);
        gson = new Gson();
    }

    @AfterEach
    public void tearDown() {
        connection.close();
    }

    @Test
    public void addingCoursesReturnsCreatedStatus() {
        Map<String, String> values = new HashMap<>();
        Course course = newTestCourse();
        values.put("name", course.getName());
        values.put("url", course.getUrl());
        ApiResponse response = client.request("POST", "/courses", gson.toJson(values));

        assertEquals(201, response.getStatus());
    }

    @Test
    public void coursesCanBeAccessedById() throws DaoException {
        Course course = newTestCourse();
        courseDao.add(course);
        ApiResponse response = client.request("GET", "/courses/" + course.getId());
        Course retrieved = gson.fromJson(response.getBody(), Course.class);

        assertEquals(course, retrieved);
    }

    @Test
    public void missingCoursesReturnNotFoundStatus() {
        ApiResponse response = client.request("GET", "/courses/42");
        assertEquals(404, response.getStatus());
    }

    @Test
    public void addingReviewGivesCreatedStatus() throws DaoException {
        Course course = newTestCourse();
        courseDao.add(course);
        Map<String, Object> values = new HashMap<>();
        values.put("rating", 5);
        values.put("comment", "Test comment");
        ApiResponse response = client.request("POST",
                String.format("/courses/%d/reviews", course.getId()),
                gson.toJson(values));

        assertEquals(201, response.getStatus());
    }

    @Test
    public void addingReviewToUnknownCourseThrowsError() {
        Map<String, Object> values = new HashMap<>();
        values.put("rating", 5);
        values.put("comment", "Test comment");
        ApiResponse response = client.request("POST", "/courses/42/reviews", gson.toJson(values));

        assertEquals(500, response.getStatus());
    }

    @Test
    public void multipleReviewsReturnedForCourse() throws DaoException {
        Course course = newTestCourse();
        courseDao.add(course);
        reviewDao.add(new Review(course.getId(), 5, "Test comment 1"));
        reviewDao.add(new Review(course.getId(), 4, "Test comment 2"));
        ApiResponse response = client.request("GET", String.format("/courses/%d/reviews", course.getId()));
        Review[] reviews = gson.fromJson(response.getBody(), Review[].class);

        assertEquals(2, reviews.length);
    }

    private Course newTestCourse() {
        return new Course("Test", "http://test.com");
    }
}