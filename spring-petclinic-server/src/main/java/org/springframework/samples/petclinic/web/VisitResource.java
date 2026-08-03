/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.web;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 */
@RestController
public class VisitResource extends AbstractResourceController {

    private final ClinicService clinicService;

    @Autowired
    public VisitResource(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    @PostMapping("/owners/{ownerId}/pets/{petId}/visits")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void create(
            @Valid @RequestBody Visit visit,
            @PathVariable("petId") int petId) {

        clinicService.findPetById(petId).addVisit(visit);
        clinicService.saveVisit(visit);
    }

    @GetMapping("/owners/{ownerId}/pets/{petId}/visits")
    public Object visits(@PathVariable("petId") int petId) {
        return clinicService.findPetById(petId).getVisits();
    }

    /**
     * Upcoming visits dashboard API.
     * {@code days}: 0 = today only, 7 = today through today+7, 30 = today through today+30.
     * Optional {@code q} filters by pet or owner name.
     */
    @GetMapping("/api/visits/upcoming")
    public List<UpcomingVisitDetails> upcomingVisits(
            @RequestParam(value = "days", defaultValue = "7") int days,
            @RequestParam(value = "q", required = false) String q) {

        int window = (days == 0 || days == 7 || days == 30) ? days : 7;
        List<UpcomingVisitDetails> results = new ArrayList<>();
        for (Visit visit : clinicService.findUpcomingVisits(window, q)) {
            results.add(new UpcomingVisitDetails(visit));
        }
        return results;
    }

    static class UpcomingVisitDetails {

        final int id;
        @JsonFormat(pattern = "yyyy-MM-dd")
        final Date date;
        final String description;
        final String petName;
        final int petId;
        final String ownerName;
        final int ownerId;

        UpcomingVisitDetails(Visit visit) {
            this.id = visit.getId();
            this.date = visit.getDate();
            this.description = visit.getDescription();
            Pet pet = visit.getPet();
            this.petName = pet.getName();
            this.petId = pet.getId();
            Owner owner = pet.getOwner();
            this.ownerName = owner.getFirstName() + " " + owner.getLastName();
            this.ownerId = owner.getId();
        }

        public int getId() {
            return id;
        }

        public Date getDate() {
            return date;
        }

        public String getDescription() {
            return description;
        }

        public String getPetName() {
            return petName;
        }

        public int getPetId() {
            return petId;
        }

        public String getOwnerName() {
            return ownerName;
        }

        public int getOwnerId() {
            return ownerId;
        }
    }
}
