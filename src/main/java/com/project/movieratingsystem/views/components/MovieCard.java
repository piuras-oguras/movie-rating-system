package com.project.movieratingsystem.views.components;

import com.project.movieratingsystem.model.Movie;
import com.project.movieratingsystem.views.details.DetailsView;
import com.project.movieratingsystem.views.util.RatingFormatter;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;

public class MovieCard extends VerticalLayout {

    public MovieCard(Movie movie) {
        addClassName("movie-card");
        setWidth("250px");
        setPadding(false);
        setSpacing(false);

        Image poster = new Image(movie.getImageUrl(), "Plakat filmu " + movie.getTitle());
        poster.addClassName("movie-poster");
        poster.setWidth("200px");
        poster.setHeight("300px");
        poster.getStyle().set("object-fit", "cover");

        H3 title = new H3(movie.getTitle());
        title.addClassName("movie-title");

        Span director = new Span("Reżyser: " + movie.getDirector());
        director.addClassName("movie-director");

        Span year = new Span("Rok: " + movie.getReleaseYear());
        year.addClassName("movie-year");

        Span genreBadge = new Span(movie.getGenre().getDisplayName());
        genreBadge.getElement().getThemeList().add("badge");
        genreBadge.addClassName(movie.getGenre().name().toLowerCase());

        Icon starIcon = VaadinIcon.STAR.create();
        starIcon.addClassName("rating-icon");

        String ratingText = RatingFormatter.formatSummary(movie.getRating(), movie.getRatingsCount());
        Span ratingValue = new Span(ratingText);
        ratingValue.addClassName("rating-value");

        HorizontalLayout ratingLayout = new HorizontalLayout(starIcon, ratingValue);
        ratingLayout.setAlignItems(Alignment.CENTER);
        ratingLayout.addClassName(LumoUtility.Margin.Top.MEDIUM);

        Button detailsButton = new Button("Zobacz szczegóły", event ->
                getUI().ifPresent(ui -> ui.navigate(DetailsView.class, movie.getId()))
        );
        detailsButton.addClassName("details-link");

        VerticalLayout contentWrapper = new VerticalLayout(title, director, year, genreBadge, ratingLayout);
        contentWrapper.setPadding(true);
        contentWrapper.getStyle().set("flex-grow", "1");

        add(poster, contentWrapper, detailsButton);
    }
}
