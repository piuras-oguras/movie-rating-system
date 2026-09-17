package com.project.movieratingsystem.views.moviemanagement;

import com.project.movieratingsystem.model.Genre;
import com.project.movieratingsystem.model.Movie;
import com.project.movieratingsystem.services.MovieService;
import com.project.movieratingsystem.views.MainLayout;
import com.project.movieratingsystem.views.homepage.HomePage;
import com.project.movieratingsystem.views.util.ConfirmationDialogs;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.List;

@PageTitle("Zarządzanie filmami")
@Route(value = "movie-management", layout = MainLayout.class)
public class MovieManagementView extends VerticalLayout implements BeforeEnterObserver {

    private final MovieService movieService;
    private Grid<Movie> movieGrid;

    public MovieManagementView(MovieService movieService) {
        this.movieService = movieService;

        Button addButton = new Button("Dodaj nowy film", e -> openMovieDialog(null));
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.setIcon(new Icon(VaadinIcon.PLUS));

        HorizontalLayout header = new HorizontalLayout(addButton);
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        movieGrid = new Grid<>(Movie.class, false);
        movieGrid.addColumn(Movie::getTitle)
                .setHeader("Tytuł")
                .setSortable(true);
        movieGrid.addColumn(Movie::getDirector)
                .setHeader("Reżyser")
                .setSortable(true);
        movieGrid.addColumn(movie -> movie.getGenre().getDisplayName())
                .setHeader("Gatunek")
                .setSortable(true);
        movieGrid.addColumn(Movie::getReleaseYear)
                .setHeader("Rok")
                .setSortable(true);
        movieGrid.addColumn(Movie::getRating)
                .setHeader("Ocena")
                .setSortable(true);
        movieGrid.addColumn(new ComponentRenderer<>(movie -> {
            HorizontalLayout buttons = new HorizontalLayout();

            Button editButton = new Button(new Icon(VaadinIcon.EDIT));
            editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
            editButton.addClickListener(e -> openMovieDialog(movie));

            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            deleteButton.addClickListener(e -> confirmDelete(movie));

            buttons.add(editButton, deleteButton);
            return buttons;
        })).setHeader("Akcje");

        movieGrid.setHeightFull();

        add(header, movieGrid);
        setSizeFull();

        updateMovieList();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!MainLayout.isCurrentUserAdmin()) {
            Notification.show("Brak uprawnień. Tylko administrator może zarządzać filmami.", 4000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            event.forwardTo(HomePage.class);
        }
    }

    private void updateMovieList() {
        List<Movie> movies = movieService.findAllMovies();
        movieGrid.setItems(movies);
    }

    private void openMovieDialog(Movie movie) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(movie == null ? "Dodaj nowy film" : "Edytuj film");
        dialog.setWidth("600px");

        FormLayout formLayout = new FormLayout();

        TextField titleField = new TextField("Tytuł");
        titleField.setRequired(true);

        TextField directorField = new TextField("Reżyser");
        directorField.setRequired(true);

        ComboBox<Genre> genreComboBox = new ComboBox<>("Gatunek");
        genreComboBox.setItems(Genre.values());
        genreComboBox.setItemLabelGenerator(Genre::getDisplayName);
        genreComboBox.setRequired(true);

        IntegerField yearField = new IntegerField("Rok wydania");
        yearField.setMin(1900);
        yearField.setMax(2030);
        yearField.setRequired(true);

        NumberField ratingField = new NumberField("Ocena");
        ratingField.setMin(0.0);
        ratingField.setMax(5.0);
        ratingField.setStep(0.1);
        ratingField.setValue(0.0);

        TextArea descriptionArea = new TextArea("Opis");
        descriptionArea.setMaxLength(1500);
        descriptionArea.setHeight("120px");

        TextField imageUrlField = new TextField("URL obrazka");
        imageUrlField.setPlaceholder("images/example.jpg");

        TextField trailerUrlField = new TextField("URL trailera");
        trailerUrlField.setPlaceholder("https://www.youtube.com/embed/...");

        if (movie != null) {
            titleField.setValue(movie.getTitle() != null ? movie.getTitle() : "");
            directorField.setValue(movie.getDirector() != null ? movie.getDirector() : "");
            genreComboBox.setValue(movie.getGenre());
            yearField.setValue(movie.getReleaseYear());
            ratingField.setValue(movie.getRating() != null ? movie.getRating() : 0.0);
            descriptionArea.setValue(movie.getDescription() != null ? movie.getDescription() : "");
            imageUrlField.setValue(movie.getImageUrl() != null ? movie.getImageUrl() : "");
            trailerUrlField.setValue(movie.getTrailerUrl() != null ? movie.getTrailerUrl() : "");
        }

        formLayout.add(titleField, directorField, genreComboBox, yearField,
                ratingField, descriptionArea, imageUrlField, trailerUrlField);

        dialog.add(formLayout);

        Button saveButton = new Button("Zapisz", e -> {
            if (titleField.isEmpty() || directorField.isEmpty() ||
                    genreComboBox.isEmpty() || yearField.isEmpty()) {
                Notification.show("Wypełnij wszystkie wymagane pola!", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            try {
                Movie movieToSave = movie != null ? movie : new Movie();
                movieToSave.setTitle(titleField.getValue());
                movieToSave.setDirector(directorField.getValue());
                movieToSave.setGenre(genreComboBox.getValue());
                movieToSave.setReleaseYear(yearField.getValue());
                movieToSave.setRating(ratingField.getValue());
                movieToSave.setDescription(descriptionArea.getValue());
                movieToSave.setImageUrl(imageUrlField.getValue());
                movieToSave.setTrailerUrl(trailerUrlField.getValue());

                movieService.save(movieToSave);

                String message = movie == null ? "Film został dodany!" : "Film został zaktualizowany!";
                Notification.show(message, 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                dialog.close();
                updateMovieList();
            } catch (Exception ex) {
                Notification.show("Błąd: " + ex.getMessage(), 5000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Anuluj", e -> dialog.close());

        dialog.getFooter().add(cancelButton, saveButton);
        dialog.open();
    }

    private void confirmDelete(Movie movie) {
        ConfirmationDialogs.confirm(
                "Potwierdzenie usunięcia",
                List.of(
                        "Czy na pewno chcesz usunąć film:",
                        "\"" + movie.getTitle() + "\" (" + movie.getReleaseYear() + ")?",
                        "Ta operacja jest nieodwracalna!"
                ),
                "Usuń",
                dialog -> {
                    try {
                        movieService.deleteById(movie.getId());
                        Notification.show("Film został usunięty!", 3000, Notification.Position.TOP_CENTER)
                                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                        dialog.close();
                        updateMovieList();
                    } catch (Exception ex) {
                        Notification.show("Błąd: " + ex.getMessage(), 5000, Notification.Position.MIDDLE)
                                .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                }
        );
    }
}
