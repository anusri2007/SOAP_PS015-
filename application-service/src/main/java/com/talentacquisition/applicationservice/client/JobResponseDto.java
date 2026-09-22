package com.talentacquisition.applicationservice.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class JobResponseDto {

    private Long id;
    private String title;
    private String description;
    private String status; // OPEN, CLOSED, INACTIVE, etc.
    private Long hrId;
    private String company;
    private String location;

    @JsonProperty("data")
    private void unpackNested(Map<String, Object> data) {
        if (data != null) {
            if (data.get("id") instanceof Number n) this.id = n.longValue();
            if (data.get("title") instanceof String s) this.title = s;
            if (data.get("description") instanceof String s) this.description = s;
            if (data.get("status") instanceof String s) this.status = s;
            if (data.get("hrId") instanceof Number n) this.hrId = n.longValue();
            if (data.get("company") instanceof String s) this.company = s;
            if (data.get("location") instanceof String s) this.location = s;
        }
    }
}

