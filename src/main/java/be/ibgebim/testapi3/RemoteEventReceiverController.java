package be.ibgebim.testapi3;

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
        //System.out.println("handleEvent validationToken: " + validationToken );
        //System.out.println("handleEvent validationToken: " + body );
        // 1️⃣ Validation du webhook
        if (validationToken != null && !validationToken.isEmpty()) {
            // SharePoint attend exactement ce token en réponse
            return ResponseEntity.ok(validationToken);
        }
        System.out.println("handleEvent body: " + body);
        // 2️⃣ Notification réelle
        if (body != null && body.containsKey("value")) {
            Object valueObj = body.get("value");
            if (valueObj instanceof List) {
                List<Map<String, Object>> notifications = (List<Map<String, Object>>) valueObj;
                for (Map<String, Object> notification : notifications) {
                    String clientState = (String) notification.get("clientState");
                    String changeType = (String) notification.get("changeType");
                    String resource = (String) notification.get("resource");

                    // Vérification du clientState
                    if (!EXPECTED_CLIENT_STATE.equals(clientState)) {
                        System.out.println(" ClientState invalide !");
                        continue;
                    }

                    System.out.printf(" Changement détecté : %s sur %s%n", changeType, resource);
                }
            }
        }

        // Toujours renvoyer 200 OK à SharePoint
        return ResponseEntity.ok("OK");
    }
}
