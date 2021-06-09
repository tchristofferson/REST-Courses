package com.tchristofferson.courses;

import com.google.gson.Gson;
import com.tchristofferson.courses.dao.CourseDao;
import com.tchristofferson.courses.dao.ReviewDao;
import com.tchristofferson.courses.dao.Sql2oCourseDao;
import com.tchristofferson.courses.dao.Sql2oReviewDao;
import com.tchristofferson.courses.exc.ApiError;
import com.tchristofferson.courses.exc.DaoException;
import com.tchristofferson.courses.models.Course;
import com.tchristofferson.courses.models.Review;
import org.sql2o.Sql2o;

import java.util.HashMap;
import java.util.Map;

import static spark.Spark.*;

public class Api {

    public static void main(String[] args) {
        String datasource = "jdbc:h2:~/reviews.db";

        if (args.length > 0) {
            if (args.length != 2) {
                System.out.println("java Api <port> <datasource>");
                System.exit(0);
            }

            port(Integer.parseInt(args[0]));
            datasource = args[1];
        }

        Sql2o sql2o = new Sql2o(
                String.format("%s;INIT=RUNSCRIPT from 'classpath:db/init.sql'", datasource),
                "", ""
        );
        CourseDao courseDao = new Sql2oCourseDao(sql2o);
        ReviewDao reviewDao = new Sql2oReviewDao(sql2o);
        Gson gson = new Gson();

        post("/courses", "application/json", (request, response) -> {
            Course course = gson.fromJson(request.body(), Course.class);
            courseDao.add(course);
            response.status(201);
            return course;
        }, gson::toJson);

        get("/courses", "application/json", (request, response) -> courseDao.findAll(), gson::toJson);

        get("/courses/:id", "application/json", (request, response) -> {
            int id = Integer.parseInt(request.params("id"));
            Course course = courseDao.findById(id);

            if (course == null)
                throw new ApiError(404, "Could not find course with id " + id);

            return course;
        }, gson::toJson);

        post("/courses/:courseId/reviews", "application/json", (request, response) -> {
            int courseId = Integer.parseInt(request.params("courseId"));
            Review review = gson.fromJson(request.body(), Review.class);
            review.setCourseId(courseId);

            try {
                reviewDao.add(review);
            } catch (DaoException e) {
                throw new ApiError(500, e.getMessage());
            }

            response.status(201);
            return review;
        }, gson::toJson);

        get("/courses/:courseId/reviews", "application/json", (request, response) -> {
            int courseId = Integer.parseInt(request.params("courseId"));
            return reviewDao.findByCourseId(courseId);
        }, gson::toJson);

        exception(ApiError.class, (exception, request, response) -> {
            Map<String, Object> jsonMap = new HashMap<>();
            jsonMap.put("status", exception.getStatus());
            jsonMap.put("errorMessage", exception.getMessage());
            response.type("application/json");
            response.status(exception.getStatus());
            response.body(gson.toJson(jsonMap));
        });

        after((request, response) -> response.type("application/json"));
    }

}
