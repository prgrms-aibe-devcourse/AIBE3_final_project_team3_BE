package triplestar.mixchat.home.home.controller;

import static java.net.InetAddress.getLocalHost;

import java.net.InetAddress;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @SneakyThrows
    @GetMapping
    public String main() {
        InetAddress localHost = getLocalHost();

        return """
                <h1>API 서버</h1>
                <p>Host Name: %s</p>
                <p>Host Address: %s</p>
                <div>
                    <a href="/swagger-ui/index.html">API 문서로 이동</a>
                </div>
                """.formatted(localHost.getHostName(), localHost.getHostAddress());
    }
}
