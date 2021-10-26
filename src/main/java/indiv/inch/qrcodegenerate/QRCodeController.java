package indiv.inch.qrcodegenerate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

@RestController
@CrossOrigin
@Slf4j
@RequestMapping("/qrcode")
public class QRCodeController {
    @GetMapping("/generate")
    public ResponseEntity<byte[]> generateRound(
            @RequestParam(name = "text") String text,
            @RequestParam(name = "width",required = false) Integer width,
            @RequestParam(name = "height",required = false) Integer height,
            @RequestParam(name = "quiet", required = false) Integer quiet,
            @RequestParam(name = "foreground", required = false) String foreground,
            @RequestParam(name = "background", required = false) String background,
            @RequestParam(name = "outerConnerColor", required = false) String outerConnerColor,
            @RequestParam(name = "middleConnerColor", required = false) String middleConnerColor,
            @RequestParam(name = "innerConnerColor", required = false) String innerConnerColor,
            @RequestParam(name = "type", required = false) String type
            ) throws IOException {
        byte[] imageBytes = null;
        try {
            BufferedImage image = QRCodeUtil.generateQRCodeImage(text, width, height, quiet, foreground, background, outerConnerColor, middleConnerColor, innerConnerColor, type);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(image,"png", out);
            imageBytes = out.toByteArray();
        } catch (Exception e) {
            log.error(e.toString());
            e.printStackTrace();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + System.currentTimeMillis() + ".png")
                .body(imageBytes);
    }
}
