package com.project.movieratingsystem.views.reviews;

import com.project.movieratingsystem.model.Rating;
import com.project.movieratingsystem.services.RatingService;
import com.project.movieratingsystem.views.MainLayout;
import com.project.movieratingsystem.views.util.ConfirmationDialogs;
import com.project.movieratingsystem.views.util.RatingFormatter;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.List;

@PageTitle("Moje recenzje")
@Route(value = "reviews", layout = MainLayout.class)
public class ReviewsView extends VerticalLayout {
    private final RatingService ratingService;
    private Grid<Rating> reviewGrid;
    private String currentUserName;
    private TextField userNameField;

    public ReviewsView(RatingService ratingService) {
        this.ratingService = ratingService;

        userNameField = new TextField("Nazwa użytkownika");
        userNameField.setPlaceholder("Wpisz nazwę użytkownika");
        userNameField.setWidth("300px");

        Button searchButton = new Button("Pokaż recenzje", e -> {
            if (userNameField.getValue() != null && !userNameField.getValue().trim().isEmpty()) {
                currentUserName = userNameField.getValue().trim();
                updateReviewList();
            } else {
                Notification.show("Wpisz nazwę użytkownika!", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout searchLayout = new HorizontalLayout(userNameField, searchButton);
        searchLayout.setAlignItems(FlexComponent.Alignment.END);


        reviewGrid = new Grid<>(Rating.class, false);
        reviewGrid.addColumn(rating -> rating.getMovie().getTitle()).setHeader("Film").setSortable(true);
        reviewGrid.addColumn(Rating::getUserName).setHeader("Autor recenzji").setSortable(true);
        reviewGrid.addColumn(rating -> rating.getMovie().getGenre().getDisplayName()).setHeader("Gatunek").setSortable(true);
        reviewGrid.addColumn(new ComponentRenderer<>(rating -> {
            String stars = rating.getScore().getDisplayName();
            return new Span(stars);
        })).setHeader("Ocena").setSortable(true);
        reviewGrid.addColumn(Rating::getComment).setHeader("Komentarz").setFlexGrow(2);
        reviewGrid.addColumn(rating -> rating.getCreatedAt().toLocalDate()).setHeader("Data").setSortable(true);

        reviewGrid.addColumn(new ComponentRenderer<>(rating -> {
            String text = RatingFormatter.formatSummary(rating.getMovie().getRating(), rating.getMovie().getRatingsCount());
            return new Span(text);
        })).setHeader("Obecna ocena filmu");

        reviewGrid.addColumn(new ComponentRenderer<>(rating -> {
            HorizontalLayout buttons = new HorizontalLayout();

            Button editButton = new Button(new Icon(VaadinIcon.EDIT));
            editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
            editButton.addClickListener(e -> openEditDialog(rating));

            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            deleteButton.addClickListener(e -> confirmDelete(rating));

            buttons.add(editButton, deleteButton);
            return buttons;
        })).setHeader("Akcje");

        reviewGrid.setHeightFull();

        add(searchLayout, reviewGrid);
        setSizeFull();

        updateReviewList();
    }

    private void updateReviewList() {
        List<Rating> ratings;
        if (currentUserName != null && !currentUserName.isEmpty()) {
            ratings = ratingService.findByUser(currentUserName);
        } else {
            ratings = ratingService.findAllRatings();
        }

        reviewGrid.setItems(ratings);

        if (ratings.isEmpty()) {
            Notification.show(
                    currentUserName != null && !currentUserName.isEmpty()
                            ? "Użytkownik nie ma żadnych recenzji"
                            : "Nie ma jeszcze wystawionych recenzji",
                    3000,
                    Notification.Position.MIDDLE
            ).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void openEditDialog(Rating rating) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Edytuj recenzję: " + rating.getMovie().getTitle());

        VerticalLayout content = new VerticalLayout();

        RadioButtonGroup<Integer> ratingGroup = new RadioButtonGroup<>();
        ratingGroup.setLabel("Twoja ocena:");
        ratingGroup.setItems(1, 2, 3, 4, 5);
        ratingGroup.setValue(rating.getScore().ordinal() + 1);
        ratingGroup.setRenderer(new ComponentRenderer<>(score -> new Span(RatingFormatter.starsWithScore(score))));

        TextArea commentArea = new TextArea("Komentarz (opcjonalny)");
        commentArea.setWidthFull();
        commentArea.setMaxLength(1000);
        commentArea.setValue(rating.getComment() != null ? rating.getComment() : "");

        content.add(ratingGroup, commentArea);
        dialog.add(content);

        Button saveButton = new Button("Zapisz zmiany", e -> {
            if (ratingGroup.getValue() == null) {
                Notification.show("Wybierz ocenę!", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            try {
                ratingService.updateRating(rating.getId(), ratingGroup.getValue(), commentArea.getValue(), currentUserName);

                Notification.show("Recenzja została zaktualizowana! Ocena filmu została automatycznie przeliczona.", 4000, Notification.Position.TOP_CENTER).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                dialog.close();

                updateReviewList();
            } catch (Exception ex) {
                Notification.show("Błąd: " + ex.getMessage(), 5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Anuluj", e -> dialog.close());
        dialog.getFooter().add(cancelButton, saveButton);

        dialog.open();
    }

    private void confirmDelete(Rating rating) {
        ConfirmationDialogs.confirm(
                "Potwierdzenie usunięcia",
                List.of(
                        "Czy na pewno chcesz usunąć recenzję filmu:",
                        "\"" + rating.getMovie().getTitle() + "\"?",
                        "Ocena filmu zostanie automatycznie przeliczona."
                ),
                "Usuń",
                dialog -> {
                    try {
                        ratingService.deleteRating(rating.getId(), currentUserName);
                        Notification.show("Recenzja została usunięta! Ocena filmu została automatycznie przeliczona.",
                                        4000, Notification.Position.TOP_CENTER)
                                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                        dialog.close();
                        updateReviewList();
                    } catch (Exception ex) {
                        Notification.show("Błąd: " + ex.getMessage(), 5000, Notification.Position.MIDDLE)
                                .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                }
        );
    }
}
