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

    public String getUserHierarchy(String userEmail){
        String hierarchy = "";
        // Création des credentials via azure-identity
        ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
                .tenantId(tenantId)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .build();

        // Scopes pour client-credentials : use .default sur graph
        List<String> scopes = Arrays.asList("https://graph.microsoft.com/.default");

        TokenCredentialAuthProvider authProvider = new TokenCredentialAuthProvider(scopes, clientSecretCredential);

        GraphServiceClient<Request> graphClient = GraphServiceClient.builder().authenticationProvider(authProvider).buildClient();

        try {
            // 1️⃣ Trouver le bon utilisateur
            UserCollectionPage usersPage = graphClient
                    .users()
                    .buildRequest()
                    .filter("mail eq '" + userEmail + "' or userPrincipalName eq '" + userEmail + "'")
                    .select("id,displayName,mail,userPrincipalName")
                    .get();

            if (usersPage.getCurrentPage().isEmpty()) {
                System.out.println("❌ Aucun utilisateur trouvé pour : " + userEmail);
                return null;
            }

            User user = usersPage.getCurrentPage().get(0);
            String currentUserId = user.id;

            System.out.println("✅ Utilisateur trouvé : " + user.displayName + " (" + user.userPrincipalName + ")");

            // 2️⃣ Remonter la hiérarchie
            List<String> hierarchyList = new ArrayList<>();

            while (true) {
                try {
                    DirectoryObject managerObject = graphClient
                            .users(currentUserId)
                            .manager()
                            .buildRequest()
                            .get();

                    if (managerObject instanceof User) {
                        User manager = (User) managerObject;

                        String managerInfo = manager.mail;
                        hierarchyList.add(managerInfo);

                        System.out.println("👔 Manager trouvé : " + managerInfo);

                        currentUserId = manager.id;
                    } else {
                        System.out.println("⚠️ Le manager n'est pas un utilisateur (contact/groupe). Arrêt de la remontée.");
                        break;
                    }

                } catch (com.microsoft.graph.http.GraphServiceException gse) {
                    if (gse.getResponseCode() == 404) {
                        // 404 = pas de manager supplémentaire → fin propre
                        System.out.println("⛔ Aucun manager supplémentaire trouvé. Fin de la hiérarchie.");
                        break;
                    } else {
                        // autre erreur = on l’affiche
                        System.err.println("❌ Erreur Graph : " + gse.getMessage());
                    }
                }
            }

            // 3️⃣ Concaténer le tout
            hierarchy = String.join(",", hierarchyList);
            System.out.println("🏁 Chaîne hiérarchique : " + hierarchy);
        } catch (Exception e) {
            e.printStackTrace();
        }


        /**try {
            DirectoryObject managerObject = graphClient
                    .users("Mathieu.Menard@inetum-realdolmen.world")
                    .manager()
                    .buildRequest()
                    .get();

            if (managerObject instanceof User) {
                User manager = (User) managerObject;
                System.out.println("Manager: " + manager.displayName);
                System.out.println("Mail: " + manager.mail);
            } else {
                System.out.println("Le manager n'est pas un utilisateur (peut-être un contact ou un groupe).");
            }
        } catch (com.microsoft.graph.http.GraphServiceException gse) {
            // 404 Not Found => pas de manager assigné
            if (gse.getResponseCode() == 404) {
                return null;
            }
            throw gse;
        }



        List<User> allUsers = new ArrayList<>();

        try {
            // 2️⃣ Appel à /users avec pagination automatique
            UserCollectionPage page = graphClient
                    .users()
                    .buildRequest()
                    // Optionnel : limiter les champs pour de meilleures perfs
                    .select("id,displayName,mail,userPrincipalName,jobTitle,department,manager")
                    .top(999) // 999 max par page
                    .get();

            while (page != null) {
                allUsers.addAll(page.getCurrentPage());

                // Récupération des pages suivantes
                if (page.getNextPage() != null) {
                    page = page.getNextPage().buildRequest().get();
                } else {
                    break;
                }
            }

            System.out.println("✅ Nombre total d’utilisateurs récupérés : " + allUsers.size());

            for (User user : allUsers) {
                System.out.println("✅ User : " + user.displayName + " / " +  user.mail);
            }
        } catch (GraphServiceException gse) {
            System.err.println("❌ Erreur Graph : " + gse.getMessage());
            throw gse;
        }**/



        return hierarchy;
    }

}
