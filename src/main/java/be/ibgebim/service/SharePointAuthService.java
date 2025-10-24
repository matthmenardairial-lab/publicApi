package be.ibgebim.service;

import be.ibgebim.entity.SharePointProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.Map;

@Service
public class SharePointAuthService {

    private final SharePointProperties props;
    private final WebClient http;
    private volatile String cachedToken;
    private volatile Instant expiresAt = Instant.EPOCH;

    public SharePointAuthService(SharePointProperties props) {
        this.props = props;
        this.http = WebClient.builder().build();
    }

    public synchronized String getAccessToken() {
        if (cachedToken != null && Instant.now().isBefore(expiresAt.minusSeconds(60))) {
            return cachedToken;
        }

        String tokenUrl = "https://login.microsoftonline.com/" + props.getTenantId() + "/oauth2/v2.0/token";

        var form = new LinkedMultiValueMap<String, String>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", props.getClientId());
        form.add("client_secret", props.getClientSecret());
        form.add("scope", props.getHost() + "/.default");

        Map<String, Object> resp = http.post()
                .uri(tokenUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(form)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        String token = (String) resp.get("access_token");
        Integer expiresIn = (Integer) resp.get("expires_in");
        this.cachedToken = token;
        this.expiresAt = Instant.now().plusSeconds(expiresIn != null ? expiresIn : 3000);
        return token;
    }
}
