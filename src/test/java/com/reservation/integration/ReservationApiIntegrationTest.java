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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestMailConfig.class)
class ReservationApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fullReservationFlowWithCancelRestoresAvailability() throws Exception {
        LocalDate date = LocalDate.now().plusDays(10);

        MvcResult placeResult = mockMvc.perform(post("/api/places")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Trattoria Test",
                                  "city": "Warszawa",
                                  "street": "Testowa",
                                  "streetNumber": 1,
                                  "postalCode": "00-001",
                                  "postOffice": "Warszawa"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        int placeId = objectMapper.readTree(placeResult.getResponse().getContentAsString()).get("id").asInt();

        mockMvc.perform(post("/api/places/{id}/setup", placeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "year": %d,
                                  "open": true,
                                  "defaultOpenFrom": "12:00:00",
                                  "defaultOpenTo": "15:00:00",
                                  "tables": [
                                    {"number": 1, "seatsCount": 4, "active": true},
                                    {"number": 2, "seatsCount": 2, "active": true}
                                  ]
                                }
                                """.formatted(date.getYear())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tablesCount").value(2))
                .andExpect(jsonPath("$.daysCount").value(
                        date.isLeapYear() ? 366 : 365));

        MvcResult reservationResult = mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ordererName": "Anna",
                                  "ordererSurname": "Nowak",
                                  "placeId": %d,
                                  "email": "anna.int@example.com",
                                  "phoneNumber": "+48123123123",
                                  "date": "%s",
                                  "startTime": "13:00:00",
                                  "durationMinutes": 60,
                                  "peopleCount": 2,
                                  "locale": "en"
                                }
                                """.formatted(placeId, date)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reservationNumber").exists())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.locale").value("en"))
                .andReturn();

        JsonNode reservation = objectMapper.readTree(reservationResult.getResponse().getContentAsString());
        String code = reservation.get("reservationNumber").asText();
        assertThat(code).startsWith("RES-");

        MvcResult scheduleBefore = mockMvc.perform(get("/api/places/{id}/schedule", placeId)
                        .param("date", date.toString()))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode beforeSlots = objectMapper.readTree(scheduleBefore.getResponse().getContentAsString());
        JsonNode slot13 = findHour(beforeSlots, "13:00:00");
        assertThat(slot13.get("reservedTables").asInt()).isEqualTo(1);
        assertThat(slot13.get("availableTables").asInt()).isEqualTo(1);

        mockMvc.perform(get("/api/places/{id}/analytics", placeId).param("date", date.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reservedTables").value(1));

        mockMvc.perform(post("/api/reservations/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reservationNumber": "%s",
                                  "reason": "integration test",
                                  "locale": "en"
                                }
                                """.formatted(code)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("cancelled")));

        MvcResult scheduleAfter = mockMvc.perform(get("/api/places/{id}/schedule", placeId)
                        .param("date", date.toString()))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode afterSlots = objectMapper.readTree(scheduleAfter.getResponse().getContentAsString());
        JsonNode slotAfter = findHour(afterSlots, "13:00:00");
        assertThat(slotAfter.get("reservedTables").asInt()).isEqualTo(0);
        assertThat(slotAfter.get("availableTables").asInt()).isEqualTo(2);
    }

    @Test
    void rejectsInvalidReservationPayload() throws Exception {
        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ordererName": "",
                                  "email": "bad"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.placeId").exists());
    }

    private JsonNode findHour(JsonNode slots, String hour) {
        for (JsonNode slot : slots) {
            if (hour.equals(slot.get("hour").asText())) {
                return slot;
            }
        }
        throw new AssertionError("Slot not found for hour " + hour);
    }
}
