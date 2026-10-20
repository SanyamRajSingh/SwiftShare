package kanin.swiftshare.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import kanin.swiftshare.core.dto.TransferRequestDTO;
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
 * TransferControllerTest
 * Demonstrates Unit 4 Syllabus: Automated MockMvc testing, HTTP status code validation,
 * request serialization, and Bean Validation verification.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/transfers returns 200 OK and populated list")
    public void testGetAllTransfers() throws Exception {
        mockMvc.perform(get("/api/transfers"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", isA(java.util.List.class)));
    }

    @Test
    @DisplayName("POST /api/transfers with valid payload returns 201 Created")
    public void testCreateTransferSuccess() throws Exception {
        TransferRequestDTO validRequest = new TransferRequestDTO(
                "unit4_test_file.bin",
                "127.0.0.1:8080",
                "127.0.0.1:52140",
                65536L,
                "Success"
        );

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("X-Audit-Status"))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.fileName", is("unit4_test_file.bin")))
                .andExpect(jsonPath("$.sizeBytes", is(65536)))
                .andExpect(jsonPath("$.status", is("Success")))
                .andExpect(jsonPath("$.auditStatus", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/transfers with blank filename returns 400 Bad Request")
    public void testCreateTransferValidationFailure() throws Exception {
        TransferRequestDTO invalidRequest = new TransferRequestDTO(
                "", // Blank: triggers @NotBlank
                "127.0.0.1",
                "127.0.0.1",
                -500L, // Negative: triggers @Positive
                "Success"
        );

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Failed")))
                .andExpect(jsonPath("$.errors.fileName", notNullValue()))
                .andExpect(jsonPath("$.errors.sizeBytes", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/transfers/{id} with invalid ID returns 404 Not Found")
    public void testGetTransferNotFound() throws Exception {
        mockMvc.perform(get("/api/transfers/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Not Found")));
    }
}
