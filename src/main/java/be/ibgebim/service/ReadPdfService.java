package be.ibgebim.service;

import be.ibgebim.dto.BookmarkInfo;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageXYZDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
public class ReadPdfService {

    @Autowired
    public ReadPdfService(SharePointService sharePointService) {}

    /**
     * This method read a pdf node and get all lines of this pedf contained in a
     * specific page of it
     *
     * @param pdfToAnalyze the pdf file we want to read
     * @param pageNumber   the number of the page we want to read
     * @return an ArrayList<String> off all non empty lines in the PDF
     */
    public ArrayList<String> getPdfContentLinesInPage(File pdfToAnalyze, int pageNumber){
        System.out.println("ReadPdfServiceImpl.getPdfContentLines BEGIN");
        ArrayList<String> allPdfLinesInPage = new ArrayList<String>();

        try {
            // Get InputStream of the file
            FileInputStream pdfToAnalyze_is = new FileInputStream(pdfToAnalyze);

            // Create PdfReader and PdfTextExtractor from the InputStream
            PdfReader pdfReader = new PdfReader(pdfToAnalyze_is);
            PdfTextExtractor extractor = new PdfTextExtractor(pdfReader);

            String textFromPage = extractor.getTextFromPage(pageNumber);

            String[] result = textFromPage.split("\\r?\\n");
            for (int i = 0; i < result.length; i++) {
                String line = result[i];
                System.out.println("ReadPdfServiceImpl.getPdfContentLines line content : " + line);
                if (!"".equals(line)) {
                    System.out.println("ReadPdfServiceImpl.getPdfContentLines line not empty add it to list");
                    allPdfLinesInPage.add(line);
                }
            }
        } catch (IOException ioe) {
            System.out.println("ReadPdfServiceImpl.getPdfContentLines error :" + ioe.getMessage());
        }
        return allPdfLinesInPage;
    }
}
