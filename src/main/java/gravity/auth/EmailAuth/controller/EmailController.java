package gravity.auth.EmailAuth.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gravity.auth.EmailAuth.dto.CallSendAuthEmailInputModel;
import gravity.auth.EmailAuth.dto.EmailInputModel;
import gravity.auth.EmailAuth.dto.JsonResult;
import gravity.auth.EmailAuth.dto.TestOutputModel;
import gravity.auth.EmailAuth.entity.EmailList;
import gravity.auth.EmailAuth.helper.CryptoHelper;
import gravity.auth.EmailAuth.helper.JsonHelper;
import gravity.auth.EmailAuth.service.EmailListService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
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
public class EmailController {

    private final CryptoHelper _helper;
    private final EmailListService _emailService;
    private final JsonHelper _jsonHelper;

    @Value("${appOption.value.key}")
    private String KEY;
    @Value("${appOption.value.sendUrl}")
    private String URL;


    @Async
    @PostMapping
    public CompletableFuture<JsonResult> setPostAsync(@RequestBody TestOutputModel model) throws Exception {
        JsonResult json = new JsonResult();

        String sign = model.getSign();
        String email = model.getEmail();
        String gameCode = model.getGameCode();

        // 유효 sign 확인
        int result = ValidateSignHeader(sign);
        if(result != 0){
            json.setErrorCode(result);
            json.setMessage(_jsonHelper.convertToMessage(result));
            return CompletableFuture.completedFuture(json);
        };

        String encStr = getEncrytString(sign);

        // 동일 토큰 송신 처리
        result = DuplicatedToken(sign);
        if(result != 0){
            json.setErrorCode(result);
            json.setMessage(_jsonHelper.convertToMessage(result));
            return CompletableFuture.completedFuture(json);
        }

        EmailInputModel input = new EmailInputModel();
        input.setEmail(model.getEmail());
        input.setGameCode(model.getGameCode());
        input.setToken(encStr);


        // 회원정보 DB 저장
        result = _emailService.sendEmail(input);
        if(result != 0){
            json.setErrorCode(result);
            json.setMessage(_jsonHelper.convertToMessage(result));
            return CompletableFuture.completedFuture(json);
        }


        String plainText = getPlainText(email, sign, gameCode, KEY);
        if(plainText.contains("/") || plainText.contains("+") || plainText.contains("=")){
            plainText = toUrlSafeBase64(plainText);
        }
        CallSendAuthEmailInputModel inputModel = new CallSendAuthEmailInputModel();
        inputModel.setEmail(email);
        inputModel.setPlainText(plainText);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<CallSendAuthEmailInputModel> entity = new HttpEntity<>(inputModel, headers);

            RestTemplate restTemplate = new RestTemplate();
            // API 호출 (POST 방식)
            ResponseEntity<String> response = restTemplate.postForEntity(URL, entity, String.class);
            String responseBody = response.getBody();

            ObjectMapper obj = new ObjectMapper();
            JsonNode node = obj.readTree(responseBody);
            json.setErrorCode(node.path("error").asInt());
            json.setMessage(node.path("message").asText());
        } catch (Exception e) {
            // 예외 처리
            json.setErrorCode(-9999);
            json.setMessage(e.getMessage());
            return CompletableFuture.completedFuture(json);
        }
        return CompletableFuture.completedFuture(json);
    }

    //region Email 발송 API 호출
    public String SendEmail(CallSendAuthEmailInputModel input){
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        HttpEntity<CallSendAuthEmailInputModel> entity = new HttpEntity<>(input, headers);

        RestTemplate restTemplate = new RestTemplate();
        try {
            // API 호출 (POST 방식)
            ResponseEntity<String> response = restTemplate.exchange(URL, HttpMethod.POST, entity, String.class);
            return response.getStatusCode().toString();
        } catch (Exception e) {
            // 예외 처리
            return e.getMessage();
        }
    }
    //endregion


    //region 내부 처리 소스
    // PlainText 생성
    private String getPlainText(String email, String sign, String gameCode, String secretKey) throws Exception {
        String timestamp = getTimestampString(sign);
        String plainText = timestamp + "|" + email + "|" + gameCode;
        return _helper.encryptAes256(plainText, secretKey, null);
    }

    // Sign값의 유효성 검사
    private int ValidateSignHeader(String sign){
        if (sign == null || sign.trim().isEmpty()) {
            // return -1001 : sign null or empty
            return -1001;
        }

        // 1. 최소길이 검증
        if (sign.length() < 20) {
            // return -1002 : sign length under 20
            return -1002;
        }

        // 2. sign에서 timestamp 추출
        String timestampString = getTimestampString(sign);
        long timestamp = 0;
        try {
            timestamp = Long.parseLong(timestampString);
        } catch (NumberFormatException e) {
            // return -1003 : invalid sign
            return -1003;
        }

        // 3. 유효시간 검증
        long validSeconds = 300;
        long currentTimestamp = Instant.now().getEpochSecond();
        long re = currentTimestamp - timestamp;

        if (currentTimestamp - timestamp > validSeconds) {
            // return -1004 : expired sign
            return -1004;
        }
        return 0;
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
    private int DuplicatedToken(String sign){
        String encString = getEncrytString(sign);

        List<EmailList> list = _emailService.getEmailList(encString);
        if(list != null && !list.isEmpty()){
            String signTimstamp = getTimestampString(sign);
            long timestamp = 0;

            try{
                timestamp = Long.parseLong(signTimstamp);
            }
            catch(NumberFormatException e){
                // return -1003 : invalid sign
                return -1003;
            }

            // 유효 시간 체크
            long validSeconds = 120;
            long currentTimestamp = Instant.now().getEpochSecond(); // 현재 시간의 Unix 타임스탬프

            if (currentTimestamp - timestamp < validSeconds) {
                // return -1004 : expired sign
                return -1004;
            }
        }
        return 0;
    }

    // Base64 -> Url Safe Base64 변환
    private String toUrlSafeBase64(String base64){
        return base64
                .replace("+","-")
                .replace("/","_")
                .replace("=","");
    }

    //endregion







}
