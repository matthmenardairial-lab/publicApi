package be.ibgebim.config;
import be.ibgebim.entity.SharePointProperties;
import be.ibgebim.service.SharePointAuthService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class SharePointRestClient {
        private final SharePointAuthService auth;
        private final SharePointProperties props;
        private final WebClient http;

        public SharePointRestClient(SharePointAuthService auth, SharePointProperties props) {
            this.auth = auth;
            this.props = props;
            this.http = WebClient.builder().build();
        }

        private String webUrl() {
            return props.getHost() + props.getSitePath();
        }

        /** FormDigest pour POST REST */
        public String getFormDigest() {
            String token = auth.getAccessToken();

            Map<String, Object> resp = http.post()
                    .uri(webUrl() + "/_api/contextinfo")
                    .header("Authorization", "Bearer " + token)
                    .header("Accept", "application/json;odata=nometadata")
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            // nometadata renvoie : { "FormDigestValue": "...", "FormDigestTimeoutSeconds": 1800, ... }
            return (String) resp.get("FormDigestValue");
        }

        /** POST GetChanges */
        public Map<String, Object> postGetChanges(String changeQueryJson, String formDigest) {
            String token = auth.getAccessToken();

            return http.post()
                    .uri(webUrl() + "/_api/web/lists(guid'" + props.getListId() + "')/GetChanges")
                    .header("Authorization", "Bearer " + token)
                    .header("Accept", "application/json;odata=verbose")        // on envoie __metadata => verbose
                    .header("Content-Type", "application/json;odata=verbose")
                    .header("X-RequestDigest", formDigest)
                    .bodyValue(changeQueryJson)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
        }

        /** GET item pour vérifier le répertoire */
        public Map<String, Object> getListItem(int itemId) {
            String token = auth.getAccessToken();
            String select = "$select=Id,FileRef,FileDirRef,Title,ContentTypeId";

            return http.get()
                    .uri(webUrl() + "/_api/web/lists(guid'" + props.getListId() + "')/items(" + itemId + ")?" + select)
                    .header("Authorization", "Bearer " + token)
                    .header("Accept", "application/json;odata=nometadata")
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
        }

}
