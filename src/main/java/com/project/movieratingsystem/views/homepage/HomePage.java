package com.project.movieratingsystem.views.homepage;

import com.project.movieratingsystem.model.Genre;
import com.project.movieratingsystem.model.Movie;
import com.project.movieratingsystem.model.RatingStar;
import com.project.movieratingsystem.services.MovieService;
import com.project.movieratingsystem.views.MainLayout;
import com.project.movieratingsystem.views.components.MovieCard;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;

import java.util.List;

@PageTitle("Baza ocen filmów")
@Route(value = "", layout = MainLayout.class)
@Uses(Icon.class)
public class HomePage extends Composite<VerticalLayout> {

    private final TextField nameMovie;
    private final TextField directorName;
    private final ComboBox<Genre> genreComboBox;
    private final ComboBox<RatingStar> starMinComboBox;
    private final ComboBox<RatingStar> starMaxComboBox;
    private final HorizontalLayout resultsContainer;
    private final MovieService movieService;

    public HomePage(MovieService movieService) {
        this.movieService = movieService;

        nameMovie = new TextField("Nazwa filmu");
        directorName = new TextField("Reżyser");
        genreComboBox = createGenreComboBox();
        starMinComboBox = createRatingStarMinComboBox();
        starMaxComboBox = createRatingStarMaxComboBox();

        Button searchButton = new Button("Szukaj");
        searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        searchButton.addClickListener(e -> searchMovies());

        Button refreshButton = new Button("Odśwież");
        refreshButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        refreshButton.addClickListener(e -> searchMovies());

        HorizontalLayout filterLayout = new HorizontalLayout(nameMovie, directorName, genreComboBox,
                starMinComboBox, starMaxComboBox, searchButton, refreshButton);
        filterLayout.setDefaultVerticalComponentAlignment(Alignment.END);
        filterLayout.setWidthFull();
        filterLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        filterLayout.addClassName(Gap.MEDIUM);

        resultsContainer = new HorizontalLayout();
        resultsContainer.getStyle().set("flex-wrap", "wrap");
        resultsContainer.setJustifyContentMode(JustifyContentMode.CENTER);
        resultsContainer.addClassName("movie-container");

        getContent().add(filterLayout, resultsContainer);
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");

        searchMovies();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        searchMovies();
    }

    private void searchMovies() {
        String titleFilter = nameMovie.getValue();
        String directorFilter = directorName.getValue();
        Genre genreFilter = genreComboBox.getValue();
        RatingStar ratingFilterMin = starMinComboBox.getValue();
        RatingStar ratingFilterMax = starMaxComboBox.getValue();

        List<Movie> foundMovies = movieService.findMovies(titleFilter, directorFilter, genreFilter, ratingFilterMin, ratingFilterMax);

        resultsContainer.removeAll();
        if (foundMovies.isEmpty()) {
            resultsContainer.add(new Span("Brak filmów spełniających kryteria."));
        } else {
            for (Movie movie : foundMovies) {
                resultsContainer.add(new MovieCard(movie));
            }
        }
    }

    private ComboBox<Genre> createGenreComboBox() {
        ComboBox<Genre> genreComboBox = new ComboBox<>("Gatunek");
        genreComboBox.setItems(Genre.values());
        genreComboBox.setItemLabelGenerator(Genre::getDisplayName);
        genreComboBox.setPlaceholder("Wszystkie");
        genreComboBox.setClearButtonVisible(true);
        return genreComboBox;
    }

    private ComboBox<RatingStar> createRatingStarMinComboBox() {
        ComboBox<RatingStar> starComboBox = new ComboBox<>("Ocena (minimum)");
        starComboBox.setItems(RatingStar.values());
        starComboBox.setItemLabelGenerator(RatingStar::getDisplayName);
        starComboBox.setPlaceholder("Dowolna");
        starComboBox.setClearButtonVisible(true);
        return starComboBox;
    }

    private ComboBox<RatingStar> createRatingStarMaxComboBox() {
        ComboBox<RatingStar> starComboBox = new ComboBox<>("Ocena (maximum)");
        starComboBox.setItems(RatingStar.values());
        starComboBox.setItemLabelGenerator(RatingStar::getDisplayName);
        starComboBox.setPlaceholder("Dowolna");
        starComboBox.setClearButtonVisible(true);
        return starComboBox;
    }
}
