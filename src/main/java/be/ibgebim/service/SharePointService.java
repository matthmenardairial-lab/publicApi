package be.ibgebim.service;

import be.ibgebim.dto.SharePointDocument;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.authentication.*;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.models.*;
import com.microsoft.graph.requests.GraphServiceClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

@Service
public class SharePointService {
    @Value("${sharepoint.url}")
    public String sharepointUrl;
    @Value("${temporary.folder}")
    public String tempFolder;

    public String downloadFromSharePoint(SharePointDocument sharePointDocument){
        String downloadedDocPath = null;
        String sharePointDownloadUrl = sharepointUrl + "/sites/" + sharePointDocument.getSite() + "/_api/web/getfilebyid('" + sharePointDocument.getFileUUID() + "')/$value";
        String token = sharePointDocument.getAuthToken();
        String downloadFilePath = tempFolder+sharePointDocument.getDocFileName(); // Input DOCX file


        try {
            URL url = new URL(sharePointDownloadUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Authorization", "Bearer "+token);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (InputStream inputStream = connection.getInputStream();
                     FileOutputStream outputStream = new FileOutputStream(downloadFilePath)) {

                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    downloadedDocPath = downloadFilePath;
                    System.out.println("Fichier téléchargé avec succès : " + downloadFilePath);
                }
            } else {
                System.err.println("Echec de téléchargement du fichier : " + downloadFilePath);
            }
        } catch (Exception e) {
            System.err.println("Echec de téléchargement du fichier : " + downloadFilePath + " "+e.getMessage());
        }
        return downloadedDocPath;
    }

    public void uploadToSharePoint(SharePointDocument sharePointDocument, File file) throws IOException {
        String completeName = file.getName();
        System.out.println("completeName: " + completeName);
        String sharePointUploadUrl = sharepointUrl + "/sites/" + sharePointDocument.getSite() + "/_api/web/GetFolderByServerRelativeUrl('" + sharePointDocument.getSharePointPath() + "')/Files/add(url='" + completeName + "',overwrite=true)";

        System.out.println("uploadToSharePoint sharePointUrl "+sharePointUploadUrl);
        URL url = new URL(sharePointUploadUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + sharePointDocument.getAuthToken());
        connection.setRequestProperty("Accept", "application/json;odata=verbose");
        connection.setRequestProperty("Content-Type", "application/octet-stream");

        try {
            FileInputStream fis = new FileInputStream(file);
            OutputStream os = connection.getOutputStream();

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }

            // Envoyer la requête et traiter la réponse
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                System.out.println("Fichier uploadé avec succès !");
                os.close();
                fis.close();
                if (file.exists() && !file.delete()) {
                    System.err.println("Impossible de supprimer le fichier temporaire : " + file.getAbsolutePath());
                }
            } else {
                System.out.println("Échec de l'upload. Code de réponse : " + responseCode);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connection.disconnect();
        }
    }
}
