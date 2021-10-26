package indiv.inch.qrcodegenerate;

import java.awt.*;

public class ColorUtil {
    public static Color getColor(String rgb) {
        if (rgb == null) {
            return null;
        }
        if (rgb.length() == 7) {
            rgb = rgb + "FF";
        } else if (rgb.length() != 9) {
            return null;
        }
        return new Color(
                Integer.parseInt(rgb.substring(1,3),16),
                Integer.parseInt(rgb.substring(3,5),16),
                Integer.parseInt(rgb.substring(5,7),16),
                Integer.parseInt(rgb.substring(7,9),16));
    }
}
