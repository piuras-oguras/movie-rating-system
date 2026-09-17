package com.project.movieratingsystem.repository;

import com.project.movieratingsystem.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    @Query("SELECT r FROM Rating r JOIN FETCH r.movie")
    List<Rating> findAllWithMovie();

    @Query("SELECT r FROM Rating r JOIN FETCH r.movie WHERE r.movie.id = :movieId")
    List<Rating> findByMovieId(@Param("movieId") Long movieId);

    @Query("SELECT r FROM Rating r JOIN FETCH r.movie WHERE r.userName = :userName")
    List<Rating> findByUserName(@Param("userName") String userName);

    Optional<Rating> findFirstByMovieIdAndUserName(Long movieId, String userName);

    boolean existsByMovieIdAndUserName(Long movieId, String userName);
}
