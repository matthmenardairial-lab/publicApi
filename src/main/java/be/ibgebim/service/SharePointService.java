package be.ibgebim.service;

import be.ibgebim.dto.SharePointDocument;
import com.microsoft.graph.authentication.*;
import com.microsoft.graph.models.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;

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

        String fileServerRelativeUrl = "/sites/" + sharePointDocument.getSite()
                + "/" + sharePointDocument.getSharePointPath()
                + "/" + sharePointDocument.getDocFileName();

        System.out.println("uploadToSharePoint fileServerRelativeUrl " + fileServerRelativeUrl);

        // 🔍 Étape 1 : Vérifier si le fichier existe déjà
        boolean exists = checkFileExists(sharePointDocument, fileServerRelativeUrl);

        if (exists) {
            System.out.println("📄 Fichier trouvé sur SharePoint, mise à jour (nouvelle version)...");
            uploadNewVersion(sharePointDocument, file, fileServerRelativeUrl);
        } else {
            System.out.println("🆕 Fichier non trouvé, création initiale...");
            uploadInitialFile(sharePointDocument, file);
        }
    }

    private boolean checkFileExists(SharePointDocument spDoc, String fileServerRelativeUrl) {
        try {
            String checkUrl = sharepointUrl
                    + "/sites/" + spDoc.getSite()
                    + "/_api/web/GetFileByServerRelativeUrl('" + fileServerRelativeUrl + "')";

            HttpURLConnection conn = (HttpURLConnection) new URL(checkUrl).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + spDoc.getAuthToken());
            conn.setRequestProperty("Accept", "application/json;odata=verbose");

            int code = conn.getResponseCode();
            conn.disconnect();

            return (code == 200);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    private void uploadNewVersion(SharePointDocument spDoc, File file, String fileServerRelativeUrl) {
        String uploadUrl = sharepointUrl
                + "/sites/" + spDoc.getSite()
                + "/_api/web/GetFileByServerRelativeUrl('" + fileServerRelativeUrl + "')/$value";

        System.out.println("uploadNewVersion URL: " + uploadUrl);

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(uploadUrl).openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("PUT"); // ⚠️ PUT obligatoire
            connection.setRequestProperty("Authorization", "Bearer " + spDoc.getAuthToken());
            connection.setRequestProperty("Accept", "application/json;odata=verbose");
            connection.setRequestProperty("Content-Type", "application/pdf");

            try (FileInputStream fis = new FileInputStream(file);
                 OutputStream os = connection.getOutputStream()) {

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK
                        || responseCode == HttpURLConnection.HTTP_CREATED
                        || responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                    System.out.println("✅ Nouvelle version du fichier créée avec succès !");
                } else {
                    System.err.println("❌ Échec du PUT. Code : " + responseCode);
                    System.out.println(connection);
                }
            }
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (file.exists() && !file.delete()) {
                System.err.println("Impossible de supprimer le fichier temporaire : " + file.getAbsolutePath());
            }
        }
    }

    private void uploadInitialFile(SharePointDocument spDoc, File file) {
        String uploadUrl = sharepointUrl
                + "/sites/" + spDoc.getSite()
                + "/_api/web/GetFolderByServerRelativeUrl('" + spDoc.getSharePointPath() + "')/Files/add(url='" + file.getName() + "',overwrite=true)";

        System.out.println("uploadInitialFile URL: " + uploadUrl);

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(uploadUrl).openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + spDoc.getAuthToken());
            connection.setRequestProperty("Accept", "application/json;odata=verbose");
            connection.setRequestProperty("Content-Type", "application/octet-stream");

            try (FileInputStream fis = new FileInputStream(file);
                 OutputStream os = connection.getOutputStream()) {

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_OK) {
                    System.out.println("✅ Fichier initial uploadé avec succès !");
                } else {
                    System.err.println("❌ Échec du POST initial. Code : " + responseCode);
                    System.out.println(connection);
                }
            }
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (file.exists() && !file.delete()) {
                System.err.println("Impossible de supprimer le fichier temporaire : " + file.getAbsolutePath());
            }
        }
    }



}
