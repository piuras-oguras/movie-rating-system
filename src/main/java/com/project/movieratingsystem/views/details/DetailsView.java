package com.project.movieratingsystem.views.details;

import com.project.movieratingsystem.model.Movie;
import com.project.movieratingsystem.model.Rating;
import com.project.movieratingsystem.model.RatingStar;
import com.project.movieratingsystem.services.MovieService;
import com.project.movieratingsystem.services.RatingService;
import com.project.movieratingsystem.views.MainLayout;
import com.project.movieratingsystem.views.homepage.HomePage;
import com.project.movieratingsystem.views.util.RatingFormatter;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.util.List;
import java.util.Optional;

@PageTitle("Szczegóły filmu")
@Route(value = "details", layout = MainLayout.class)
public class DetailsView extends VerticalLayout implements HasUrlParameter<Long> {

    private final MovieService movieService;
    private final VerticalLayout contentLayout;
    private final RatingService ratingService;
    private VerticalLayout reviewsContainer;
    private HorizontalLayout ratingLayout;
    private Movie currentMovie;

    public DetailsView(MovieService movieService,
                       RatingService ratingService) {
        this.movieService = movieService;
        this.ratingService = ratingService;
        this.contentLayout = new VerticalLayout();
        contentLayout.setWidthFull();
        contentLayout.setPadding(false);

        add(contentLayout);
        setSizeFull();
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter Long parameter) {
        if (parameter == null) {
            contentLayout.add(new H2("Nie podano ID filmu."));
        } else {
            Optional<Movie> movieOpt = movieService.findById(parameter);
            movieOpt.ifPresentOrElse(
                    movie -> {
                        this.currentMovie = movie;
                        buildLayout(movie);
                    },
                    () -> contentLayout.add(new H2("Film o podanym ID nie istnieje."))
            );
        }
    }

    private void buildLayout(Movie movie) {
        contentLayout.removeAll();

        RouterLink backLink = new RouterLink(" Wróć do listy", HomePage.class);
        backLink.setId(String.valueOf(VaadinIcon.ARROW_LEFT.create()));
        backLink.addClassName(LumoUtility.Margin.Bottom.LARGE);

        Image poster = new Image(movie.getImageUrl(), "Plakat filmu " + movie.getTitle());
        poster.setHeight("450px");
        poster.addClassName("details-poster");

        H1 title = new H1(movie.getTitle());
        title.addClassName(LumoUtility.Margin.Top.NONE);

        Span director = new Span("Reżyser: " + movie.getDirector());
        Span year = new Span("Rok: " + movie.getReleaseYear());
        Span genre = new Span(movie.getGenre().getDisplayName());
        genre.getElement().getThemeList().add("badge");
        genre.addClassName(movie.getGenre().name().toLowerCase());
        HorizontalLayout metaLayout = new HorizontalLayout(director, year, genre);
        metaLayout.setSpacing(true);

        Paragraph description = new Paragraph(movie.getDescription());
        description.addClassName(LumoUtility.Margin.Top.LARGE);

        IFrame videoFrame = new IFrame(movie.getTrailerUrl());
        videoFrame.setWidth("300px");
        videoFrame.setHeight("150px");
        videoFrame.getElement().setAttribute("allowfullscreen", "");
        videoFrame.getElement().setAttribute("allow", "accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture");

        ratingLayout = createRatingLayout(movie);

        reviewsContainer = new VerticalLayout();
        reviewsContainer.setWidthFull();
        reviewsContainer.setMaxHeight("400px");
        reviewsContainer.getStyle().set("overflow-y", "auto");
        reviewsContainer.getStyle().set("border", "1px solid #e0e0e0");
        reviewsContainer.getStyle().set("padding", "10px");
        loadReviews(movie.getId());

        Button rateButton = new Button("Oceń");
        rateButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        rateButton.addClickListener(e -> openRatingDialog(movie));

        VerticalLayout infoLayout = new VerticalLayout(title, metaLayout, description, ratingLayout, reviewsContainer, rateButton);
        infoLayout.setPadding(false);
        infoLayout.setSpacing(true);

        HorizontalLayout mainContent = new HorizontalLayout(poster, infoLayout);
        mainContent.addClassName(LumoUtility.Gap.LARGE);
        mainContent.setWidthFull();

        contentLayout.add(backLink, mainContent, videoFrame);
    }

