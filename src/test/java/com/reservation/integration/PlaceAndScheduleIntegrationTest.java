package com.reservation.integration;

import com.reservation.config.TestMailConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestMailConfig.class)
class PlaceAndScheduleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void canLockScheduleSlotAvailability() throws Exception {
        LocalDate date = LocalDate.now().plusDays(3);
        int year = date.getYear();

        MvcResult placeResult = mockMvc.perform(post("/api/places")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Lock Cafe",
                                  "city": "Krakow",
                                  "street": "Florianska",
                                  "streetNumber": 7,
                                  "postalCode": "31-019",
                                  "postOffice": "Krakow"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        int placeId = objectMapper.readTree(placeResult.getResponse().getContentAsString()).get("id").asInt();

        mockMvc.perform(post("/api/places/{id}/setup", placeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "year": %d,
                                  "open": true,
                                  "defaultOpenFrom": "10:00:00",
                                  "defaultOpenTo": "12:00:00",
                                  "tables": [{"number": 1, "seatsCount": 4, "active": true}]
                                }
                                """.formatted(year)))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/places/{id}/schedule/availability", placeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "date": "%s",
                                  "hour": "10:00:00",
                                  "locked": true
                                }
                                """.formatted(date)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.locked").value(true));

        MvcResult schedule = mockMvc.perform(get("/api/places/{id}/schedule", placeId)
                        .param("date", date.toString()))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode slots = objectMapper.readTree(schedule.getResponse().getContentAsString());
        assertThat(slots).isNotEmpty();
        boolean locked = false;
        for (JsonNode slot : slots) {
            if ("10:00:00".equals(slot.get("hour").asText())) {
                locked = slot.get("locked").asBoolean();
            }
        }
        assertThat(locked).isTrue();
    }

    @Test
    void frontendPagesAreServed() throws Exception {
        mockMvc.perform(get("/restaurant/")).andExpect(status().isOk());
        mockMvc.perform(get("/booking/")).andExpect(status().isOk());
    }
}
