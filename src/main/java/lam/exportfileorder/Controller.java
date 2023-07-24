package lam.exportfileorder;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class Controller {
    @GetMapping("/download/excel")
    public ResponseEntity<InputStreamResource> downloadExcelFile() throws IOException {
        LocalDateTime now = LocalDateTime.now();

        // Đọc tệp Excel từ đường dẫn đã cho
        String pathFile = "D:\\Traning\\ProjectTraning\\warehouse-management\\WareHouseManagement\\src\\main\\resources\\static\\public\\fileTemplates\\LabelTemp.xlsx";
        FileInputStream fileInputStream = new FileInputStream(pathFile);
        Workbook workbook = new XSSFWorkbook(fileInputStream);
        fileInputStream.close();
        Sheet sheet = workbook.getSheetAt(0);
        workbook.setSheetName(0, "Đơn 1");
        // Tiến hành các thao tác xử lý dữ liệu trong tệp Excel nếu cần
        // Lấy hoặc tạo ô cần đặt giá trị (ví dụ: ô ở dòng 1 và cột 1)
        Row row = sheet.getRow(7);
        Cell cell = row.createCell(1);

        // Giới hạn nội dung của ô không vượt quá 20 kí tự
        String value = "Hello, tôi là bá lâm nè tôi đến từ Đông Anh!";
        int maxLength = 30;
        int rowNum = cell.getRowIndex();
        int colNum = cell.getColumnIndex();

        // Đặt giá trị cho ô
        if (value.length() > maxLength) {
            CellStyle dateCellStyle = workbook.createCellStyle();
            Font dateFont = workbook.createFont();
            dateFont.setFontName("Arial");
            dateFont.setFontHeightInPoints((short) 9);
            dateCellStyle.setFont(dateFont);
            cell.setCellStyle(dateCellStyle);
            cell.setCellValue(value.substring(0, maxLength));

            // Tạo các ô mới ở các cell bên dưới (nếu cần) để đặt phần còn lại của giá trị
            int remainingLength = value.length() - maxLength;
            while (remainingLength > 0) {
                row = sheet.getRow(++rowNum);
                if (row == null) {
                    row = sheet.createRow(rowNum);
                }
                cell = row.createCell(colNum);

                cell.setCellStyle(dateCellStyle);
                cell.setCellValue(value.substring(maxLength, Math.min(maxLength + maxLength, value.length())));
                remainingLength -= maxLength;
                maxLength = Math.min(maxLength, remainingLength);
            }
        } else {
            cell.setCellValue(value);
        }


        createQR(workbook, sheet);
        createCode(workbook, sheet);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

        // Chuyển đổi thời gian thành chuỗi dạng định dạng
        String formattedDateTime = now.format(formatter);
        row = sheet.getRow(26);
        cell = row.createCell(12);
        cell.setCellValue(formattedDateTime);
        if (formattedDateTime.length() > 10) {
            String firstPart = formattedDateTime.substring(0, 10);
            String secondPart = formattedDateTime.substring(10);
            CellStyle dateCellStyle = workbook.createCellStyle();
            Font dateFont = workbook.createFont();
            dateFont.setFontName("Arial");
            dateFont.setFontHeightInPoints((short) 11); // Cỡ chữ 12
            dateCellStyle.setAlignment(HorizontalAlignment.CENTER); // Căn giữa nội dung
            dateFont.setBold(true); // Đậm nhạt
            dateCellStyle.setFont(dateFont);
            cell.setCellStyle(dateCellStyle);
            cell.setCellValue(firstPart);

            // Tạo dòng mới (hàng 27) và đặt phần còn lại của giá trị vào ô ở cột 12 của dòng đó
            row = sheet.getRow(27);
            cell = row.createCell(12);
            cell.setCellStyle(dateCellStyle);
            cell.setCellValue(secondPart);
        } else {
            cell.setCellValue(formattedDateTime);
        }
        // Ghi workbook vào ByteArrayOutputStream để tạo tệp mới


        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        // Chuyển đổi ByteArrayOutputStream thành ByteArrayInputStream để trả về dữ liệu
        byte[] excelBytes = outputStream.toByteArray();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(excelBytes);

        // Thiết lập HTTP Headers để đảm bảo tệp Excel mới được tải xuống
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=" + now + ".xlsx");

        // Trả về phản hồi với tệp Excel mới như InputStreamResource
        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(inputStream));
    }

    private static void createQR(Workbook workbook, Sheet sheet) throws IOException {
        Map<String, String> barcodeContentMap = new HashMap<>();
        barcodeContentMap.put("https://chat.openai.com/", "DH-230724-00001");


        for (Map.Entry<String, String> entry : barcodeContentMap.entrySet()) {
            String barcode = entry.getKey();
            // Tạo mã vạch từ nội dung
            BitMatrix bitMatrix = null;
            try {
                bitMatrix = new MultiFormatWriter().encode(barcode, BarcodeFormat.QR_CODE, 110, 110,
                        createBarcodeHints());
            } catch (WriterException e) {
                throw new RuntimeException(e);
            }

            // Ghi mã vạch vào file Excel
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", out);
            int pictureIdx = workbook.addPicture(out.toByteArray(), Workbook.PICTURE_TYPE_PNG);

            Drawing drawing = sheet.getDrawingPatriarch();
            ClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 12, 19, 13, 23);
            Picture picture = drawing.createPicture(anchor, pictureIdx);
            picture.resize();


        }
    }

    private static void createCode(Workbook workbook, Sheet sheet) throws IOException {
        Map<String, String> barcodeContentMap = new HashMap<>();
        barcodeContentMap.put("https://chat.openai.com/", "DH-230724-00001");


        for (Map.Entry<String, String> entry : barcodeContentMap.entrySet()) {
            String barcode = entry.getKey();
            String content = entry.getValue();
            int codeWidth = 100; // Đặt độ rộng mã là 100 pixel
            int codeHeight = 35; // Đặt độ cao mã là 35 pixel
            // Tạo mã vạch từ nội dung
            BitMatrix bitMatrix = null;
            try {
                bitMatrix = new MultiFormatWriter().encode(barcode, BarcodeFormat.CODE_128, codeWidth, codeHeight,
                        createBarcodeHints());
            } catch (WriterException e) {
                throw new RuntimeException(e);
            }

            // Ghi mã vạch vào file Excel
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", out);
            int pictureIdx = workbook.addPicture(out.toByteArray(), Workbook.PICTURE_TYPE_PNG);

            Drawing drawing = sheet.getDrawingPatriarch();
            ClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 10, 1, 14, 2);
            Picture picture = drawing.createPicture(anchor, pictureIdx);
            // Tính toán tỷ lệ giữa width và height để thay đổi kích thước mã
            picture.resize();

            // Ghi nội dung mã vạch vào cột thứ hai
            CellStyle dateCellStyle11 = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setFontName("Arial");
            font.setFontHeightInPoints((short) 9); // Cỡ chữ 12
            Row row1 = sheet.getRow(3);
            Cell cell1 = row1.createCell(13);
            dateCellStyle11.setFont(font);
            cell1.setCellStyle(dateCellStyle11);
            cell1.setCellValue(content);
        }
    }

    private static Map<EncodeHintType, Object> createBarcodeHints() {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8"); // Đặt bộ ký tự
        return hints;
    }
}
