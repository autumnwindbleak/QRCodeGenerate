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

    /**
     * generate a qrcode access function
     * mainly set default if some options are null
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
     * @return {BufferedImage} type of qrcode
     */
    public static BufferedImage generateQRCodeImage( String text, Integer width, Integer height, Integer quiet,
                                                     String foreground, String background, String outerConnerColor,
                                                     String middleConnerColor, String innerConnerColor,
                                                     String type, Boolean backgroundRound) throws WriterException {
        //set default if they are null
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
        if (backgroundRound == null) {
            backgroundRound = false;
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
        //use set config and generate
        return configAndGenerate(text, width, height, quiet, fore, back, outer, middle, inner, type, backgroundRound);
    }

    /**
     * generate a qrcode access function
     * mainly set some configuration of the qrcode and generate the qrcode matrix
     * and then render it
     * @param text content text
     * @param width width of the QRCode in px default 400
     * @param height height of the QRCode in px default 400
     * @param quiet the quiet zone size default 4
     * @param fore color in RGBA
     * @param back foreground color in RGBA
     * @param outer the position detection patterns outer color
     * @param middle the position detection patterns middle color
     * @param inner the position detection patterns inner color
     * @param type the type, default is rect, another choice is point
     * @param backgroundRound should the background be round? only point type have round background
     * @return {BufferedImage} type of qrcode
     */
    private static BufferedImage configAndGenerate( String text, Integer width, Integer height, Integer quiet, Color fore,
                                           Color back, Color outer, Color middle,
                                           Color inner, String type, Boolean backgroundRound) throws WriterException {

        final Map<EncodeHintType, Object> encodingHints = new HashMap<>();
        encodingHints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        QRCode code = Encoder.encode(text, ErrorCorrectionLevel.H, encodingHints);
        //render the image with the matrix
        return renderQRImage(code, width, height, quiet, fore, back, outer, middle, inner, type,backgroundRound);
    }

    /**
     * function that render the qrcode using the QRCode matrix
     * @param width width of the QRCode in px default 400
     * @param height height of the QRCode in px default 400
     * @param quietZone the quiet zone size default 4
     * @param fore color in RGBA
     * @param back foreground color in RGBA
     * @param outer the position detection patterns outer color
     * @param middle the position detection patterns middle color
     * @param inner the position detection patterns inner color
     * @param type the type, default is rect, another choice is point
     * @param backgroundRound should the background be round? only point type have round background
     * @return {BufferedImage} type of qrcode
     */
    private static BufferedImage renderQRImage(QRCode code, int width, int height, int quietZone, Color fore,
                                               Color back, Color outer, Color middle, Color inner, String type,
                                               Boolean backgroundRound) {
        //create BufferedImage
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        //get Graphics
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        //set background color
        graphics.setBackground(back);
        //clear first
        graphics.clearRect(0, 0, width, height);
        //set foreground color
        graphics.setColor(fore);
        //get matrix
        ByteMatrix input = code.getMatrix();
        if (input == null) {
            throw new IllegalStateException();
        }
        //matrix width and height (how many points)
        int inputWidth = input.getWidth();
        int inputHeight = input.getHeight();
        // whole qrcode width and height (how many point)
        int qrWidth = inputWidth + (quietZone * 2);
        int qrHeight = inputHeight + (quietZone * 2);
        // qr code width and height in pixel
        int outputWidth = Math.max(width, qrWidth);
        int outputHeight = Math.max(height, qrHeight);
        // how may pixel in a point
        int multiple = Math.min(outputWidth / qrWidth, outputHeight / qrHeight);
        // quiet zone padding in pixel
        int leftPadding = (outputWidth - (inputWidth * multiple)) / 2;
        int topPadding = (outputHeight - (inputHeight * multiple)) / 2;
        // size of the position detection patterns
        final int FINDER_PATTERN_SIZE = 7;
        // the size of a point ( percentage to fill the point rect)
        final float CIRCLE_SCALE_DOWN_FACTOR = 0.9f;
        // pixel of the fill size
        int innerSize = (int) (multiple * CIRCLE_SCALE_DOWN_FACTOR);
        // loop y and x and get render the information points
        for (int inputY = 0, outputY = topPadding; inputY < inputHeight; inputY++, outputY += multiple) {
            for (int inputX = 0, outputX = leftPadding; inputX < inputWidth; inputX++, outputX += multiple) {
                if (input.get(inputX, inputY) == 1) {
                    if (!(inputX <= FINDER_PATTERN_SIZE && inputY <= FINDER_PATTERN_SIZE ||
                            inputX >= inputWidth - FINDER_PATTERN_SIZE && inputY <= FINDER_PATTERN_SIZE ||
                            inputX <= FINDER_PATTERN_SIZE && inputY >= inputHeight - FINDER_PATTERN_SIZE)) {
                        //which type is the information point should be rendered
                        if (type.equals("rect")) {
                            graphics.fillRect(outputX, outputY, innerSize, innerSize);
                        } else if (type.equals("point")) {
                            graphics.fillOval(outputX, outputY, innerSize, innerSize);
                        }
                    }
                }
            }
        }
        //if the rendered type is point make the background point
        if (type.equals("point") && backgroundRound) {
            drawRoundBackground(graphics,topPadding,leftPadding,outputWidth,outputHeight,innerSize,multiple);
        }
        //draw the position detection patterns now
        int cornerInnerSize = multiple * FINDER_PATTERN_SIZE;
        //draw three position detection patterns in different location
        drawFinderPatternCircleStyle(graphics, leftPadding, topPadding, cornerInnerSize, outer, middle, inner, type);
        drawFinderPatternCircleStyle(graphics, leftPadding + (inputWidth - FINDER_PATTERN_SIZE) * multiple, topPadding, cornerInnerSize, outer, middle, inner, type);
        drawFinderPatternCircleStyle(graphics, leftPadding, topPadding + (inputHeight - FINDER_PATTERN_SIZE) * multiple, cornerInnerSize, outer, middle, inner, type);
        //close graphics
        graphics.dispose();
        return image;
    }

    /**
     * draw a round background of the qrcode
     * @param graphics the graphics to draw
     * @param topPadding quiet zone padding in pixel
     * @param leftPadding quiet zone padding in pixel
     * @param outputWidth qr code width in pixel
     * @param outputHeight qr code height in pixel
     * @param innerSize the size of fill a point in pixel
     * @param multiple how many pixel in a point
     */
    private static void drawRoundBackground(Graphics graphics, int topPadding, int leftPadding, int outputWidth, int outputHeight,
                                            int innerSize, int multiple) {
        Random random = new Random();
        //draw top
        for (int y = topPadding - multiple; y > -multiple; y -= multiple) {
            if (y < topPadding) {
                for (int x = leftPadding; x < outputWidth - leftPadding; x += multiple) {
                    if (checkInside(x,y,outputWidth,outputHeight,innerSize) && random.nextDouble() > 0.5) {
                        graphics.fillOval(x, y, innerSize, innerSize);
                    }
                }
            }
        }
        //draw middle
        for (int y = topPadding; y < outputHeight; y += multiple) {
            //draw left middle
            for (int x = leftPadding - multiple; x > -multiple; x -= multiple) {
                if (checkInside(x,y,outputWidth,outputHeight,innerSize) && random.nextDouble() > 0.5) {
                    graphics.fillOval(x, y, innerSize, innerSize);
                }
            }
            //draw right middle
            for (int x = outputWidth - leftPadding; x < outputWidth; x += multiple) {
                if (checkInside(x,y,outputWidth,outputHeight,innerSize) && random.nextDouble() > 0.5) {
                    graphics.fillOval(x, y, innerSize, innerSize);
                }
            }
        }
        //draw bottom
        for (int y = outputHeight - topPadding; y < outputHeight + multiple; y += multiple){
            for (int x = leftPadding; x < outputWidth - leftPadding; x += multiple) {
                if (checkInside(x,y,outputWidth,outputHeight,innerSize) && random.nextDouble() > 0.5) {
                    graphics.fillOval(x, y, innerSize, innerSize);
                }
            }
        }
    }

    /**
     * check if a point should be drawn in round background
     * @param x the point x location
     * @param y the point y location
     * @param width width of the qrcode
     * @param height height of the qrcode
     * @param innerSize draw size of the point
     * @return true if should be drawn otherwise false
     */
    private static boolean checkInside(int x, int y, int width, int height, int innerSize) {
        double centerx = (double) width / 2;
        double centery = (double) height / 2;
        double distance =  Math.sqrt(((double)x - centerx) * ((double)x - centerx) + ((double)y - centery) * ((double)y - centery));
        return distance + (double) innerSize < (double) width / 2;
    }

    /**
     * draw the position detection patterns
     * @param graphics the graphics to draw
     * @param x location x
     * @param y location y
     * @param cornerInnerSize size of it
     * @param outer color of outer
     * @param middle color of middle
     * @param inner color of inner
     * @param type type of qr code rect/point
     */
    private static void drawFinderPatternCircleStyle(Graphics2D graphics, int x, int y, int cornerInnerSize, Color outer, Color middle, Color inner, String type) {
        final int MIDDLE_CIRCLE_SIZE = cornerInnerSize * 5 / 7;
        final int MIDDLE_CIRCLE_OFFSET = cornerInnerSize / 7;
        final int INNER_CIRCLE_SIZE = cornerInnerSize * 3 / 7;
        final int INNER_CIRCLE_OFFSET = cornerInnerSize * 2 / 7;
        //draw outer
        graphics.setColor(outer);
        if (type.equals("rect")) {
            graphics.fillRect(x, y, cornerInnerSize, cornerInnerSize);
        } else if (type.equals("point")) {
            graphics.fillOval(x, y, cornerInnerSize, cornerInnerSize);
        }
        //draw middle
        graphics.setColor(middle);
        if (type.equals("rect")) {
            graphics.fillRect(x + MIDDLE_CIRCLE_OFFSET, y + MIDDLE_CIRCLE_OFFSET, MIDDLE_CIRCLE_SIZE, MIDDLE_CIRCLE_SIZE);
        } else if (type.equals("point")) {
            graphics.fillOval(x + MIDDLE_CIRCLE_OFFSET, y + MIDDLE_CIRCLE_OFFSET, MIDDLE_CIRCLE_SIZE, MIDDLE_CIRCLE_SIZE);
        }
        //draw inner
        graphics.setColor(inner);
        if (type.equals("rect")) {
            graphics.fillRect(x + INNER_CIRCLE_OFFSET, y + INNER_CIRCLE_OFFSET, INNER_CIRCLE_SIZE, INNER_CIRCLE_SIZE);
        } else if (type.equals("point")) {
            graphics.fillOval(x + INNER_CIRCLE_OFFSET, y + INNER_CIRCLE_OFFSET, INNER_CIRCLE_SIZE, INNER_CIRCLE_SIZE);
        }
    }
}
