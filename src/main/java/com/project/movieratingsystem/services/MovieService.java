package com.project.movieratingsystem.services;

import com.project.movieratingsystem.model.Genre;
import com.project.movieratingsystem.model.Movie;
import com.project.movieratingsystem.model.RatingStar;
import com.project.movieratingsystem.repository.MovieRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MovieService {

    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public List<Movie> findMovies(String title, String director, Genre genre, RatingStar ratingStarMax, RatingStar ratingStarMin) {
        Specification<Movie> spec = byGenreAndRatingRange(genre, ratingStarMin, ratingStarMax);

        if (title != null && !title.isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + title.toLowerCase() + "%"));
        }
        if (director != null && !director.isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("director")), "%" + director.toLowerCase() + "%"));
        }

        return movieRepository.findAll(spec);
    }

    @Transactional(readOnly = true)
    public List<Movie> findAllMovies() {
        return movieRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Movie> findById(Long id) {
        return movieRepository.findById(id);
    }

    public Movie save(Movie movie) {
        return movieRepository.save(movie);
    }

    public void deleteById(Long id) {
        if (!movieRepository.existsById(id)) {
            throw new IllegalArgumentException("Film o ID " + id + " nie istnieje");
        }
        movieRepository.deleteById(id);
    }

    public void delete(Movie movie) {
        movieRepository.delete(movie);
    }

    @Transactional(readOnly = true)
    public List<Movie> findMoviesForRandomization(Genre genre, RatingStar minRating, RatingStar maxRating) {
        return movieRepository.findAll(byGenreAndRatingRange(genre, minRating, maxRating));
    }

    @Transactional(readOnly = true)
    public long countMoviesWithCriteria(Genre genre, RatingStar minRating, RatingStar maxRating) {
        return movieRepository.count(byGenreAndRatingRange(genre, minRating, maxRating));
    }

    private Specification<Movie> byGenreAndRatingRange(Genre genre, RatingStar minRating, RatingStar maxRating) {
        Specification<Movie> spec = Specification.where(null);

        if (genre != null) {
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("genre"), genre));
        }
        if (minRating != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get("rating"), minRating.getStarValue()));
        }
        if (maxRating != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThanOrEqualTo(root.get("rating"), maxRating.getStarValue()));
        }

        return spec;
    }
}
