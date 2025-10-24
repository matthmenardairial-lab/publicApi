package be.ibgebim.service;

import be.ibgebim.config.SharePointRestClient;
import be.ibgebim.entity.SharePointProperties;
import be.ibgebim.store.ChangeTokenEntity;
import be.ibgebim.store.ChangeTokenRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class SharePointChangesService {
    private final SharePointRestClient client;
    private final ChangeTokenRepository repo;
    private final SharePointProperties props;

    public SharePointChangesService(SharePointRestClient client, ChangeTokenRepository repo, SharePointProperties props) {
        this.client = client;
        this.repo = repo;
        this.props = props;
    }

    public List<Map<String, Object>> fetchAndProcessChanges() {
        // 1) Charger le dernier token
        String listId = props.getListId();
        String lastToken = repo.findById(listId).map(ChangeTokenEntity::getLastChangeToken).orElse(null);
        System.out.println("fetchAndProcessChanges listId: " + listId);
        System.out.println("fetchAndProcessChanges lastToken: " + lastToken);
        // 2) Construire le ChangeQuery JSON (verbose)
        //   NB: pour odata=verbose, on doit envoyer __metadata.type
        StringBuilder json = new StringBuilder();
        json.append("{\"query\":{");
        json.append("\"__metadata\":{\"type\":\"SP.ChangeQuery\"},");
        json.append("\"Item\":true,");
        json.append("\"Add\":true,");
        json.append("\"Update\":true,");
        json.append("\"DeleteObject\":true");
        if (lastToken != null && !lastToken.isBlank()) {
            json.append(",\"ChangeTokenStart\":{\"__metadata\":{\"type\":\"SP.ChangeToken\"},\"StringValue\":\"")
                    .append(escape(lastToken)).append("\"}");
        }
        json.append("}}");
        System.out.println("fetchAndProcessChanges json: " + lastToken);
        String changeQueryJson = json.toString();
        System.out.println("fetchAndProcessChanges changeQueryJson: " + changeQueryJson);

        // 3) Obtenir le FormDigest
        String digest = client.getFormDigest();
        System.out.println("fetchAndProcessChanges digest: " + digest);

        // 4) POST GetChanges
        Map<String, Object> resp = client.postGetChanges(changeQueryJson, digest);
        System.out.println("fetchAndProcessChanges resp: " + resp);

        // 5) Lire la réponse odata=verbose:
        // Structure typique:
        // {
        //   "d": {
        //     "GetChanges": {
        //       "results": [
        //         { "__metadata": {...}, "ChangeType": 1, "ItemId": 3, "Time": "...", "ChangeToken": { "StringValue": "1;3;..." }, ... },
        //         ...
        //       ]
        //     }
        //   }
        // }
        Map<String, Object> d = (Map<String, Object>) resp.get("d");
        Map<String, Object> getChanges = (Map<String, Object>) d.get("GetChanges");
        List<Map<String, Object>> results = (List<Map<String, Object>>) getChanges.get("results");
        System.out.println("fetchAndProcessChanges d: " + d);
        System.out.println("fetchAndProcessChanges getChanges: " + getChanges);
        System.out.println("fetchAndProcessChanges results: " + results);

        List<Map<String, Object>> processed = new ArrayList<>();
        String newestToken = lastToken;

        if (results != null) {
            for (Map<String, Object> change : results) {
                Integer changeType = (Integer) change.get("ChangeType"); // 1=Add, 2=Update, 3=DeleteObject (valeurs internes SP)
                Integer itemId = (Integer) change.get("ItemId");
                System.out.println("fetchAndProcessChanges changeType: " + changeType);
                System.out.println("fetchAndProcessChanges itemId: " + itemId);
                // Met à jour le "newestToken" à chaque entrée (le dernier sera le plus récent)
                Map<String, Object> ct = (Map<String, Object>) change.get("ChangeToken");
                System.out.println("fetchAndProcessChanges ct: " + ct);

                if (ct != null) {
                    String s = (String) ct.get("StringValue");
                    if (s != null) newestToken = s;
                }

                // (Optionnel) filtre par dossier si demandé
                if (itemId != null && shouldKeepByFolder(itemId)) {
                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("ChangeType", mapChangeType(changeType));
                    entry.put("ItemId", itemId);
                    entry.put("Time", change.get("Time"));
                    processed.add(entry);
                }
            }
        }

        // 6) Persister le token le plus récent
        if (newestToken != null && !Objects.equals(newestToken, lastToken)) {
            repo.save(new ChangeTokenEntity(listId, newestToken, Instant.now()));
        }
        System.out.println("fetchAndProcessChanges processed: " + processed);

        return processed;
    }

    private boolean shouldKeepByFolder(int itemId) {
        String folder = props.getFilterFolder();
        if (folder == null || folder.isBlank()) return true;

        Map<String, Object> item = client.getListItem(itemId);
        String fileDirRef = (String) item.get("FileDirRef"); // ex: "/sites/POC-eSign/Checklists_Matthieu"
        if (fileDirRef == null) return false;
        // garde l'item s'il est dans le dossier cible (ou sous-dossiers)
        return fileDirRef.equalsIgnoreCase(folder) || fileDirRef.toLowerCase().startsWith(folder.toLowerCase() + "/");
    }

    private String mapChangeType(Integer ct) {
        if (ct == null) return "Unknown";
        return switch (ct) {
            case 1 -> "ItemAdded";
            case 2 -> "ItemUpdated";
            case 3 -> "ItemDeleted"; // DeleteObject
            default -> "Other(" + ct + ")";
        };
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
