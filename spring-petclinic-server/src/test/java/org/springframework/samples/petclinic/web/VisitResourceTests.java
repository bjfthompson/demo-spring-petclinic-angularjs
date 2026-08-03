package org.springframework.samples.petclinic.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VisitResource.class)
public class VisitResourceTests {

    @Autowired
    private MockMvc mvc;

    @MockBean
    ClinicService clinicService;

    @Test
    public void shouldReturnUpcomingVisitsForDefaultWindow() throws Exception {
        given(clinicService.findUpcomingVisits(7, null)).willReturn(setupUpcomingVisits());

        mvc.perform(get("/api/visits/upcoming").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].petName").value("Leo"))
                .andExpect(jsonPath("$[0].ownerName").value("George Franklin"))
                .andExpect(jsonPath("$[0].ownerId").value(1))
                .andExpect(jsonPath("$[0].petId").value(1))
                .andExpect(jsonPath("$[0].description").value("annual checkup"))
                .andExpect(jsonPath("$[1].petName").value("Samantha"));

        verify(clinicService).findUpcomingVisits(7, null);
    }

    @Test
    public void shouldFilterByTodayWindow() throws Exception {
        given(clinicService.findUpcomingVisits(0, null)).willReturn(Collections.singletonList(setupUpcomingVisits().get(0)));

        mvc.perform(get("/api/visits/upcoming").param("days", "0").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].petName").value("Leo"));

        verify(clinicService).findUpcomingVisits(0, null);
    }

    @Test
    public void shouldFilterByThirtyDayWindow() throws Exception {
        given(clinicService.findUpcomingVisits(30, null)).willReturn(setupUpcomingVisits());

        mvc.perform(get("/api/visits/upcoming").param("days", "30").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(clinicService).findUpcomingVisits(30, null);
    }

    @Test
    public void shouldSearchByPetOrOwnerName() throws Exception {
        given(clinicService.findUpcomingVisits(7, "cole")).willReturn(Collections.singletonList(setupUpcomingVisits().get(1)));

        mvc.perform(get("/api/visits/upcoming")
                        .param("days", "7")
                        .param("q", "cole")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].ownerName").value("Jean Coleman"))
                .andExpect(jsonPath("$[0].petName").value("Samantha"));

        verify(clinicService).findUpcomingVisits(eq(7), eq("cole"));
    }

    @Test
    public void shouldReturnEmptyListWhenNoVisitsMatch() throws Exception {
        given(clinicService.findUpcomingVisits(7, "zzz")).willReturn(Collections.emptyList());

        mvc.perform(get("/api/visits/upcoming")
                        .param("q", "zzz")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    public void shouldFallBackToSevenDaysForInvalidWindow() throws Exception {
        given(clinicService.findUpcomingVisits(7, null)).willReturn(Collections.emptyList());

        mvc.perform(get("/api/visits/upcoming").param("days", "14").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(clinicService).findUpcomingVisits(7, null);
    }

    private List<Visit> setupUpcomingVisits() {
        Owner george = new Owner();
        george.setId(1);
        george.setFirstName("George");
        george.setLastName("Franklin");

        Pet leo = new Pet();
        leo.setId(1);
        leo.setName("Leo");
        george.addPet(leo);

        Visit checkup = new Visit();
        checkup.setId(5);
        checkup.setDate(startOfDay(new Date()));
        checkup.setDescription("annual checkup");
        leo.addVisit(checkup);

        Owner jean = new Owner();
        jean.setId(6);
        jean.setFirstName("Jean");
        jean.setLastName("Coleman");

        Pet samantha = new Pet();
        samantha.setId(7);
        samantha.setName("Samantha");
        jean.addPet(samantha);

        Visit dental = new Visit();
        dental.setId(6);
        dental.setDate(addDays(startOfDay(new Date()), 2));
        dental.setDescription("dental cleaning");
        samantha.addVisit(dental);

        return Arrays.asList(checkup, dental);
    }

    private static Date startOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private static Date addDays(Date date, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, days);
        return calendar.getTime();
    }
}
