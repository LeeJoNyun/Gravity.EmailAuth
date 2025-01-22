package gravity.auth.EmailAuth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TestOutputModel {

    @JsonProperty("email")
    public String Email;
    @JsonProperty("gameCode")
    public String GameCode;
    @JsonProperty("sign")
    public String Sign;
}
