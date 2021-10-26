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
    /**
     * A QRCode Generator
     * @param text content text
     * @param width width of the QRCode in px default 400
     * @param height height of the QRCode in px default 400
     * @param quiet the quiet zone size default 4
     * @param foreground foreground color in RGBA default #000000FF eg: #FF0000A1
     * @param background foreground color in RGBA default #FFFFFFFF eg: #FF0000A1
     * @param outerConnerColor the position detection patterns outer color, default is foreground color
     * @param middleConnerColor the position detection patterns middle color, default is background color(without alpha)
     * @param innerConnerColor the position detection patterns inner color, default is foreground color
     * @param type the type, default is rect, another choice is point
     * @param backgroundRound should the background be round? only point type have round background
     * @return a png file of qrcode
     */
    @GetMapping("/generate")
    public ResponseEntity<byte[]> generate(
            @RequestParam(name = "text") String text,
            @RequestParam(name = "width",required = false) Integer width,
            @RequestParam(name = "height",required = false) Integer height,
            @RequestParam(name = "quiet", required = false) Integer quiet,
            @RequestParam(name = "foreground", required = false) String foreground,
            @RequestParam(name = "background", required = false) String background,
            @RequestParam(name = "outerConnerColor", required = false) String outerConnerColor,
            @RequestParam(name = "middleConnerColor", required = false) String middleConnerColor,
            @RequestParam(name = "innerConnerColor", required = false) String innerConnerColor,
            @RequestParam(name = "type", required = false) String type,
            @RequestParam(name = "backgroundRound", required = false) Boolean backgroundRound
            ){
        byte[] imageBytes = null;
        try {
            //use QRCodeUtil to generate a image
            BufferedImage image = QRCodeUtil.generateQRCodeImage(text, width, height, quiet, foreground,
                    background, outerConnerColor, middleConnerColor, innerConnerColor, type,backgroundRound);
            //BufferedImage to byte array
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
