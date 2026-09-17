package com.project.movieratingsystem;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
@PageTitle("Filmy")
@Theme(value = "netrating")
public class MovieRatingSystemApplication implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(MovieRatingSystemApplication.class, args);
    }

}
