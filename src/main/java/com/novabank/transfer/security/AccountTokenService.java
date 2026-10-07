package com.novabank.transfer.security;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;

@Service
public class AccountTokenService {

    private final byte[] tokenKey;

    public AccountTokenService() {
        String secret = System.getenv("ACCOUNT_TOKEN_KEY");

        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("ACCOUNT_TOKEN_KEY is not configured");
        }

        tokenKey = secret.getBytes(StandardCharsets.UTF_8);

        if (tokenKey.length != 16 && tokenKey.length != 24 && tokenKey.length != 32) {
            throw new IllegalStateException(
                    "ACCOUNT_TOKEN_KEY must be 16, 24, or 32 bytes");
        }
    }

    public String tokenize(String accountNumber) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    new SecretKeySpec(tokenKey, "AES"));

            byte[] encrypted = cipher.doFinal(
                    accountNumber.getBytes(StandardCharsets.UTF_8));

            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(encrypted);

        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Could not create token", e);
        }
    }
}
