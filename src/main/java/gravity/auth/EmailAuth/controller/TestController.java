package gravity.auth.EmailAuth.controller;

import gravity.auth.EmailAuth.helper.CryptoHelper;
import gravity.auth.EmailAuth.dto.TestInputModel;
import gravity.auth.EmailAuth.dto.TestOutputModel;
import gravity.auth.EmailAuth.service.GameCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.*;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@EnableAsync
public class TestController {

    private final GameCodeService _service;
    private final CryptoHelper _helper;

    @Async
    @PostMapping("/makeToken")
    public CompletableFuture<TestOutputModel> setToken(@RequestBody TestInputModel model) throws NoSuchAlgorithmException, InvalidKeyException {
        String secretKey = _service.getSercretKey(model.getGameCode());

        long now = Instant.now().getEpochSecond();
        String plainText = now + "|" + model.getEmail();

        String encStr = _helper.computeHmacSha256(plainText, secretKey);

        TestOutputModel outputmodel = new TestOutputModel();
        outputmodel.setEmail(model.getEmail());
        outputmodel.setGameCode(model.getGameCode());

        String sign = String.format(
                "%s%s%s",
                String.valueOf(now).substring(0, 5), // 첫 5자리
                encStr,                              // HMAC 해시 값
                String.valueOf(now).substring(5, 10) // 마지막 5자리
        );

        outputmodel.setSign(sign);

        return CompletableFuture.completedFuture(outputmodel);
    }

}
