package org.springframework.samples.petclinic.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class ClinicServiceUpcomingVisitsTests {

    @Autowired
    private ClinicService clinicService;

    @Test
    public void shouldReturnVisitsForNextSevenDays() {
        List<Visit> visits = clinicService.findUpcomingVisits(7, null);

        assertThat(visits).isNotEmpty();
        assertThat(visits).extracting(Visit::getDescription)
                .contains("annual checkup", "dental cleaning", "vaccination booster");
        assertThat(visits).extracting(Visit::getDescription)
                .doesNotContain("skin allergy follow-up", "wellness exam");
    }

    @Test
    public void shouldIncludeFartherVisitsInThirtyDayWindow() {
        List<Visit> visits = clinicService.findUpcomingVisits(30, null);

        assertThat(visits).extracting(Visit::getDescription)
                .contains("skin allergy follow-up", "wellness exam");
    }

    @Test
    public void shouldFilterByPetName() {
        List<Visit> visits = clinicService.findUpcomingVisits(30, "leo");

        assertThat(visits).isNotEmpty();
        assertThat(visits).allSatisfy(visit ->
                assertThat(visit.getPet().getName().toLowerCase()).contains("leo"));
    }

    @Test
    public void shouldFilterByOwnerName() {
        List<Visit> visits = clinicService.findUpcomingVisits(30, "Coleman");

        assertThat(visits).isNotEmpty();
        assertThat(visits).allSatisfy(visit ->
                assertThat(visit.getPet().getOwner().getLastName()).isEqualToIgnoringCase("Coleman"));
    }

    @Test
    public void shouldReturnEmptyWhenNameDoesNotMatch() {
        List<Visit> visits = clinicService.findUpcomingVisits(30, "no-such-name");

        assertThat(visits).isEmpty();
    }
}
