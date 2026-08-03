package org.springframework.samples.petclinic.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class VisitRepositoryTests {

    @Autowired
    private VisitRepository visitRepository;

    @Test
    public void shouldFindUpcomingVisitsOrderedByDate() {
        Date start = startOfDay(new Date());
        Date end = addDays(start, 30);

        List<Visit> visits = visitRepository.findByDateBetween(start, end);

        assertThat(visits).isNotEmpty();
        assertThat(visits).allSatisfy(visit -> {
            assertThat(visit.getDate()).isAfterOrEqualTo(start);
            assertThat(visit.getDate()).isBeforeOrEqualTo(end);
            assertThat(visit.getPet()).isNotNull();
            assertThat(visit.getPet().getOwner()).isNotNull();
            assertThat(visit.getPet().getName()).isNotBlank();
        });

        for (int i = 1; i < visits.size(); i++) {
            assertThat(visits.get(i - 1).getDate()).isBeforeOrEqualTo(visits.get(i).getDate());
        }
    }

    @Test
    public void shouldFindOnlyTodaysVisitsInZeroDayWindow() {
        Date today = startOfDay(new Date());
        List<Visit> visits = visitRepository.findByDateBetween(today, today);

        assertThat(visits).isNotEmpty();
        assertThat(visits).allSatisfy(visit ->
                assertThat(startOfDay(visit.getDate())).isEqualTo(today));
    }

    @Test
    public void shouldReturnEmptyWhenNoVisitsInFarFutureRange() {
        Date start = addDays(startOfDay(new Date()), 365);
        Date end = addDays(start, 7);

        List<Visit> visits = visitRepository.findByDateBetween(start, end);

        assertThat(visits).isEmpty();
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
