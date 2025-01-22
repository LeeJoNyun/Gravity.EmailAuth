package gravity.auth.EmailAuth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TestInputModel {
    @JsonProperty("gameCode")
    public String GameCode;
    @JsonProperty("email")
    public String Email;
}
