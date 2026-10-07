package com.novabank.transfer.security;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;

@Service
public class ReceiptSigner {

    private final String signingKey;

    public ReceiptSigner() {
        signingKey = System.getenv("RECEIPT_SIGNING_KEY");

        if (signingKey == null || signingKey.isBlank()) {
            throw new IllegalStateException("RECEIPT_SIGNING_KEY is not configured");
        }
    }

    public String sign(String reference, String fromAccount) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                    signingKey.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"));

            byte[] signature = mac.doFinal(
                    (reference + "|" + fromAccount)
                            .getBytes(StandardCharsets.UTF_8));

            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(signature);

        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Could not sign receipt", e);
        }
    }
}
