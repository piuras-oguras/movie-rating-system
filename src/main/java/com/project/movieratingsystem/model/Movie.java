package com.project.movieratingsystem.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "movies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int upVotes;

    private int downVotes;


    @Column(nullable = false)
    private Double rating = 0.0;

    @Column(nullable = false)
    private Integer ratingsCount = 0;

    @Column(name = "release_year", nullable = false)
    private Integer releaseYear;

    @Column(length = 1500)
    private String description;

    private String director;

    private String imageUrl;

    private String trailerUrl;

    private String title;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private Genre genre;

    @ManyToOne
    @JoinColumn(name="user_id")
    private User watchedMovie;


}
