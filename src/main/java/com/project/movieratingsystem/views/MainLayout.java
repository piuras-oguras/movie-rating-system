package com.project.movieratingsystem.views;

import com.project.movieratingsystem.views.moviemanagement.MovieManagementView;
import com.project.movieratingsystem.views.auth.LoginView;
import com.project.movieratingsystem.views.auth.RegisterView;
import com.project.movieratingsystem.views.homepage.HomePage;
import com.project.movieratingsystem.views.reviews.ReviewsView;
import com.project.movieratingsystem.views.suggestedmovie.SuggestedMovieView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility;

@AnonymousAllowed
public class MainLayout extends AppLayout {

    private static final String SESSION_USERNAME_ATTRIBUTE = "userName";

    private Span userStatusLabel;
    private Button loginButton;
    private Button registerButton;
    private Button logoutButton;

    public MainLayout() {
        addToNavbar(createHeaderContent());
        addToDrawer(createDrawerContent());
    }

    private Component createHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();

        Image logo = new Image("images/logo.png", "Logo aplikacji Filmy");

        logo.setHeight("44px");

        HorizontalLayout header = new HorizontalLayout(toggle, logo);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.addClassNames(LumoUtility.Padding.Vertical.NONE, LumoUtility.Padding.Horizontal.MEDIUM);

        header.expand(logo);

        return header;
    }

    private Component createDrawerContent() {
        SideNav mainNav = new SideNav();
        mainNav.setLabel("Menu");
        mainNav.addItem(new SideNavItem("Strona główna", HomePage.class, VaadinIcon.HOME_O.create()));
        mainNav.addItem(new SideNavItem("Zasugeruj film", SuggestedMovieView.class, VaadinIcon.FILM.create()));
        mainNav.addItem(new SideNavItem("Recenzje", ReviewsView.class, VaadinIcon.FILE_O.create()));
        mainNav.addItem(new SideNavItem("Zarządzanie filmami", MovieManagementView.class, VaadinIcon.EDIT.create()));

        userStatusLabel = new Span();
        userStatusLabel.addClassNames(LumoUtility.FontWeight.SEMIBOLD);

        loginButton = new Button("Zaloguj", e -> UI.getCurrent().navigate(LoginView.class));
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        registerButton = new Button("Zarejestruj się", e -> UI.getCurrent().navigate(RegisterView.class));
        registerButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        logoutButton = new Button("Wyloguj", e -> logout());

        VerticalLayout authLayout = new VerticalLayout(userStatusLabel, loginButton, registerButton, logoutButton);
        authLayout.setPadding(false);
        authLayout.setSpacing(true);

        VerticalLayout navigationLayout = new VerticalLayout(mainNav, authLayout);
        navigationLayout.setPadding(false);
        navigationLayout.setSpacing(false);

        VerticalLayout drawerContent = new VerticalLayout(navigationLayout);
        drawerContent.setPadding(false);
        drawerContent.setSpacing(false);
        drawerContent.getThemeList().set("spacing-s", true);
        drawerContent.expand(navigationLayout);
        drawerContent.addClassName("menu-content");

        updateAuthSection();

        return drawerContent;
    }

    @Override
    public void showRouterLayoutContent(HasElement content) {
        super.showRouterLayoutContent(content);
        updateAuthSection();
    }

    private void updateAuthSection() {
        if (userStatusLabel == null) {
            return;
        }
        String currentUserName = getCurrentUserName();
        boolean loggedIn = currentUserName != null;

        userStatusLabel.setText(loggedIn ? "Zalogowano jako: " + currentUserName : "");
        userStatusLabel.setVisible(loggedIn);
        loginButton.setVisible(!loggedIn);
        registerButton.setVisible(!loggedIn);
        logoutButton.setVisible(loggedIn);
    }

    private void logout() {
        setCurrentUserName(null);
        updateAuthSection();
        Notification.show("Wylogowano", 3000, Notification.Position.TOP_CENTER);
        UI.getCurrent().navigate(HomePage.class);
    }

    public static String getCurrentUserName() {
        VaadinSession session = VaadinSession.getCurrent();
        return session != null ? (String) session.getAttribute(SESSION_USERNAME_ATTRIBUTE) : null;
    }

    public static void setCurrentUserName(String userName) {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            session.setAttribute(SESSION_USERNAME_ATTRIBUTE, userName);
        }
    }
}
