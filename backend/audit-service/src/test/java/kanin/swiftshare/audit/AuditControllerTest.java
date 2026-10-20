package kanin.swiftshare.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import kanin.swiftshare.audit.model.AuditLogEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuditControllerTest
 * Verifies independent microservice persistence and REST endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class AuditControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/audit/events persists event and returns 201 Created")
    public void testRecordAuditEvent() throws Exception {
        AuditLogEntity event = new AuditLogEntity(
                "TEST_INTEGRATION_EVENT",
                101L,
                "Verified inter-service payload from core-service to audit-service",
                "core-service"
        );

        mockMvc.perform(post("/api/audit/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.eventType", is("TEST_INTEGRATION_EVENT")))
                .andExpect(jsonPath("$.transferId", is(101)))
                .andExpect(jsonPath("$.originService", is("core-service")));
    }

    @Test
    @DisplayName("GET /api/audit/events returns 200 OK and list of events")
    public void testGetAllAuditEvents() throws Exception {
        mockMvc.perform(get("/api/audit/events"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", isA(java.util.List.class)))
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(1)));
    }
}
