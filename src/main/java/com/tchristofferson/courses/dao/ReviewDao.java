package com.tchristofferson.courses.dao;

import com.tchristofferson.courses.exc.DaoException;
import com.tchristofferson.courses.models.Review;

import java.util.List;

public interface ReviewDao {

    void add(Review review) throws DaoException;

    List<Review> findAll();

    List<Review> findByCourseId(int courseId);

}
