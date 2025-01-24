package gravity.auth.EmailAuth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CallSendEmailInputModel {

    @JsonProperty("plainText")
    public String PlainText;

    @JsonProperty("email")
    public String Email;
}
