package be.ibgebim.service;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.models.DirectoryObject;
import com.microsoft.graph.models.User;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.requests.UserCollectionPage;
import okhttp3.Request;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class EntraIdService {

    @Value("${sharepoint.tenantId}")
    public String tenantId;
    @Value("${sharepoint.clientId}")
    public String clientId;
    @Value("${sharepoint.clientSecret}")
    public String clientSecret;

    public static class UserDto {
        private String id;
        private String displayName;
        private String mail;
        private String jobTitle;
        private String userPrincipalName;

        public UserDto() {}

        public UserDto(String id, String displayName, String mail, String jobTitle, String userPrincipalName) {
            this.id = id;
            this.displayName = displayName;
            this.mail = mail;
            this.jobTitle = jobTitle;
            this.userPrincipalName = userPrincipalName;
        }

        // --- Getters & Setters ---
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }

        public String getMail() { return mail; }
        public void setMail(String mail) { this.mail = mail; }

        public String getJobTitle() { return jobTitle; }
        public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

        public String getUserPrincipalName() { return userPrincipalName; }
        public void setUserPrincipalName(String userPrincipalName) { this.userPrincipalName = userPrincipalName; }
    }

    public List<UserDto> getUserHierarchy(String userEmail) {
        List<UserDto> hierarchyList = new ArrayList<>();

        try {
            // ✅ Authentification Azure AD
            ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
                    .tenantId(tenantId)
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .build();

            List<String> scopes = Arrays.asList("https://graph.microsoft.com/.default");
            TokenCredentialAuthProvider authProvider = new TokenCredentialAuthProvider(scopes, clientSecretCredential);
            GraphServiceClient<Request> graphClient = GraphServiceClient.builder().authenticationProvider(authProvider).buildClient();

            // 🔍 Recherche de l’utilisateur initial
            UserCollectionPage usersPage = graphClient
                    .users()
                    .buildRequest()
                    .filter("mail eq '" + userEmail + "' or userPrincipalName eq '" + userEmail + "'")
                    .select("id,displayName,mail,userPrincipalName,jobTitle")
                    .get();

            if (usersPage.getCurrentPage().isEmpty()) {
                System.out.println("❌ Aucun utilisateur trouvé pour : " + userEmail);
                return hierarchyList;
            }

            User user = usersPage.getCurrentPage().get(0);
            String currentUserId = user.id;
            System.out.println("✅ Utilisateur trouvé : " + user.displayName);

            // 🔁 Remonter la hiérarchie de managers
            while (true) {
                try {
                    DirectoryObject managerObject = graphClient
                            .users(currentUserId)
                            .manager()
                            .buildRequest()
                            .get();

                    if (managerObject instanceof User) {
                        User manager = (User) managerObject;

                        UserDto dto = new UserDto(
                                manager.id,
                                manager.displayName,
                                manager.mail,
                                manager.jobTitle,
                                manager.userPrincipalName
                        );

                        hierarchyList.add(dto);
                        System.out.println("👔 Manager trouvé : " + manager.displayName + " <" + manager.mail + ">");

                        currentUserId = manager.id;
                    } else {
                        System.out.println("⚠️ Le manager n'est pas un utilisateur. Arrêt de la remontée.");
                        break;
                    }
                } catch (com.microsoft.graph.http.GraphServiceException gse) {
                    if (gse.getResponseCode() == 404) {
                        System.out.println("⛔ Aucun manager supplémentaire trouvé. Fin de la hiérarchie.");
                        break;
                    } else {
                        System.err.println("❌ Erreur Graph : " + gse.getMessage());
                        break;
                    }
                }
            }
            System.out.println("🏁 Hiérarchie complète : " + hierarchyList.size() + " managers trouvés.");
            System.out.println("🏁 Hiérarchie complète : " + hierarchyList.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hierarchyList;
    }
}
