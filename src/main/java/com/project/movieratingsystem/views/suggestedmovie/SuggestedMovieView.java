package com.project.movieratingsystem.views.suggestedmovie;

import com.project.movieratingsystem.model.Genre;
import com.project.movieratingsystem.model.Movie;
import com.project.movieratingsystem.model.RatingStar;
import com.project.movieratingsystem.services.MovieService;
import com.project.movieratingsystem.views.MainLayout;
import com.project.movieratingsystem.views.details.DetailsView;
import com.project.movieratingsystem.views.util.RatingFormatter;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@PageTitle("Losowanie filmu")
@Route(value = "randomMovie", layout = MainLayout.class)
public class SuggestedMovieView extends VerticalLayout {

    private final MovieService movieService;
    private final Random random = new Random();
    private VerticalLayout movieDisplayLayout;
    private ComboBox<Genre> genreComboBox;
    private ComboBox<RatingStar> minRatingComboBox;
    private ComboBox<RatingStar> maxRatingComboBox;
    private Span availableMoviesSpan;
    private List<Movie> recentlyRandomizedMovies = new ArrayList<>();
    private final int MAX_RECENT_MOVIES = 5;

    public SuggestedMovieView(MovieService movieService) {
        this.movieService = movieService;

        VerticalLayout controlPanel = createControlPanel();

        movieDisplayLayout = new VerticalLayout();
        movieDisplayLayout.setAlignItems(Alignment.CENTER);
        movieDisplayLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        movieDisplayLayout.setWidthFull();
        movieDisplayLayout.setMinHeight("400px");
        movieDisplayLayout.getStyle()
                .set("border", "2px dashed #ccc")
                .set("border-radius", "10px")
                .set("background-color", "#f9f9f9")
                .set("padding", "20px");

        showInitialMessage();

        add(controlPanel, movieDisplayLayout);
        setSizeFull();
        setPadding(false);
        setSpacing(true);
        setAlignItems(Alignment.CENTER);
        getStyle().set("overflow", "auto");
    }

    private VerticalLayout createControlPanel() {
        genreComboBox = new ComboBox<>("Gatunek");
        genreComboBox.setItems(Genre.values());
        genreComboBox.setItemLabelGenerator(Genre::getDisplayName);
        genreComboBox.setPlaceholder("Dowolny");
        genreComboBox.setClearButtonVisible(true);
        genreComboBox.addValueChangeListener(e -> updateAvailableMoviesCount());

        minRatingComboBox = new ComboBox<>("Ocena min.");
        minRatingComboBox.setItems(RatingStar.values());
        minRatingComboBox.setItemLabelGenerator(RatingStar::getDisplayName);
        minRatingComboBox.setPlaceholder("Dowolna");
        minRatingComboBox.setClearButtonVisible(true);
        minRatingComboBox.addValueChangeListener(e -> updateAvailableMoviesCount());

        maxRatingComboBox = new ComboBox<>("Ocena max.");
        maxRatingComboBox.setItems(RatingStar.values());
        maxRatingComboBox.setItemLabelGenerator(RatingStar::getDisplayName);
        maxRatingComboBox.setPlaceholder("Dowolna");
        maxRatingComboBox.setClearButtonVisible(true);
        maxRatingComboBox.addValueChangeListener(e -> updateAvailableMoviesCount());

        HorizontalLayout filterLayout = new HorizontalLayout(genreComboBox, minRatingComboBox, maxRatingComboBox);
        filterLayout.setSpacing(true);
        availableMoviesSpan = new Span();
        availableMoviesSpan.getStyle().set("font-weight", "bold");
        updateAvailableMoviesCount();

        Button randomizeButton = new Button( "Zasugeruj film!");
        randomizeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        randomizeButton.addClickListener(e -> randomizeMovie());

        Button clearFiltersButton = new Button("Wyczyść filtry");
        clearFiltersButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        clearFiltersButton.addClickListener(e -> clearFilters());

        HorizontalLayout buttonLayout = new HorizontalLayout(randomizeButton, clearFiltersButton);
        buttonLayout.setSpacing(true);
        buttonLayout.setAlignItems(Alignment.END);

        VerticalLayout controlPanel = new VerticalLayout(filterLayout, availableMoviesSpan, buttonLayout);
        controlPanel.setAlignItems(Alignment.CENTER);
        controlPanel.setSpacing(true);
        controlPanel.getStyle()
                .set("border", "1px solid #e0e0e0")
                .set("border-radius", "8px")
                .set("padding", "16px");
        controlPanel.setWidth("auto");

        return controlPanel;
    }

    private void updateAvailableMoviesCount() {
        Genre selectedGenre = genreComboBox.getValue();
        RatingStar minRating = minRatingComboBox.getValue();
        RatingStar maxRating = maxRatingComboBox.getValue();

        long count = movieService.countMoviesWithCriteria(selectedGenre, minRating, maxRating);

        if (count == 0) {
            availableMoviesSpan.setText("Brak filmów spełniających kryteria");
            availableMoviesSpan.getStyle().set("color", "var(--lumo-error-text-color)");
        } else {
            availableMoviesSpan.setText("Dostępne filmy: " + count);
            availableMoviesSpan.getStyle().set("color", "var(--lumo-primary-text-color)");
        }
    }

    private void showInitialMessage() {
        movieDisplayLayout.removeAll();
        movieDisplayLayout.setJustifyContentMode(JustifyContentMode.CENTER);

        H3 message = new H3("Kliknij 'Zasugeruj film', aby znaleźć coś dla siebie");
        message.getStyle().set("color", "#666").set("text-align", "center");

        long totalMovies = movieService.countMoviesWithCriteria(null, null, null);
        Span totalMoviesSpan = new Span("W naszej bazie mamy obecnie " + totalMovies + " filmów.");
        totalMoviesSpan.getStyle().set("font-weight", "bold").set("margin-top", "20px");

        movieDisplayLayout.add( message, totalMoviesSpan);
    }

