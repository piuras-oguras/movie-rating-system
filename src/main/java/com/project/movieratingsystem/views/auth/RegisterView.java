package com.project.movieratingsystem.views.auth;

import com.project.movieratingsystem.model.User;
import com.project.movieratingsystem.services.UserService;
import com.project.movieratingsystem.views.MainLayout;
import com.project.movieratingsystem.views.homepage.HomePage;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@PageTitle("Rejestracja")
@Route(value = "register", layout = MainLayout.class)
@AnonymousAllowed
public class RegisterView extends VerticalLayout {

    private static final int MIN_PASSWORD_LENGTH = 6;

    private final UserService userService;

    public RegisterView(UserService userService) {
        this.userService = userService;

        addClassName("register-view");
        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        TextField usernameField = new TextField("Nazwa użytkownika");
        usernameField.setRequired(true);

        PasswordField passwordField = new PasswordField("Hasło");
        passwordField.setRequired(true);
        passwordField.setHelperText("Minimum " + MIN_PASSWORD_LENGTH + " znaków");

        PasswordField confirmPasswordField = new PasswordField("Powtórz hasło");
        confirmPasswordField.setRequired(true);

        FormLayout formLayout = new FormLayout(usernameField, passwordField, confirmPasswordField);
        formLayout.setMaxWidth("400px");

        Button registerButton = new Button("Zarejestruj się",
                e -> register(usernameField.getValue(), passwordField.getValue(), confirmPasswordField.getValue()));
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerButton.addClickShortcut(com.vaadin.flow.component.Key.ENTER);

        RouterLink loginLink = new RouterLink("Masz już konto? Zaloguj się", LoginView.class);

        VerticalLayout card = new VerticalLayout(new H2("Rejestracja"), formLayout, registerButton, loginLink);
        card.setAlignItems(FlexComponent.Alignment.CENTER);
        card.setMaxWidth("450px");

        add(card);
    }

    private void register(String username, String password, String confirmPassword) {
        if (username == null || username.trim().isEmpty()) {
            showError("Podaj nazwę użytkownika!");
            return;
        }
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            showError("Hasło musi mieć co najmniej " + MIN_PASSWORD_LENGTH + " znaków!");
            return;
        }
        if (!password.equals(confirmPassword)) {
            showError("Hasła nie są identyczne!");
            return;
        }

        try {
            User user = userService.register(username.trim(), password);
            MainLayout.setCurrentUser(user);
            Notification.show("Konto utworzone. Witaj, " + user.getUsername() + "!", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            UI.getCurrent().navigate(HomePage.class);
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void showError(String message) {
        Notification.show(message, 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
