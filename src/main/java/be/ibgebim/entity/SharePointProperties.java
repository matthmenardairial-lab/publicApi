package be.ibgebim.entity;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sharepoint")
public class SharePointProperties {
    private String tenantId = "5e10662a-82a7-400d-9015-b7e723952f4f";
    private String clientId = "ab37203f-6fd7-4baa-b2fd-85aa92ca2293";
    private String clientSecret = "aPc8Q~urwPwubc75m-jhuODPb3zv2D._CrmkLaQ.";
    private String host = "https://mnx4.sharepoint.com";        // ex: https://mnx4.sharepoint.com
    private String sitePath = "/sites/POC-eSign";    // ex: /sites/POC-eSign
    private String listId = "5f3571aa-b68a-49f5-9224-3df47ca0c85e";      // GUID
    private String expectedClientState;
    private String filterFolder; // peut être null/empty

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getSitePath() {
        return sitePath;
    }

    public void setSitePath(String sitePath) {
        this.sitePath = sitePath;
    }

    public String getListId() {
        return listId;
    }

    public void setListId(String listId) {
        this.listId = listId;
    }

    public String getExpectedClientState() {
        return expectedClientState;
    }

    public void setExpectedClientState(String expectedClientState) {
        this.expectedClientState = expectedClientState;
    }

    public String getFilterFolder() {
        return filterFolder;
    }

    public void setFilterFolder(String filterFolder) {
        this.filterFolder = filterFolder;
    }
}
