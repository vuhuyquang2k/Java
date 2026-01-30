package com.base.demo.dtos.user;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UpdateUserRequest {
    @Email
    private String email;

    @Size(min = 3, max = 100)
    private String fullName;

    @Size(min = 3, max = 50)
    private String username;

    @Size(min = 8, max = 100)
    private String password;
}
