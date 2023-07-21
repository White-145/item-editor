package me.white.itemeditor.util;

public class ColorUtil {
    public static int mean(int[] colors) {
        int red = 0;
        int green = 0;
        int blue = 0;
        for (int color : colors) {
            red += (color & 0xFF0000) >> 16;
            green += (color & 0x00FF00) >> 8;
            blue += (color & 0x0000FF);
        }
        return ((red / colors.length) << 16) + ((green / colors.length) << 8) + blue / colors.length;
    }

    public static String format(int color) {
        return String.format("#%06X", (0xFFFFFF & color));
    }
}
