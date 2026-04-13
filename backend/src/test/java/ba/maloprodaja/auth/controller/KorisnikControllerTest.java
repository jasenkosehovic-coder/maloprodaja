package ba.maloprodaja.auth.controller;

import ba.maloprodaja.auth.dto.KorisnikDTO;
import ba.maloprodaja.auth.entity.Korisnik;
import ba.maloprodaja.auth.entity.KorisnikUloga;
import ba.maloprodaja.auth.service.IKorisnikService;
import ba.maloprodaja.common.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(KorisnikController.class)
@ActiveProfiles("test")
class KorisnikControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IKorisnikService korisnikService;

    private Korisnik adminKorisnik() {
        Korisnik k = new Korisnik();
        k.setUsername("admin");
        k.setPasswordHash("hash");
        k.setUloga(KorisnikUloga.ADMIN);
        k.setAktivan(true);
        k.setIdKompanije(1L);
        return k;
    }

    @Test
    void listAll_asAdmin_returns200() throws Exception {
        var item = new KorisnikDTO.KorisnikListItemDTO(1L, "testuser", "Test", "User",
                "test@test.ba", KorisnikUloga.BLAGAJNIK, null, true, null);
        when(korisnikService.listAll(any())).thenReturn(List.of(item));

        mockMvc.perform(get("/api/korisnici")
                        .with(user(adminKorisnik())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].username").value("testuser"));
    }

    @Test
    @WithMockUser(roles = "BLAGAJNIK")
    void listAll_asBlagajnik_returns403() throws Exception {
        mockMvc.perform(get("/api/korisnici"))
                .andExpect(status().isForbidden());
    }

    @Test
    void create_validData_returns201() throws Exception {
        var created = new KorisnikDTO.KorisnikListItemDTO(2L, "novuser", "Novi", "User",
                "novi@test.ba", KorisnikUloga.BLAGAJNIK, null, true, null);
        when(korisnikService.create(any(), any())).thenReturn(created);

        var dto = new KorisnikDTO.CreateKorisnikDTO("novuser", "pass123", "Novi", "User",
                "novi@test.ba", KorisnikUloga.BLAGAJNIK, null);

        mockMvc.perform(post("/api/korisnici")
                        .with(user(adminKorisnik()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.username").value("novuser"));
    }

    @Test
    void create_blankUsername_returns400() throws Exception {
        var dto = new KorisnikDTO.CreateKorisnikDTO("", "pass123", "Novi", "User",
                "novi@test.ba", KorisnikUloga.BLAGAJNIK, null);

        mockMvc.perform(post("/api/korisnici")
                        .with(user(adminKorisnik()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deactivate_nonExistingId_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Korisnik", 99L))
                .when(korisnikService).deactivate(99L);

        mockMvc.perform(delete("/api/korisnici/99")
                        .with(user(adminKorisnik()))
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getIzbornici_returnsIzbornici() throws Exception {
        var izbornik = new KorisnikDTO.KorisnikIzbornikaDTO("PRODAJA", true);
        when(korisnikService.getIzbornici(1L)).thenReturn(List.of(izbornik));

        mockMvc.perform(get("/api/korisnici/1/izbornici")
                        .with(user(adminKorisnik())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].izbornikKljuc").value("PRODAJA"));
    }

    @Test
    void updateIzbornici_validData_returns200() throws Exception {
        var izbornici = List.of(new KorisnikDTO.KorisnikIzbornikaDTO("PRODAJA", true));
        when(korisnikService.updateIzbornici(eq(1L), any())).thenReturn(izbornici);

        mockMvc.perform(put("/api/korisnici/1/izbornici")
                        .with(user(adminKorisnik()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(izbornici)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].izbornikKljuc").value("PRODAJA"));
    }
}
