package indiv.inch.qrcodegenerate;

import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.ByteMatrix;
import com.google.zxing.qrcode.encoder.Encoder;
import com.google.zxing.qrcode.encoder.QRCode;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Slf4j
public class QRCodeUtil {


    public static BufferedImage generateQRCodeImage( String text, Integer width, Integer height, Integer quiet,
                                                     String foreground, String background, String outerConnerColor,
                                                     String middleConnerColor, String innerConnerColor,
                                                     String type) throws WriterException {
        if (width == null || height == null) {
            width = 400;
            height = 400;
        }
        if (quiet == null) {
            quiet = 4;
        }
        if (type == null) {
            type = "rect";
        }
        Color color = ColorUtil.getColor(foreground);
        Color fore = color == null ? Color.black : color;
        color = ColorUtil.getColor(background);
        Color back = color == null ? Color.white : color;
        color = ColorUtil.getColor(outerConnerColor);
        Color outer = color == null ? fore : color;
        color = ColorUtil.getColor(middleConnerColor);
        Color middle = color == null ? new Color(back.getRed(),back.getGreen(),back.getBlue()) : color;
        color = ColorUtil.getColor(innerConnerColor);
        Color inner = color == null ? fore : color;
        return generate(text, width, height, quiet, fore, back, outer, middle, inner, type);
    }

    private static BufferedImage generate( String text, Integer width, Integer height, Integer quiet, Color fore,
                                           Color back, Color outer, Color middle,
                                           Color inner, String type) throws WriterException {

        final Map<EncodeHintType, Object> encodingHints = new HashMap<>();
        encodingHints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        QRCode code = Encoder.encode(text, ErrorCorrectionLevel.H, encodingHints);
        return renderQRImage(code, width, height, quiet, fore, back, outer, middle, inner, type);
    }

