package io.goorm.route33.service;

import io.goorm.route33.exception.CustomException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Jwts;
import javax.crypto.SecretKey;

@Component
public class TokenService {
    private static final String USER_ID_CLAIM = "userId";

    private final SecretKey secretKey;
    private final long accessTokenExpirationMillis;
    private final long refreshTokenExpirationMillis;
    private final UserRepository userRepository;

    public TokenService(TokenProperty tokenProperty, UserRepository userRepository){


        byte[] keyBytes = Decoders.BASE64.decode(tokenProperty.secretKey());
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
//        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(tokenProperty.secretKey()));
        this.accessTokenExpirationMillis = tokenProperty.accessTokenExpirationMillis();
        this.refreshTokenExpirationMillis = tokenProperty.refreshTokenExpirationMillis();
        this.userRepository = userRepository;
    }

    public Long extractUserId(String token){
        try{
            return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get(USER_ID_CLAIM, Long.class);
        }catch (Exception e){
            throw new CustomException("유효하지 않은 Token입니다.", HttpStatus.UNAUTHORIZED);
        }
    }

}
