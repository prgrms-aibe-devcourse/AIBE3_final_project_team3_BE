package triplestar.mixchat.testutils;

import org.springframework.context.annotation.Profile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import triplestar.mixchat.global.security.CustomUserDetails;

@Profile("test")
@RestController
@RequestMapping("/test/auth")
public class TestHelperController {

    @GetMapping
    public String test(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return "username : " + userDetails.getUsername()
                + ", password : " + userDetails.getPassword();
    }
}