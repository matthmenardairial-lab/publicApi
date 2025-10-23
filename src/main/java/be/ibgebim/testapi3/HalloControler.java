package be.ibgebim.testapi3;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HalloControler {
    @GetMapping("/hello")
    public String hello() {
        return "Hello Wordddddld!";
    }
}
