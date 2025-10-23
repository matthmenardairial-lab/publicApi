package be.ibgebim.dto;

public class SharePointDocument {
    private String docFileName;
    private String authToken;
    private String sharePointPath;
    private String fileUUID;
    private String site;

    public SharePointDocument() {
    }

    public SharePointDocument(String docFileName, String authToken, String sharePointPath, String fileUUID, String site) {
        this.docFileName = docFileName;
        this.authToken = authToken;
        this.sharePointPath = sharePointPath;
        this.fileUUID = fileUUID;
        this.site = site;
    }

    public String getDocFileName() {
        return docFileName;
    }

    public void setDocFileName(String docFileName) {
        this.docFileName = docFileName;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String getSharePointPath() {
        return sharePointPath;
    }

    public void setSharePointPath(String sharePointPath) {
        this.sharePointPath = sharePointPath;
    }

    public String getFileUUID() {
        return fileUUID;
    }

    public void setFileUUID(String fileUUID) {
        this.fileUUID = fileUUID;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }
}
