package gravity.auth.EmailAuth.controller;

import gravity.auth.EmailAuth.dto.JsonResult;
import gravity.auth.EmailAuth.dto.TokenInputModel;
import gravity.auth.EmailAuth.helper.CryptoHelper;
import gravity.auth.EmailAuth.helper.JsonHelper;
import gravity.auth.EmailAuth.service.EmailAuthService;
import gravity.auth.EmailAuth.service.GameCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@EnableAsync
public class AuthController {
    private final CryptoHelper _helper;
    private final GameCodeService _gameCodeService;
    private final JsonHelper _jsonHelper;
    private final EmailAuthService _emailAuthService;

    @Value("${appOption.value.key}")
    private String KEY;
    @Value("${appOption.value.sendUrl}")
    private String URL;

    @PostMapping
    @Async
    public CompletableFuture<JsonResult> setEmailTokenAuthCheck(@RequestBody TokenInputModel model) throws Exception {
        JsonResult json  = new JsonResult();
        String tokenString = FromUrlSafeBase64(model.Token);
        String decStr = _helper.decryptAes256(tokenString, KEY, null);
        String timestamp = decStr.split("\\|")[0];
        String email = decStr.split("\\|")[1];
        String gameCode = decStr.split("\\|")[2];
        String secretKey = _gameCodeService.getSercretKey(gameCode);
        String encStr = ComputeHmacSha256(timestamp, email, secretKey).toUpperCase();

        if(IsExpired(timestamp)){
            json.setErrorCode(-3001);
            json.setMessage(_jsonHelper.convertToMessage(-3001));
            return CompletableFuture.completedFuture(json);
        }

        int result = _emailAuthService.authEmailAsync(encStr);

        if(result != 0){
            json.setMessage(_jsonHelper.convertToMessage(result));
            json.setErrorCode(result);
            return CompletableFuture.completedFuture(json);
        }

        json.setMessage(_jsonHelper.convertToMessage(0));
        json.setErrorCode(0);
        return CompletableFuture.completedFuture(json);
    }

    // region 내부 함수
    private String ComputeHmacSha256(String timestamp, String email, String secretKey) throws NoSuchAlgorithmException, InvalidKeyException {
        String plainText = timestamp + "|" + email;

        // HMAC-SHA256 해시 생성
        Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        hmacSHA256.init(secretKeySpec);

        byte[] hashBytes = hmacSHA256.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        // 바이트 배열을 16진수 문자열로 변환
        StringBuilder hashHex = new StringBuilder();
        for (byte b : hashBytes) {
            hashHex.append(String.format("%02x", b));
        }

        return hashHex.toString();
    }

    private String FromUrlSafeBase64(String token){
        String base64 = token
                .replace("-", "+")
                .replace("_", "/");

        // 패딩 문자(`=`) 추가
        switch (token.length() % 4) {
            case 2:
                base64 += "==";
                break;
            case 3:
                base64 += "=";
                break;
        }

        return base64;
    }

    private boolean IsExpired(String timestamp){
        long time = Long.parseLong(timestamp) + 3600; // 1시간 추가
        long now = Instant.now().getEpochSecond(); // 현재 UTC Unix 타임스탬프

        return time <= now;
    }
    // endregion
}
