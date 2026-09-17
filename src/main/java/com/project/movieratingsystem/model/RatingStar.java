package com.project.movieratingsystem.model;

import lombok.Getter;

@Getter
public enum RatingStar {
    ONE_STAR("★☆☆☆☆", 1.0),
    TWO_STARS("★★☆☆☆", 2.0),
    THREE_STARS("★★★☆☆", 3.0),
    FOUR_STARS("★★★★☆", 4.0),
    FIVE_STARS("★★★★★", 5.0);

    private final String displayName;
    private final double starValue;

    RatingStar(String displayName, double starValue) {
        this.displayName = displayName;
        this.starValue = starValue;
    }
}
