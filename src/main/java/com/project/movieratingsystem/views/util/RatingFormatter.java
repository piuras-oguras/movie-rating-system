package com.project.movieratingsystem.views.util;

/**
 * Formats {@link com.project.movieratingsystem.model.Movie} rating values and
 * {@link com.project.movieratingsystem.model.RatingStar} scores into display text,
 * shared by every view that renders a movie's rating.
 */
public final class RatingFormatter {

    private RatingFormatter() {
    }

    public static String formatSummary(Double rating, Integer ratingsCount) {
        if (rating == null || rating <= 0) {
            return "Brak ocen";
        }
        String base = String.format("%.1f", rating);
        if (ratingsCount == null || ratingsCount <= 0) {
            return base;
        }
        return base + " (" + ratingsCount + " " + reviewsWord(ratingsCount) + ")";
    }

    public static String formatOutOfFive(Double rating, Integer ratingsCount) {
        if (rating == null || rating <= 0) {
            return "Brak ocen";
        }
        String base = String.format("%.1f / 5", rating);
        if (ratingsCount == null || ratingsCount <= 0) {
            return base;
        }
        return base + " (" + ratingsCount + " " + reviewsWord(ratingsCount) + ")";
    }

    public static String starsWithScore(int score) {
        return "★".repeat(score) + "☆".repeat(5 - score) + " (" + score + ")";
    }

    private static String reviewsWord(int count) {
        if (count == 1) {
            return "recenzja";
        }
        if (count < 5) {
            return "recenzje";
        }
        return "recenzji";
    }
}
