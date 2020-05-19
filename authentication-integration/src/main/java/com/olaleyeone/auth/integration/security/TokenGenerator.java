package com.olaleyeone.auth.integration.security;

import com.olaleyeone.auth.data.entity.RefreshToken;
import com.olaleyeone.auth.dto.JwtDto;

public interface TokenGenerator {

    JwtDto generateJwt(RefreshToken refreshToken);
}