    private void clearFilters() {
        genreComboBox.clear();
        minRatingComboBox.clear();
        maxRatingComboBox.clear();
        updateAvailableMoviesCount();
        Notification.show("Filtry wyczyszczone", 2000, Notification.Position.BOTTOM_CENTER);
    }

    private void randomizeMovie() {
        Genre selectedGenre = genreComboBox.getValue();
        RatingStar minRating = minRatingComboBox.getValue();
        RatingStar maxRating = maxRatingComboBox.getValue();

        if (minRating != null && maxRating != null && minRating.getStarValue() > maxRating.getStarValue()) {
            Notification.show("Minimalna ocena nie może być większa od maksymalnej!", 4000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        List<Movie> available = movieService.findMoviesForRandomization(selectedGenre, minRating, maxRating);
        if (available.isEmpty()) {
            showNoMoviesMessage();
            return;
        }

        List<Movie> candidates = available.stream()
                .filter(m -> !recentlyRandomizedMovies.contains(m))
                .collect(Collectors.toList());

        boolean avoided = !candidates.equals(available);
        if (candidates.isEmpty()) {
            recentlyRandomizedMovies.clear();
            candidates = available;
            Notification.show("Wszystkie pasujące filmy były już zasugerowane. Zaczynamy od nowa!",
                            3000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        }

        Movie randomMovie = candidates.stream()
                .skip(random.nextInt(candidates.size()))
                .findFirst()
                .get();

        recentlyRandomizedMovies = Stream.concat(Stream.of(randomMovie), recentlyRandomizedMovies.stream())
                .limit(MAX_RECENT_MOVIES)
                .collect(Collectors.toList());

        displayRandomMovie(randomMovie);

        String info = "Wylosowano spośród " + candidates.size() + " filmów" +
                (avoided ? " (unikając ostatnich powtórzeń)" : "") + "!";
        Notification.show(info, 3000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }


    private void showNoMoviesMessage() {
        movieDisplayLayout.removeAll();
        movieDisplayLayout.setJustifyContentMode(JustifyContentMode.CENTER);

        Icon sadIcon = VaadinIcon.FROWN_O.create();
        sadIcon.setSize("64px");
        sadIcon.setColor("#ff9800");

        H3 message = new H3("Niestety, brak filmów spełniających wybrane kryteria");
        message.getStyle().set("color", "#666").set("text-align", "center");

        Paragraph suggestion = new Paragraph("Spróbuj zmienić lub wyczyścić filtry.");
        suggestion.getStyle().set("color", "#999").set("text-align", "center");

        movieDisplayLayout.add(sadIcon, message, suggestion);
    }

    private void displayRandomMovie(Movie movie) {
        movieDisplayLayout.removeAll();
        movieDisplayLayout.setJustifyContentMode(JustifyContentMode.START);

        VerticalLayout movieCard = new VerticalLayout();
        movieCard.setAlignItems(Alignment.CENTER);
        movieCard.setSpacing(true);
        movieCard.setWidthFull();
        movieCard.setMaxWidth("500px"); // Lepszy rozmiar dla czytelności
        movieCard.getStyle()
                .set("background-color", "white")
                .set("border-radius", "10px")
                .set("box-shadow", "0 4px 8px rgba(0,0,0,0.1)")
                .set("padding", "20px");

        H2 titleHeader = new H2(movie.getTitle());
        titleHeader.getStyle().set("text-align", "center").set("color", "#2196F3").set("margin", "0");

        Image poster = new Image(movie.getImageUrl(), "Plakat filmu " + movie.getTitle());
        poster.setWidth("200px");
        poster.setHeight("300px");
        poster.getStyle().set("object-fit", "cover").set("border-radius", "8px");

        FormLayout infoLayout = new FormLayout();
        infoLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("400px", 2)
        );

        infoLayout.add(createMovieInfoLine("Reżyser:", movie.getDirector()));
        infoLayout.add(createMovieInfoLine("Rok:", String.valueOf(movie.getReleaseYear())));

        Span genre = new Span(movie.getGenre().getDisplayName());
        genre.getElement().getThemeList().add("badge");
        infoLayout.addFormItem(genre, "Gatunek:");

        String ratingText = RatingFormatter.formatOutOfFive(movie.getRating(), movie.getRatingsCount());
        infoLayout.add(createMovieInfoLine("Ocena:", ratingText));

        Paragraph description = new Paragraph(movie.getDescription());
        description.getStyle()
                .set("text-align", "justify")
                .set("color", "#666")
                .set("margin", "15px 0");

        Button detailsButton = new Button("Zobacz szczegóły", e ->
                getUI().ifPresent(ui -> ui.navigate(DetailsView.class, movie.getId()))
        );
        detailsButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button randomizeAgainButton = new Button("Zasugeruj ponownie", e -> randomizeMovie());
        randomizeAgainButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);

        HorizontalLayout buttonLayout = new HorizontalLayout(detailsButton, randomizeAgainButton);
        buttonLayout.setSpacing(true);

        movieCard.add(titleHeader, poster, infoLayout, description, buttonLayout);
        movieDisplayLayout.add(movieCard);
    }

    private HorizontalLayout createMovieInfoLine(String label, String value) {
        Span labelSpan = new Span(label);
        labelSpan.getStyle().set("font-weight", "bold");

        Span valueSpan = new Span(value);

        HorizontalLayout layout = new HorizontalLayout(labelSpan, valueSpan);
        layout.setSpacing(true);
        return layout;
    }
}
