package be.ibgebim.testapi3;

import be.ibgebim.service.EntraIdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class MpooControler {
    @Value("${sharepoint.url}")
    public String sharepointUrl;

    private final EntraIdService entraIdService;

    @Autowired
    public MpooControler(EntraIdService entraIdService) {
        this.entraIdService = entraIdService;
    }

    // ✅ DTO interne statique
    public static class UserMailRequest {
        private String userMail;

        public String getUserMail() {
            return userMail;
        }

        public void setUserMail(String userMail) {
            this.userMail = userMail;
        }
    }

    @PostMapping("/mpoo/getHierarchy")
    public List<EntraIdService.UserDto> getHierarchy(@RequestBody UserMailRequest request) {
        System.out.println("MpooControler.getHierarchy method called");
        String userMail = request.getUserMail();
        System.out.println("MpooControler.getMailPropertyOnPDF userMail: " + userMail);
        return entraIdService.getUserHierarchy(userMail);
    }
}
