package be.ibgebim.testapi3;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HalloController {
    @GetMapping("/hello")
    public String hello() {
        System.out.println("handleEvent called");
        return "Hello Wordddddld!";
    }
}