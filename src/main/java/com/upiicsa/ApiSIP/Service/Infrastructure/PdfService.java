package com.upiicsa.ApiSIP.Service.Infrastructure;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.upiicsa.ApiSIP.Exception.BusinessException;
import com.upiicsa.ApiSIP.Model.Enum.CoordsEnum;
import com.upiicsa.ApiSIP.Model.Enum.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Service
public class PdfService {

    @Value("${storage.certificate.pdf}")
    private String scr;

    @Value("${storage.save.certificate}")
    private String dest;

    public void stampTextOnPdf(Map<CoordsEnum, String> dataToStamp, String outputFileName) {

        try {
            PdfReader pdfReader = new PdfReader(scr);
            PdfStamper pdfStamper = new PdfStamper(pdfReader, new FileOutputStream(dest + outputFileName));
            PdfContentByte content = pdfStamper.getOverContent(1);

            BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
            content.beginText();
            content.setFontAndSize(bf, 10);
            content.setColorFill(BaseColor.BLUE);

            for (Map.Entry<CoordsEnum, String> entry : dataToStamp.entrySet()) {
                CoordsEnum coord = entry.getKey();
                String text = entry.getValue();
                writeText(content, text, coord.getCoordsX(), coord.getCoordsY());
            }

            content.endText();

            pdfStamper.close();
            pdfReader.close();
            System.out.println("Text included and saved");
        } catch (IOException | com.itextpdf.text.DocumentException e){
            throw new BusinessException(ErrorCode.PDF_GENERATION_ERROR);
        }
    }

    private void writeText(PdfContentByte content, String text, float x, float y) {
        if(text != null && !text.trim().isEmpty() && x != -1 && y != -1) {
            content.showTextAligned(PdfContentByte.ALIGN_LEFT, text, x, y, 0);
        }
    }

    public Resource loadCedulaAsResource(String enrollment) {
        try {
            Path filePath = Paths.get(dest).resolve("cedula_" + enrollment + ".pdf").normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                return null;
            }
        } catch (MalformedURLException ex) {
            throw new BusinessException(ErrorCode.FILE_STORAGE_ERROR);
        }
    }
}
