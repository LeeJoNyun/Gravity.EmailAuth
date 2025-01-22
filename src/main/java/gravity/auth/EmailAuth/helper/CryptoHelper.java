package gravity.auth.EmailAuth.helper;

import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;


@Component
public class CryptoHelper {

    public String computeHmacSha256(String plainText, String secretKey) throws NoSuchAlgorithmException, InvalidKeyException {

        // HMAC-SHA256 계산
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);

        byte[] hashBytes = mac.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        // 해시 바이트를 16진수 문자열로 변환
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            hexString.append(String.format("%02x", b));
        }

        return hexString.toString();
    }
}
