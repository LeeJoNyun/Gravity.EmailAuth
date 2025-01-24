package gravity.auth.EmailAuth.controller;

import com.fasterxml.jackson.databind.util.JSONPObject;
import gravity.auth.EmailAuth.dto.CallSendEmailInputModel;
import gravity.auth.EmailAuth.dto.EmailInputModel;
import gravity.auth.EmailAuth.dto.JsonResult;
import gravity.auth.EmailAuth.dto.TestOutputModel;
import gravity.auth.EmailAuth.entity.EmailList;
import gravity.auth.EmailAuth.helper.CryptoHelper;
import gravity.auth.EmailAuth.service.EmailListService;
import gravity.auth.EmailAuth.service.GameCodeService;
import io.swagger.v3.core.util.Json;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequiredArgsConstructor
@EnableAsync
@RequestMapping("/api/sendApi")
public class EmailAuthController {

    private final CryptoHelper _helper;
    private final EmailListService _emailService;
    private final GameCodeService _gameCodeService;

    @Async
    @PostMapping
    public CompletableFuture<JsonResult> setPostAsync(@RequestBody TestOutputModel model) throws Exception {
        String sign = model.getSign();
        String email = model.getEmail();
        String gameCode = model.getGameCode();

        // 유효 sign 확인
        ValidateSignHeader(sign);

        String encStr = getEncrytString(sign);

        // 동일 토큰 송신 처리
        DuplicatedToken(sign);

        EmailInputModel input = new EmailInputModel();
        input.setEmail(model.getEmail());
        input.setGameCode(model.getGameCode());
        input.setToken(encStr);

        JsonResult result = new JsonResult();

        // 회원정보 DB 저장
        int resultCode = _emailService.sendEmail(input);
        String resultMessage = "";
        if(resultCode != 0){
            result.setMessage("DB 저장 에러");
            result.setErrorCode(resultCode);
            return CompletableFuture.completedFuture(result);
        }
        // secretKey 조회
        String secretKey = _gameCodeService.getSercretKey(gameCode);

        String plainText = getPlainText(email, sign, gameCode, secretKey);
        if(plainText.contains("/") || plainText.contains("+") || plainText.contains("=")){
            plainText = toUrlSafeBase64(plainText);
        }
        CallSendEmailInputModel inputModel = new CallSendEmailInputModel();
        inputModel.setEmail(email);
        inputModel.setPlainText(plainText);

        try{
            // 외부 api 호출
            SendEmail(inputModel);
        }catch( Exception e ){
            result.setErrorCode(-999);
            result.setMessage(e.getMessage());
        }

        result.setMessage(resultMessage);
        result.setErrorCode(0);

        return CompletableFuture.completedFuture(result);


    }

    //region Email 발송 API 호출
    public String SendEmail(CallSendEmailInputModel input){
        String url = "https://test-authemail.gnjoy.com/api/EmailAuth/exeEmailSend";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        HttpEntity<CallSendEmailInputModel> entity = new HttpEntity<>(input, headers);

        RestTemplate restTemplate = new RestTemplate();
        try {
            // API 호출 (POST 방식)
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            return response.getStatusCode().toString();
        } catch (Exception e) {
            // 예외 처리
            return e.getMessage();
        }
    }
    //endregion


    // region 내부 처리 소스

    // PlainText 생성
    private String getPlainText(String email, String sign, String gameCode, String secretKey) throws Exception {
        String timestamp = getTimestampString(sign);
        String plainText = timestamp + "|" + email + "|" + gameCode;
        return _helper.encryptAes256(plainText, secretKey, null);
    }

    // Sign값의 유효성 검사
    private void ValidateSignHeader(String sign) throws Exception {
        if (sign == null || sign.trim().isEmpty()) {
            throw new IllegalArgumentException("The sign parameter is required.");
        }

        // 1. 최소길이 검증
        if (sign.length() < 20) {
            throw new Exception("Invalid signature.");
        }

        // 2. sign에서 timestamp 추출
        String timestampString = getTimestampString(sign);
        long timestamp = 0;
        try {
            timestamp = Long.parseLong(timestampString);
        } catch (NumberFormatException e) {
            throw new Exception("Invalid signature.");
        }

        // 3. 유효시간 검증
        long validSeconds = 300;
        long currentTimestamp = Instant.now().getEpochSecond();
        long re = currentTimestamp - timestamp;

        if (currentTimestamp - timestamp > validSeconds) {
            throw new Exception("The signature has been expired.");
        }
    }

    // sign에서 timstamp 추출
    private String getTimestampString(String sign) {
        String start = sign.substring(0,5);
        String end = sign.substring(sign.length()-5);
        return start + end;
    }

    // DB에 저장될 Token 추출
    private String getEncrytString(String sign){
        String encStr = sign.substring(5);
        encStr = encStr.substring(0, encStr.length()-5);

        return encStr.toUpperCase();
    }

    // 중복 토큰 체크
    private void DuplicatedToken(String sign) throws Exception{
        String encString = getEncrytString(sign);

        List<EmailList> list = _emailService.getEmailList(encString);
        if(list != null && !list.isEmpty()){
            String signTimstamp = getTimestampString(sign);
            long timestamp = 0;

            try{
                timestamp = Long.parseLong(signTimstamp);
            }
            catch(NumberFormatException e){
                throw new Exception("Invalid signature.");
            }

            // 유효 시간 체크
            long validSeconds = 120;
            long currentTimestamp = Instant.now().getEpochSecond(); // 현재 시간의 Unix 타임스탬프

            if (currentTimestamp - timestamp < validSeconds) {
                throw new Exception("사용 할 수 없는 값입니다.");
            }
        }
    }

    // Base64 -> Url Safe Base64 변환
    private String toUrlSafeBase64(String base64){
        return base64
                .replace("+","-")
                .replace("/","_")
                .replace("=","");
    }
    // endregion







}
