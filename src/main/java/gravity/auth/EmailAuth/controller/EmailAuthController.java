package gravity.auth.EmailAuth.controller;

import gravity.auth.EmailAuth.dto.EmailInputModel;
import gravity.auth.EmailAuth.dto.JsonResult;
import gravity.auth.EmailAuth.dto.TestOutputModel;
import gravity.auth.EmailAuth.entity.EmailList;
import gravity.auth.EmailAuth.helper.CryptoHelper;
import gravity.auth.EmailAuth.service.EmailListService;
import io.swagger.v3.core.util.Json;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sendApi")
public class EmailAuthController {

    private final CryptoHelper _helper;
    private final EmailListService _emailService;

    @PostMapping
    public JsonResult setPostAsync(@RequestBody TestOutputModel model) throws Exception {
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

        // 회원정보 DB 저장
        int result = _emailService.sendEmail(input);

        JsonResult rtn = new JsonResult();
        rtn.setErrorCode(result);
        rtn.setMessage("success");

        //우선 여기까지 개발
        return rtn;


    }

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
        if (currentTimestamp - timestamp > validSeconds) {
            throw new Exception("The signature has been expired.");
        }
    }

    private String getTimestampString(String sign) {
        return sign.substring(0, 10);
    }

    private String getEncrytString(String sign){
        String encStr = sign.substring(5);
        encStr = encStr.substring(0, encStr.length()-5);

        return encStr.toUpperCase();
    }

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
}