    private HorizontalLayout createRatingLayout(Movie movie) {
        Icon starIcon = VaadinIcon.STAR.create();
        String ratingText = RatingFormatter.formatSummary(movie.getRating(), movie.getRatingsCount());
        Span movieRating = new Span(ratingText);
        HorizontalLayout layout = new HorizontalLayout(starIcon, movieRating);
        layout.setAlignItems(Alignment.CENTER);
        return layout;
    }

    private void loadReviews(Long movieId) {
        reviewsContainer.removeAll();
        List<Rating> ratings = ratingService.findByMovie(movieId);
        ratings.forEach(review -> reviewsContainer.add(createReviewLayout(review)));
    }

    private void openRatingDialog(Movie movie) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Oceń film: " + movie.getTitle());

        VerticalLayout content = new VerticalLayout();

        String currentUserName = MainLayout.getCurrentUserName();
        Optional<Rating> existingRating = currentUserName != null
                ? ratingService.findUserRatingForMovie(movie.getId(), currentUserName)
                : Optional.empty();

        Span infoSpan = new Span();
        infoSpan.getStyle().set("color", "var(--lumo-warning-text-color)");

        RadioButtonGroup<Integer> ratingGroup = new RadioButtonGroup<>();
        ratingGroup.setLabel("Twoja ocena:");
        ratingGroup.setItems(1, 2, 3, 4, 5);
        ratingGroup.setRenderer(new ComponentRenderer<>(score -> new Span(RatingFormatter.starsWithScore(score))));

        TextArea commentArea = new TextArea("Komentarz (opcjonalny)");
        commentArea.setWidthFull();
        commentArea.setMaxLength(1000);

        if (existingRating.isPresent()) {
            Rating rating = existingRating.get();
            ratingGroup.setValue(rating.getScore().ordinal() + 1);
            commentArea.setValue(rating.getComment() != null ? rating.getComment() : "");
        }

        content.add(infoSpan, ratingGroup, commentArea);
        dialog.add(content);

        Button saveButton = new Button("Zapisz", e -> {
            Integer stars = ratingGroup.getValue();

            if (stars == null) {
                Notification.show("Wybierz ocenę!", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            String userName = MainLayout.getCurrentUserName();
            if (userName == null) {
                Notification.show("Musisz się zalogować, aby ocenić film!", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            RatingStar ratingStar = RatingStar.values()[stars - 1];
            boolean hasExistingRating = ratingService.hasUserRatedMovie(movie.getId(), userName);

            ratingService.addRating(movie.getId(), ratingStar, commentArea.getValue(), userName);

            refreshMovieData();

            loadReviews(movie.getId());

            String message = hasExistingRating ? "Ocena została zaktualizowana!" : "Ocena została dodana!";
            Notification.show(message, 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            dialog.close();
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Anuluj", e -> dialog.close());
        dialog.getFooter().add(cancelButton, saveButton);

        dialog.open();
    }

    private void refreshMovieData() {
        Optional<Movie> updatedMovieOpt = movieService.findById(currentMovie.getId());
        if (updatedMovieOpt.isPresent()) {
            currentMovie = updatedMovieOpt.get();

            HorizontalLayout newRatingLayout = createRatingLayout(currentMovie);

            VerticalLayout parent = (VerticalLayout) ratingLayout.getParent().orElse(null);
            if (parent != null) {
                int index = parent.indexOf(ratingLayout);
                parent.remove(ratingLayout);
                parent.addComponentAtIndex(index, newRatingLayout);
                ratingLayout = newRatingLayout;
            }
        }
    }

    private VerticalLayout createReviewLayout(Rating review) {
        HorizontalLayout header = new HorizontalLayout(
                new Span(review.getUserName()),
                new Span(review.getScore().getDisplayName()),
                new Span(review.getCreatedAt().toLocalDate().toString())
        );
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);

        VerticalLayout reviewLayout = new VerticalLayout(header);
        if (review.getComment() != null && !review.getComment().isEmpty()) {
            reviewLayout.add(new Span(review.getComment()));
        }
        reviewLayout.getStyle()
                .set("border", "1px solid #ddd")
                .set("padding", "10px")
                .set("margin", "5px 0");
        return reviewLayout;
    }
}
