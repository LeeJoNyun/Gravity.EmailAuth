package gravity.auth.EmailAuth.helper;

import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;


@Component
public class CryptoHelper {

    //SHA256 암호화
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

    // AES256 암호화
    public String encryptAes256(String plainText, String keyString, String ivString) throws Exception {
        // keyString을 바이트 배열로 변환
        byte[] key = keyString.getBytes(StandardCharsets.UTF_8);

        // ivString을 바이트 배열로 변환
        byte[] iv = new byte[16];  // AES 블록 크기(16바이트)

        if (ivString != null && !ivString.isEmpty()) {
            iv = ivString.getBytes(StandardCharsets.UTF_8);
        }

        // Cipher 객체 생성 및 설정
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        // 키와 IV를 이용해 암호화 설정
        cipher.init(Cipher.ENCRYPT_MODE, new javax.crypto.spec.SecretKeySpec(key, "AES"), ivSpec);

        // 평문 암호화
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        // 암호화된 데이터를 Base64로 인코딩해서 반환
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    // AES256 복호화
    public String decryptAes256(String encryptedText, String keyString, String ivString) throws Exception {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return null;
        }

        // keyString을 바이트 배열로 변환
        byte[] key = keyString.getBytes(StandardCharsets.UTF_8);

        // ivString을 바이트 배열로 변환 (16바이트로 맞춰서 설정)
        byte[] iv = new byte[16];  // AES 블록 크기(16바이트)
        if (ivString != null && !ivString.isEmpty()) {
            iv = ivString.getBytes(StandardCharsets.UTF_8);
        }

        // Cipher 객체 생성 및 설정
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        // 키와 IV를 이용해 복호화 설정
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), ivSpec);

        // Base64로 인코딩된 암호화된 텍스트를 디코딩하여 바이트 배열로 변환
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedText);

        // 복호화 수행
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

        // 복호화된 바이트 배열을 문자열로 변환하여 반환
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
}