    private static BufferedImage renderQRImage(QRCode code, int width, int height, int quietZone, Color fore,
                                               Color back, Color outer, Color middle, Color inner, String type) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setBackground(back);
        graphics.clearRect(0, 0, width, height);
        graphics.setColor(fore);
        ByteMatrix input = code.getMatrix();
        if (input == null) {
            throw new IllegalStateException();
        }
        int inputWidth = input.getWidth();
        int inputHeight = input.getHeight();
        int qrWidth = inputWidth + (quietZone * 2);
        int qrHeight = inputHeight + (quietZone * 2);
        int outputWidth = Math.max(width, qrWidth);
        int outputHeight = Math.max(height, qrHeight);
        int multiple = Math.min(outputWidth / qrWidth, outputHeight / qrHeight);
        int leftPadding = (outputWidth - (inputWidth * multiple)) / 2;
        int topPadding = (outputHeight - (inputHeight * multiple)) / 2;
        final int FINDER_PATTERN_SIZE = 7;
        final float CIRCLE_SCALE_DOWN_FACTOR = 0.9f;
        int innerSize = (int) (multiple * CIRCLE_SCALE_DOWN_FACTOR);
        for (int inputY = 0, outputY = topPadding; inputY < inputHeight; inputY++, outputY += multiple) {
            for (int inputX = 0, outputX = leftPadding; inputX < inputWidth; inputX++, outputX += multiple) {
                if (input.get(inputX, inputY) == 1) {
                    if (!(inputX <= FINDER_PATTERN_SIZE && inputY <= FINDER_PATTERN_SIZE ||
                            inputX >= inputWidth - FINDER_PATTERN_SIZE && inputY <= FINDER_PATTERN_SIZE ||
                            inputX <= FINDER_PATTERN_SIZE && inputY >= inputHeight - FINDER_PATTERN_SIZE)) {
                        if (type.equals("rect")) {
                            graphics.fillRect(outputX, outputY, innerSize, innerSize);
                        } else if (type.equals("point")) {
                            graphics.fillOval(outputX, outputY, innerSize, innerSize);
                        }
                    }
                }
            }
        }
        if (type.equals("point")) {
            drawRoundBackground(graphics,topPadding,leftPadding,outputWidth,outputHeight,width,height,innerSize,multiple);
        }
        int cornerInnerSize = multiple * FINDER_PATTERN_SIZE;
        drawFinderPatternCircleStyle(graphics, leftPadding, topPadding, cornerInnerSize, outer, middle, inner, type);
        drawFinderPatternCircleStyle(graphics, leftPadding + (inputWidth - FINDER_PATTERN_SIZE) * multiple, topPadding, cornerInnerSize, outer, middle, inner, type);
        drawFinderPatternCircleStyle(graphics, leftPadding, topPadding + (inputHeight - FINDER_PATTERN_SIZE) * multiple, cornerInnerSize, outer, middle, inner, type);
        graphics.dispose();
        return image;
    }

    private static void drawRoundBackground(Graphics graphics, int topPadding, int leftPadding, int outputWidth, int outputHeight,
                                            int width, int height,int innerSize, int multiple) {
        Random random = new Random();
        for (int y = topPadding - multiple; y > -multiple; y -= multiple) {
            if (y < topPadding) {
                for (int x = leftPadding; x < outputWidth - leftPadding; x += multiple) {
                    if (checkInside(x,y,width,height,innerSize) && random.nextDouble() > 0.5) {
                        graphics.fillOval(x, y, innerSize, innerSize);
                    }
                }
            }
        }
        for (int y = topPadding; y < outputHeight; y += multiple) {
            for (int x = leftPadding - multiple; x > -multiple; x -= multiple) {
                if (checkInside(x,y,width,height,innerSize) && random.nextDouble() > 0.5) {
                    graphics.fillOval(x, y, innerSize, innerSize);
                }
            }
            for (int x = outputWidth - leftPadding; x < outputWidth; x += multiple) {
                if (checkInside(x,y,width,height,innerSize) && random.nextDouble() > 0.5) {
                    graphics.fillOval(x, y, innerSize, innerSize);
                }
            }
        }
        for (int y = outputHeight - topPadding; y < outputHeight + multiple; y += multiple){
            for (int x = leftPadding; x < outputWidth - leftPadding; x += multiple) {
                if (checkInside(x,y,width,height,innerSize) && random.nextDouble() > 0.5) {
                    graphics.fillOval(x, y, innerSize, innerSize);
                }
            }
        }
    }

    private static boolean checkInside(int x, int y, int width, int height, int innerSize) {
        double centerx = (double) width / 2;
        double centery = (double) height / 2;
        double distance =  Math.sqrt(((double)x - centerx) * ((double)x - centerx) + ((double)y - centery) * ((double)y - centery));
        if (distance + (double) innerSize> (double) width / 2) {
            return false;
        } else {
            return true;
        }
    }

    private static void drawFinderPatternCircleStyle(Graphics2D graphics, int x, int y, int cornerInnerSize, Color outer, Color middle, Color inner, String type) {
        final int MIDDLE_CIRCLE_SIZE = cornerInnerSize * 5 / 7;
        final int MIDDLE_CIRCLE_OFFSET = cornerInnerSize / 7;
        final int INNER_CIRCLE_SIZE = cornerInnerSize * 3 / 7;
        final int INNER_CIRCLE_OFFSET = cornerInnerSize * 2 / 7;
        graphics.setColor(outer);
        if (type.equals("rect")) {
            graphics.fillRect(x, y, cornerInnerSize, cornerInnerSize);
        } else if (type.equals("point")) {
            graphics.fillOval(x, y, cornerInnerSize, cornerInnerSize);
        }
        graphics.setColor(middle);
        if (type.equals("rect")) {
            graphics.fillRect(x + MIDDLE_CIRCLE_OFFSET, y + MIDDLE_CIRCLE_OFFSET, MIDDLE_CIRCLE_SIZE, MIDDLE_CIRCLE_SIZE);
        } else if (type.equals("point")) {
            graphics.fillOval(x + MIDDLE_CIRCLE_OFFSET, y + MIDDLE_CIRCLE_OFFSET, MIDDLE_CIRCLE_SIZE, MIDDLE_CIRCLE_SIZE);
        }
        graphics.setColor(inner);
        if (type.equals("rect")) {
            graphics.fillRect(x + INNER_CIRCLE_OFFSET, y + INNER_CIRCLE_OFFSET, INNER_CIRCLE_SIZE, INNER_CIRCLE_SIZE);
        } else if (type.equals("point")) {
            graphics.fillOval(x + INNER_CIRCLE_OFFSET, y + INNER_CIRCLE_OFFSET, INNER_CIRCLE_SIZE, INNER_CIRCLE_SIZE);
        }
    }
}
