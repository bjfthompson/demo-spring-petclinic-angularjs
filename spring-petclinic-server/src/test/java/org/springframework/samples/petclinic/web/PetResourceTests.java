package org.springframework.samples.petclinic.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collection;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PetResource.class)
public class PetResourceTests {

    @Autowired
    private MockMvc mvc;

    @MockBean
    ClinicService clinicService;

    @Test
    public void shouldGetAPetInJSonFormat() throws Exception {

        Pet pet = setupPet();

        given(clinicService.findPetById(2)).willReturn(pet);


        mvc.perform(get("/owners/2/pets/2").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("Basil"))
                .andExpect(jsonPath("$.type.id").value(6));
    }

    @Test
    public void shouldSearchPetsByExactName() throws Exception {
        given(clinicService.findAll()).willReturn(setupOwnersWithPets());

        mvc.perform(get("/api/pets/search").param("name", "Leo").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Leo"));
    }

    @Test
    public void shouldSearchPetsByPartialName() throws Exception {
        given(clinicService.findAll()).willReturn(setupOwnersWithPets());

        mvc.perform(get("/api/pets/search").param("name", "as").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Basil"));
    }

    @Test
    public void shouldSearchPetsCaseInsensitive() throws Exception {
        given(clinicService.findAll()).willReturn(setupOwnersWithPets());

        mvc.perform(get("/api/pets/search").param("name", "leo").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Leo"));
    }

    @Test
    public void shouldReturnEmptyListWhenNoPetsMatch() throws Exception {
        given(clinicService.findAll()).willReturn(setupOwnersWithPets());

        mvc.perform(get("/api/pets/search").param("name", "xyz").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    public void shouldReturnEmptyListWhenSearchNameIsEmpty() throws Exception {
        mvc.perform(get("/api/pets/search").param("name", "").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(clinicService, never()).findAll();
    }

    @Test
    public void shouldReturnEmptyListWhenSearchNameIsMissing() throws Exception {
        mvc.perform(get("/api/pets/search").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(clinicService, never()).findAll();
    }

    private Pet setupPet() {
        Owner owner = new Owner();
        owner.setFirstName("George");
        owner.setLastName("Bush");

        Pet pet = new Pet();

        pet.setName("Basil");
        pet.setId(2);

        PetType petType = new PetType();
        petType.setId(6);
        pet.setType(petType);

        owner.addPet(pet);
        return pet;
    }

    private Collection<Owner> setupOwnersWithPets() {
        Owner owner1 = new Owner();
        owner1.setFirstName("George");
        owner1.setLastName("Franklin");

        Pet leo = new Pet();
        leo.setId(1);
        leo.setName("Leo");
        PetType cat = new PetType();
        cat.setId(1);
        leo.setType(cat);
        owner1.addPet(leo);

        Owner owner2 = new Owner();
        owner2.setFirstName("Betty");
        owner2.setLastName("Davis");

        Pet basil = new Pet();
        basil.setId(2);
        basil.setName("Basil");
        PetType hamster = new PetType();
        hamster.setId(6);
        basil.setType(hamster);
        owner2.addPet(basil);

        return Arrays.asList(owner1, owner2);
    }
}
