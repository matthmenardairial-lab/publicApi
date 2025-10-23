package be.ibgebim.testapi3;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RemoteEventReceiverController {

    @PostMapping("/remote-event-receiver")
    public String handleEvent(@RequestBody String event) {
        System.out.println("Remote Event Receiver received: " + event);

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

        return "Event received";
    }
}
