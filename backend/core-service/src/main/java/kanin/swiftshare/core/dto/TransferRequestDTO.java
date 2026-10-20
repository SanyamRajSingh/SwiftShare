package kanin.swiftshare.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * TransferRequestDTO
 * Demonstrates Unit 4 Syllabus: Jakarta Validation annotations (@NotBlank, @NotNull, @Positive).
 */
public class TransferRequestDTO {

    @NotBlank(message = "File name must not be blank")
    @Size(max = 255, message = "File name cannot exceed 255 characters")
    private String fileName;

    private String sender;
    private String receiver;

    @NotNull(message = "File size is required")
    @Positive(message = "File size must be greater than 0 bytes")
    private Long sizeBytes;

    private String status;

    public TransferRequestDTO() {}

    public TransferRequestDTO(String fileName, String sender, String receiver, Long sizeBytes, String status) {
        this.fileName = fileName;
        this.sender = sender;
        this.receiver = receiver;
        this.sizeBytes = sizeBytes;
        this.status = status;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public Long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(Long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
