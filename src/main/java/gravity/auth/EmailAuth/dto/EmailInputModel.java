package gravity.auth.EmailAuth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class EmailInputModel {
    public String email;
    public String gameCode;
    public String token;

}
