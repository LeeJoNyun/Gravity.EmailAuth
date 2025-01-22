package gravity.auth.EmailAuth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class JsonResult {
    @JsonProperty("errorCode")
    public int errorCode;
    @JsonProperty("message")
    public String message;

}
