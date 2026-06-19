package cl.smartlogix.auth.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import cl.smartlogix.auth.domain.UserAccount;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final String issuer;
    private final long accessTokenMinutes;

    public JwtService(
            JwtEncoder jwtEncoder,
            @Value("${security.jwt.issuer}") String issuer,
            @Value("${security.jwt.access-token-minutes:30}") long accessTokenMinutes
    ) {
        this.jwtEncoder = jwtEncoder;
        this.issuer = issuer;
        this.accessTokenMinutes = accessTokenMinutes;
    }

    public String generateAccessToken(UserAccount user) {
        Instant now = Instant.now();
        Instant expiration = now.plus(accessTokenMinutes, ChronoUnit.MINUTES);

        String scopes = "ADMIN".equalsIgnoreCase(user.getRole())
                ? "read:inventory write:inventory read:orders write:orders read:shipments write:shipments read:users write:users"
                : "read:inventory read:orders read:shipments";

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .id(UUID.randomUUID().toString())
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(expiration)
                .subject(user.getEmail())
                .claim("name", user.getName())
                .claim("role", user.getRole())
                .claim("scope", scopes)
                .audience(java.util.List.of("bff"))
                .build();

        JwsHeader jwsHeader = JwsHeader.with(SignatureAlgorithm.RS256)
                .keyId("auth-key-1")
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
    }

    public long getAccessTokenTtlSeconds() {
        return accessTokenMinutes * 60;
    }
}
