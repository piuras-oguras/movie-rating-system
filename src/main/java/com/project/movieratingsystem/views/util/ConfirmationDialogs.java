package com.project.movieratingsystem.views.util;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.List;
import java.util.function.Consumer;

/**
 * Builds the "are you sure?" confirmation dialog shared by the movie and review
 * management views. {@code onConfirm} receives the dialog itself so it can close it
 * only once the action it performs actually succeeds.
 */
public final class ConfirmationDialogs {

    private ConfirmationDialogs() {
    }

    public static void confirm(String title, List<String> messageLines, String confirmLabel, Consumer<Dialog> onConfirm) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(title);

        VerticalLayout content = new VerticalLayout();
        messageLines.forEach(line -> content.add(new Span(line)));
        dialog.add(content);

        Button confirmButton = new Button(confirmLabel, e -> onConfirm.accept(dialog));
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        Button cancelButton = new Button("Anuluj", e -> dialog.close());
        dialog.getFooter().add(cancelButton, confirmButton);

        dialog.open();
    }
}
