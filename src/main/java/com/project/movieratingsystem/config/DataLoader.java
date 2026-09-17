package com.project.movieratingsystem.config;

import com.project.movieratingsystem.model.Genre;
import com.project.movieratingsystem.model.Movie;
import com.project.movieratingsystem.services.MovieService;
import com.project.movieratingsystem.services.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private final MovieService movieService;
    private final UserService userService;
    private final String adminUsername;
    private final String adminPasswordHash;

    public DataLoader(MovieService movieService,
                       UserService userService,
                       @Value("${app.admin.username}") String adminUsername,
                       @Value("${app.admin.password-hash}") String adminPasswordHash) {
        this.movieService = movieService;
        this.userService = userService;
        this.adminUsername = adminUsername;
        this.adminPasswordHash = adminPasswordHash;
    }

    @Override
    public void run(String... args) {
        userService.ensureAdminAccount(adminUsername, adminPasswordHash);

        if (movieService.findMovies("", null, null, null, null).isEmpty()) {
            Movie m1 = new Movie();
            m1.setTitle("The Matrix");
            m1.setGenre(Genre.SCI_FI);
            m1.setReleaseYear(1999);
            m1.setRating(0.0);
            m1.setRatingsCount(0);
            m1.setDescription("Neo odkrywa prawdziwą naturę rzeczywistości.");
            m1.setDirector("Wachowscy");
            m1.setTrailerUrl("https://www.youtube.com/embed/GZ4GPOSQ6-g");
            m1.setImageUrl("images/matrix.jpg");

            movieService.save(m1);

            Movie m2 = new Movie();
            m2.setTitle("Incepcja");
            m2.setGenre(Genre.ACTION);
            m2.setReleaseYear(2010);
            m2.setRating(0.0);
            m2.setRatingsCount(0);
            m2.setDescription("Zespół specjalistów wkrada się do snów.");
            m2.setDirector("Christopher Nolan");
            m2.setTrailerUrl("https://www.youtube.com/embed/bLvqoHBptjg");
            m2.setImageUrl("images/incepcja.jpeg");
            movieService.save(m2);
        }
    }
}