package com.project.movieratingsystem.views.auth;

import com.project.movieratingsystem.model.User;
import com.project.movieratingsystem.services.UserService;
import com.project.movieratingsystem.views.MainLayout;
import com.project.movieratingsystem.views.homepage.HomePage;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.util.Optional;

@PageTitle("Logowanie")
@Route(value = "login", layout = MainLayout.class)
@AnonymousAllowed
public class LoginView extends VerticalLayout {

    private final UserService userService;
    private final LoginForm loginForm = new LoginForm();

    public LoginView(UserService userService) {
        this.userService = userService;

        addClassName("login-view");
        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        loginForm.setI18n(createI18n());
        loginForm.setForgotPasswordButtonVisible(false);
        loginForm.addLoginListener(event -> attemptLogin(event.getUsername(), event.getPassword()));

        RouterLink registerLink = new RouterLink("Nie masz konta? Zarejestruj się", RegisterView.class);

        VerticalLayout card = new VerticalLayout(new H2("Logowanie"), loginForm, registerLink);
        card.setAlignItems(FlexComponent.Alignment.CENTER);
        card.setMaxWidth("400px");

        add(card);
    }

    private void attemptLogin(String username, String password) {
        Optional<User> user = userService.authenticate(username, password);
        if (user.isPresent()) {
            MainLayout.setCurrentUserName(user.get().getUsername());
            Notification.show("Zalogowano jako: " + user.get().getUsername(), 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            UI.getCurrent().navigate(HomePage.class);
        } else {
            loginForm.setError(true);
        }
    }

    private LoginI18n createI18n() {
        LoginI18n i18n = LoginI18n.createDefault();
        i18n.getForm().setTitle("Logowanie");
        i18n.getForm().setUsername("Nazwa użytkownika");
        i18n.getForm().setPassword("Hasło");
        i18n.getForm().setSubmit("Zaloguj");
        i18n.getErrorMessage().setTitle("Błędna nazwa użytkownika lub hasło");
        i18n.getErrorMessage().setMessage("Sprawdź poprawność danych i spróbuj ponownie.");
        return i18n;
    }
}
