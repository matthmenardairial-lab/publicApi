package be.ibgebim.service;

import be.ibgebim.dto.BookmarkInfo;
import be.ibgebim.dto.SharePointDocument;
import com.lowagie.text.pdf.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageXYZDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
public class BookmarkService {
    @Value("${temporary.folder}")
    public String tempFolder;

    private final SharePointService sharePointService;

    @Autowired
    public BookmarkService(SharePointService sharePointService) {
        this.sharePointService = sharePointService;
    }


    public String addBookmarkOnPdf(String downloadedDocPath, String bookmark_name, int bookmark_pageNumber, int bookmark_posLeft, int bookmark_posTop) {
        String newPdfPath = null;
        if (downloadedDocPath != null) {
            System.out.println("BookmarkService.addBookmarkOnPdf downloadedDocPath: " +  downloadedDocPath);
            PDPageXYZDestination bookmarkDestination = new PDPageXYZDestination();
            bookmarkDestination.setPageNumber(bookmark_pageNumber);
            bookmarkDestination.setLeft(bookmark_posLeft);
            bookmarkDestination.setTop(bookmark_posTop);
            bookmarkDestination.setZoom(0);

            File myPdf = new File(downloadedDocPath);
            File copyPdf = new File(downloadedDocPath.replace(".pdf", "_bookmarkAdded.pdf"));

            try {
                Files.copy(myPdf.toPath(), copyPdf.toPath(), StandardCopyOption.REPLACE_EXISTING);
                PDDocument document = PDDocument.load(copyPdf);
                if (document.getNumberOfPages() > 0) {
                    PDPage page = document.getPage(bookmark_pageNumber);
                    bookmarkDestination.setPage(page);
                    PDDocumentOutline outline = document.getDocumentCatalog().getDocumentOutline();
                    if (outline == null) {
                        outline = new PDDocumentOutline();
                        document.getDocumentCatalog().setDocumentOutline(outline);
                    }

                    PDOutlineItem pagesOutline = new PDOutlineItem();
                    pagesOutline.setTitle(bookmark_name);
                    pagesOutline.setDestination(bookmarkDestination);
                    outline.addLast(pagesOutline);

                    document.save(copyPdf);
                    document.close();
                    System.out.println("Bookmark added successfully to the PDF.");
                    newPdfPath = copyPdf.getAbsolutePath();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return newPdfPath;
    }


    public BookmarkInfo detectBookmarkOnInvoice(File docToAnalyze,String bookmarkName){
        Boolean detected = false;
        try {
            FileInputStream docToAnalyze_is = new FileInputStream(docToAnalyze);
            PdfReader reader = new PdfReader(docToAnalyze_is);

            Map destinations = reader.getNamedDestination();
            Map filledFields = reader.getAcroFields().getFields();

            String nextBookmarkName = null;
            PdfArray nextBookmarkLocation = null;
            int pageNumber = -1;
            float xPos = 0;
            float yPos = 0;

            if (destinations != null && !destinations.isEmpty()) {
                Iterator d = destinations.keySet().iterator();
                while (d.hasNext()) {
                    String destination = (String) d.next();
                    if (destination.startsWith(bookmarkName) && filledFields.get(destination) == null) {
                        if (nextBookmarkName == null || nextBookmarkName.compareTo(destination) > 0) {
                            nextBookmarkName = destination;
                            nextBookmarkLocation = (PdfArray) destinations.get(nextBookmarkName);

                            pageNumber = getDestinationPageNumber(reader, nextBookmarkLocation);
                            xPos = (nextBookmarkLocation.getAsNumber(2)).floatValue();
                            yPos = (nextBookmarkLocation.getAsNumber(3)).floatValue();
                        }
                    }
                }
            }
            if(pageNumber == -1){
                List bookmarks = SimpleBookmark.getBookmark(reader);
                if (bookmarks != null) {
                    Iterator d = bookmarks.iterator();
                    while (d.hasNext()) {
                        Map bookmarkAttributes = (Map) d.next();
                        try {
                            String destination = (String) bookmarkAttributes.get("Title");
                            if (destination != null && destination.startsWith(bookmarkName)
                                    && filledFields.get(destination) == null) {
                                if (nextBookmarkName == null || nextBookmarkName.compareTo(destination) > 0) {
                                    nextBookmarkName = destination;

                                    String bookmarkCoordinatesString = (String) bookmarkAttributes.get("Page");
                                    String[] bookmarkCoordinatesArray = bookmarkCoordinatesString.split(" ");

                                    pageNumber = Integer.parseInt(bookmarkCoordinatesArray[0]);
                                    xPos = Integer.parseInt(bookmarkCoordinatesArray[2]);
                                    yPos = Integer.parseInt(bookmarkCoordinatesArray[3]);
                                }
                            }
                        } catch (RuntimeException e) {
                            System.out.println("Could not extract destination from SimpleBookmark " + bookmarkAttributes.toString());
                        }
                    }
                }
            }

            if (pageNumber > -1) {
                return new BookmarkInfo(xPos, yPos, pageNumber, true);
            } else {
                return new BookmarkInfo();
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private int getDestinationPageNumber(PdfReader reader, PdfArray dest) {
        int pageNumber = -1;

        PdfIndirectReference indRef = dest.getAsIndirectObject(0);
        int i = -1;

        PdfDictionary catalog = reader.getCatalog();
        PdfDictionary rootPages = (PdfDictionary) PdfReader.getPdfObject(catalog.get(PdfName.PAGES));
        PdfArray kids = rootPages.getAsArray(PdfName.KIDS);
        int kidsSize = kids.size();

        for (i = 0; (i <= kidsSize) && (pageNumber == -1); i++) {
            PdfIndirectReference pageRef = kids.getAsIndirectObject(i);
            if ((indRef.toString()).equals(pageRef.toString())) {
                pageNumber = i + 1;
            }
        }

        return pageNumber;
    }



}
