package com.project.movieratingsystem.services;

import com.project.movieratingsystem.model.Movie;
import com.project.movieratingsystem.model.Rating;
import com.project.movieratingsystem.model.RatingStar;
import com.project.movieratingsystem.repository.RatingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RatingService {

    private final RatingRepository ratingRepository;
    private final MovieService movieService;

    public RatingService(RatingRepository ratingRepository, MovieService movieService) {
        this.ratingRepository = ratingRepository;
        this.movieService = movieService;
    }

    @Transactional(readOnly = true)
    public List<Rating> findAllRatings() {
        return ratingRepository.findAllWithMovie();
    }


    public Rating addRating(Long movieId, RatingStar score, String comment, String userName) {
        Movie movie = movieService.findById(movieId)
                .orElseThrow(() -> new IllegalArgumentException("Film nie istnieje"));

        Rating rating = ratingRepository
                .findFirstByMovieIdAndUserName(movieId, userName)
                .map(existing -> {
                    existing.setScore(score);
                    existing.setComment(comment);
                    return existing;
                })
                .orElseGet(() -> new Rating(score, comment, movie, userName));

        Rating savedRating = ratingRepository.save(rating);

        updateMovieRating(movieId);

        return savedRating;
    }

    public Rating updateRating(Long ratingId, Integer score, String comment, String userName) {
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new IllegalArgumentException("Ocena nie istnieje"));

        if (!rating.getUserName().equals(userName)) {
            throw new IllegalStateException("Możesz edytować tylko swoje oceny");
        }

        RatingStar ratingStar = RatingStar.values()[score - 1];
        rating.setScore(ratingStar);
        rating.setComment(comment);

        Rating savedRating = ratingRepository.save(rating);

        updateMovieRating(rating.getMovie().getId());

        return savedRating;
    }

    public void deleteRating(Long ratingId, String userName) {
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new IllegalArgumentException("Ocena nie istnieje"));

        if (!rating.getUserName().equals(userName)) {
            throw new IllegalStateException("Możesz usuwać tylko swoje oceny");
        }

        Long movieId = rating.getMovie().getId();
        ratingRepository.delete(rating);

        updateMovieRating(movieId);
    }

    @Transactional(readOnly = true)
    public List<Rating> findByMovie(Long movieId) {
        return ratingRepository.findByMovieId(movieId);
    }

    @Transactional(readOnly = true)
    public List<Rating> findByUser(String userName) {
        return ratingRepository.findByUserName(userName);
    }

    public Optional<Rating> findUserRatingForMovie(Long movieId, String userName) {
        return ratingRepository.findFirstByMovieIdAndUserName(movieId, userName);
    }

    public boolean hasUserRatedMovie(Long movieId, String userName) {
        return ratingRepository.existsByMovieIdAndUserName(movieId, userName);
    }

    private void updateMovieRating(Long movieId) {
        List<Rating> ratings = ratingRepository.findByMovieId(movieId);

        Movie movie = movieService.findById(movieId)
                .orElseThrow(() -> new IllegalArgumentException("Film nie istnieje"));

        if (ratings.isEmpty()) {
            movie.setRating(0.0);
            movie.setRatingsCount(0);
        } else {
            double averageRating = ratings.stream()
                    .mapToDouble(rating -> rating.getScore().getStarValue())
                    .average()
                    .orElse(0.0);

            averageRating = Math.round(averageRating * 10.0) / 10.0;

            movie.setRating(averageRating);
            movie.setRatingsCount(ratings.size());
        }

        movieService.save(movie);
    }
}
