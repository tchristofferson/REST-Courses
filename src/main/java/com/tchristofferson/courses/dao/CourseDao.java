package com.tchristofferson.courses.dao;

import com.tchristofferson.courses.exc.DaoException;
import com.tchristofferson.courses.models.Course;

import java.util.List;

public interface CourseDao {

    void add(Course course) throws DaoException;

    List<Course> findAll();

    Course findById(int id);
}
