package be.ibgebim.testapi3;

import be.ibgebim.dto.BookmarkInfo;
import be.ibgebim.dto.SharePointDocument;
import be.ibgebim.service.BookmarkService;
import be.ibgebim.service.ReadPdfService;
import be.ibgebim.service.SharePointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RestController
public class CptaMailoutControler {
    @Value("${sharepoint.url}")
    public String sharepointUrl;

    private final BookmarkService bookmarkService;
    private final SharePointService sharePointService;
    private final ReadPdfService readPdfService;

    @Autowired
    public CptaMailoutControler(BookmarkService bookmarkService,
                                SharePointService sharePointService,
                                ReadPdfService readPdfService) {
        this.bookmarkService = bookmarkService;
        this.sharePointService = sharePointService;
        this.readPdfService = readPdfService;
    }

    @PostMapping("/cpta/mailout/addBookmarkOnInvoice")
    public void addBookmarkOnInvoice(@RequestBody SharePointDocument spDocument) {
        System.out.println("cpta/mailout/addBookmarkOnInvoice method called");
        System.out.println("spDocument: " + spDocument);
        String downloadedDocPath = sharePointService.downloadFromSharePoint(spDocument);
        String newPath = bookmarkService.addBookmarkOnPdf(downloadedDocPath, "Signature1", 0, 1, 200);
        System.out.println("newPath: " + newPath);
        String newNewPath = bookmarkService.addBookmarkOnPdf(newPath, "Signature2", 0, 300, 200);
        System.out.println("newNewPath: " + newNewPath);
        File file = new File(newNewPath);
        try {
            sharePointService.uploadToSharePoint(spDocument, file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/cpta/mailout/detectBookmarkOnInvoice")
    public Map<String, Boolean> detectBookmarkOnInvoice(@RequestBody SharePointDocument spDocument) {
        Boolean arePresent = false;
        System.out.println("cpta/mailout/addBookmarkOnInvoice method called");
        System.out.println("spDocument: " + spDocument);
        String downloadedDocPath = sharePointService.downloadFromSharePoint(spDocument);
        File docToAnalyze = new File(downloadedDocPath);
        BookmarkInfo bookmarkInfos_Signature1 = bookmarkService.detectBookmarkOnInvoice(docToAnalyze, "Signature1");
        System.out.println("bookmarkInfos_Signature1 isPresent: " + bookmarkInfos_Signature1.isBookmarkPresent());
        BookmarkInfo bookmarkInfos_Signature2 = bookmarkService.detectBookmarkOnInvoice(docToAnalyze, "Signature2");
        System.out.println("bookmarkInfos_Signature2 isPresent: " + bookmarkInfos_Signature2.isBookmarkPresent());
        if (bookmarkInfos_Signature1.isBookmarkPresent() && bookmarkInfos_Signature2.isBookmarkPresent()) {
            arePresent = true;
        }
        // Créer une map pour retourner la réponse JSON
        Map<String, Boolean> response = new HashMap<>();
        response.put("arePresent", arePresent);
        return response;
    }

    private String getMailPropertyOnPDF(ArrayList<String> allPdfLinesOnFirstPage) {
        String mailProperty = "";

        for (int i = 0; i < allPdfLinesOnFirstPage.size(); i++) {
            String pdfLine = allPdfLinesOnFirstPage.get(i);
            System.out.println("CptaMailoutControler.getMailPropertyOnPDF, pdfLine: " + pdfLine);
            int indexOf_EMAIL = pdfLine.indexOf("E-MAIL");
            System.out.println("CptaMailoutControler.getMailPropertyOnPDF, indexOf_EMAIL: " + indexOf_EMAIL);
            if (indexOf_EMAIL > -1) {
                /** Suppress the Word "E-MAIL:" **/
                pdfLine = pdfLine.replace("E-MAIL:", "");
                /** Suppress blank space on the property **/
                pdfLine = pdfLine.replaceAll("\\s", "");

                mailProperty = pdfLine;
                break;
            }
            System.out.println("CptaMailoutControler.getMailPropertyOnPDF, mailProperty: " + mailProperty);
        }
        return mailProperty;
    }

    @PostMapping("/cpta/mailout/getMailPropertyOnPDF")
    public Map<String, String> getMailPropertyOnPDF(@RequestBody SharePointDocument spDocument) {
        System.out.println("CptaMailoutControler.getMailPropertyOnPDF method called");
        String email = "";

        System.out.println("CptaMailoutControler.getMailPropertyOnPDF spDocument: " + spDocument.toString());
        String downloadedDocPath = sharePointService.downloadFromSharePoint(spDocument);
        File docToAnalyze = new File(downloadedDocPath);

        ArrayList<String> allPdfLinesOnFirstPage = readPdfService.getPdfContentLinesInPage(docToAnalyze, 1);
        email = getMailPropertyOnPDF(allPdfLinesOnFirstPage);
        System.out.println("CptaMailoutControler.getMailPropertyOnPDF mailProperty: " + email);

        Map<String, String> response = new HashMap<>();
        response.put("email", email);
        return response;
    }

}
