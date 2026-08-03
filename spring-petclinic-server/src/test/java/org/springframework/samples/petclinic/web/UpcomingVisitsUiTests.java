package org.springframework.samples.petclinic.web;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UI structure tests for the upcoming visits dashboard (no new frontend test framework).
 * Verifies the AngularJS template and navigation wiring that power the page.
 */
public class UpcomingVisitsUiTests {

    @Test
    public void upcomingVisitsTemplateShouldExposeFiltersSearchAndEmptyState() throws Exception {
        String template = readClientFile("src/scripts/upcoming-visits/upcoming-visits.template.html");

        assertThat(template).contains("Upcoming Visits");
        assertThat(template).contains("Today");
        assertThat(template).contains("Next 7 Days");
        assertThat(template).contains("Next 30 Days");
        assertThat(template).contains("Search by pet or owner");
        assertThat(template).contains("Nature of Visit");
        assertThat(template).contains("visit.petName");
        assertThat(template).contains("visit.ownerName");
        assertThat(template).contains("visit.description");
        assertThat(template).contains("visible-xs");
        assertThat(template).contains("ownerDetails({ ownerId: visit.ownerId })");
        assertThat(template).contains("No upcoming visits match the selected criteria.");
        assertThat(template).contains("table-responsive");
    }

    @Test
    public void navigationShouldLinkToUpcomingVisits() throws Exception {
        String nav = readClientFile("src/scripts/fragments/nav.html");

        assertThat(nav).contains("ui-sref=\"upcomingVisits\"");
        assertThat(nav).contains("Visits");
    }

    @Test
    public void appModuleShouldRegisterUpcomingVisits() throws Exception {
        String app = readClientFile("src/scripts/app.js");
        String route = readClientFile("src/scripts/upcoming-visits/upcoming-visits.js");
        String controller = readClientFile("src/scripts/upcoming-visits/upcoming-visits.controller.js");

        assertThat(app).contains("'upcomingVisits'");
        assertThat(route).contains("url: '/visits/upcoming'");
        assertThat(controller).contains("api/visits/upcoming");
        assertThat(controller).contains("params.q");
    }

    private static String readClientFile(String relativePath) throws Exception {
        Path path = resolveClientPath(relativePath);
        assertThat(path).exists();
        return Files.readString(path);
    }

    private static Path resolveClientPath(String relativePath) {
        Path fromModule = Paths.get("..", "spring-petclinic-client", relativePath).normalize();
        if (Files.exists(fromModule)) {
            return fromModule;
        }
        Path fromRoot = Paths.get("spring-petclinic-client", relativePath).normalize();
        if (Files.exists(fromRoot)) {
            return fromRoot;
        }
        return Paths.get("/workspace/spring-petclinic-client", relativePath);
    }
}
