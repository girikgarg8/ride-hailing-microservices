package com.girikgarg.uberreviewservice.repositories;

import com.girikgarg.uberentityservice.models.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Integer countAllByRatingIsLessThanEqual(Integer givenRating);

    List <Review> findAllByRatingIsLessThanEqual(Integer givenRating);

    List <Review> findAllByCreatedAtBefore(Date date);
}
