package indiv.inch.qrcodegenerate;

import java.awt.*;

/**
 * this is an util class of color
 */
public class ColorUtil {
    /**
     * get color by a rbga string like #FF0000FF
     * @param rgb rbga string eg: #FF0000FF means red with 1.0 alpha
     * @return Color represent this color
     */
    public static Color getColor(String rgb) {
        if (rgb == null) {
            return null;
        }
        //if no alpha set then set alpha to 1.0
        if (rgb.length() == 7) {
            rgb = rgb + "FF";
        } else if (rgb.length() != 9) { //if string length is not right
            return null;
        }
        return new Color(
                Integer.parseInt(rgb.substring(1,3),16),
                Integer.parseInt(rgb.substring(3,5),16),
                Integer.parseInt(rgb.substring(5,7),16),
                Integer.parseInt(rgb.substring(7,9),16));
    }
}
