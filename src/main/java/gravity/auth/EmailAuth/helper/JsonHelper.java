package gravity.auth.EmailAuth.helper;


import org.springframework.stereotype.Component;

@Component
public class JsonHelper {

    public String convertToMessage(int error){
        return switch (error) {
            case -1001 -> "sign null or empty";
            case -1002 -> "sign length under 20";
            case -1003 -> "invalid sign";
            case -1004 -> "expired sign";
            case -2000 -> "transaction error";
            case -2001 -> "wrong token value";
            case -3001 -> "expired token";
            default -> "success";
        };
    }

}
