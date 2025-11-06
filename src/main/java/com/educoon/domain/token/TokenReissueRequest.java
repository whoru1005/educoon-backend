package com.educoon.domain.token;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TokenReissueRequest {

    @NotEmpty(message = "Refresh Token은 필수입니다")
    private String refreshToken;
}
