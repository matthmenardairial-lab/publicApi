package be.ibgebim.testapi3;

import be.ibgebim.entity.SharePointProperties;
import be.ibgebim.service.SharePointChangesService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class RemoteEventReceiverController {

    private static final String EXPECTED_CLIENT_STATE = "testMatthieu";

    private final SharePointChangesService changesService;
    private final SharePointProperties props;

    public RemoteEventReceiverController(SharePointChangesService changesService, SharePointProperties props) {
        this.changesService = changesService;
        this.props = props;
    }
    //@PostMapping("/remote-event-receiver")
    /**public ResponseEntity handleEvent(@RequestBody String event) {
        System.out.println("Remote Event Receiver received: " + event);
        ResponseEntity entity = new ResponseEntity<>(event, HttpStatus.OK);
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(event);

            // Vérifier que l'événement est un ajout de document
            if (jsonNode.has("value")) {
                JsonNode valueNode = jsonNode.get("value").get(0);
                String eventType = valueNode.get("eventType").asText();
                String fileName = valueNode.get("resourceData").get("name").asText();
                String fileUrl = valueNode.get("resourceData").get("webUrl").asText();
                String parentFolderUrl = valueNode.get("resourceData").get("parentReference").get("path").asText();

                // Filtrer les événements pour le dossier spécifique
                if ("ItemAdded".equals(eventType) && parentFolderUrl.contains("/sites/POC-eSign/Checklists_Matthieu")) {
                    // Logique à exécuter lorsque le document est ajouté au dossier spécifique
                    System.out.println("Document ajouté: " + fileName + " à " + fileUrl);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return entity;
    }**/
    // Point d'entrée pour SharePoint
    @PostMapping("/remote-event-receiver")
    public ResponseEntity<String> handleEvent(
            @RequestParam(value = "validationtoken", required = false) String validationToken,
            @RequestBody(required = false) Map<String, Object> body) {
        System.out.println("handleEvent validationToken: " + validationToken);
        System.out.println("handleEvent body: " + body);

        // 1) Validation handshake
        if (validationToken != null && !validationToken.isBlank()) {
            return ResponseEntity.ok(validationToken);
        }

        // 1️⃣ Validation du webhook
        if (validationToken != null && !validationToken.isEmpty()) {
            // SharePoint attend exactement ce token en réponse
            return ResponseEntity.ok(validationToken);
        }

        // 2️⃣ Notification réelle
        if (body != null && body.containsKey("value")) {
            Object valueObj = body.get("value");
            if (valueObj instanceof List) {
                List<Map<String, Object>> notifications = (List<Map<String, Object>>) valueObj;
                for (Map<String, Object> notification : notifications) {
                    System.out.println("Notification details: " + notification);

                    String clientState = (String) notification.get("clientState");
                    String changeType = (String) notification.get("changeType");
                    String resource = (String) notification.get("resource");

                    // Vérification du clientState
                    if (!EXPECTED_CLIENT_STATE.equals(clientState)) {
                        System.out.println("ClientState invalide !");
                        continue;
                    }

                    System.out.printf("Changement détecté : %s sur %s%n", changeType, resource);

                    // Traitez les différents types de changements
                    if (notification.containsKey("resourceData")) {
                        Map<String, Object> resourceData = (Map<String, Object>) notification.get("resourceData");
                        System.out.println("Resource Data: " + resourceData);
                    } else {
                        System.out.println("Resource Data n'est pas retrouvé: " + notification.containsKey("resourceData"));
                    }

                    switch (changeType) {
                        case "ItemAdded":
                            System.out.println("Un document a été ajouté.");
                            break;
                        case "ItemUpdated":
                            System.out.println("Un document a été modifié.");
                            break;
                        case "ItemDeleted":
                            System.out.println("Un document a été supprimé.");
                            break;
                        default:
                            System.out.println("Type de changement non pris en charge : " + changeType);
                            break;
                    }
                }
            } else {
                System.out.println("Le format de la notification n'est pas une liste.");
            }
        } else {
            System.out.println("Le corps de la requête ne contient pas la clé 'value'.");
        }

        // Toujours renvoyer 200 OK à SharePoint
        return ResponseEntity.ok("OK");
    }
    @PostMapping("/remote-event-receiver-complete")
    public ResponseEntity<String> handleEventComplete(
            @RequestParam(value = "validationtoken", required = false) String validationToken,
            @RequestBody(required = false) Map<String, Object> body) {
        System.out.println("handleEvent called");
        System.out.println("body: " + body);

        if (body == null || !body.containsKey("value")) {
            // SharePoint peut parfois envoyer un corps vide (réessaie) -> toujours 200
            return ResponseEntity.ok("OK");
        }

        Object valueObj = body.get("value");
        if (!(valueObj instanceof List<?> list)) {
            return ResponseEntity.ok("OK");
        }

        // Plusieurs notifications peuvent arriver en batch
        for (Object o : list) {
            if (!(o instanceof Map<?,?> notif)) continue;

            String clientState = (String) notif.get("clientState");
            if (clientState == null || !clientState.equals(props.getExpectedClientState())) {
                // clientState invalide -> on ignore poliment
                continue;
            }

            // À titre indicatif : autres champs utiles (selon SPO)
            // String subscriptionId = (String) notif.get("subscriptionId");
            // String resource = (String) notif.get("resource"); // GUID de la liste
            // String siteUrl = (String) notif.get("siteUrl");
            // String webId = (String) notif.get("webId");
            // String expirationDateTime = (String) notif.get("expirationDateTime");

            // 3) Appeler GetChanges et traiter
            List<Map<String, Object>> changes = changesService.fetchAndProcessChanges();

            // (Ici tu peux logger, pousser vers une file, etc.)
            System.out.println("Changes processed: " + changes);
        }

        // SPO attend toujours 200 OK
        return ResponseEntity.ok("OK");
    }
}
