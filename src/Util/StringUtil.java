package Util;

/**
 * Created by Khanh Nguyen on 3/5/2016.
 */
public class StringUtil {

    public static String replaceLast(String string, String from, String to) {
        int lastIndex = string.lastIndexOf(from);
        if (lastIndex < 0) return string;
        String tail = string.substring(lastIndex).replaceFirst(from, to);
        return string.substring(0, lastIndex) + tail;
    }

    public static String extractIndex(String source, String prefix, String suffix) {
        int index = 1;
        int prefixIndex = source.lastIndexOf(prefix);
        String tail = source.substring(prefixIndex+prefix.length());
        return tail.substring(0, tail.lastIndexOf(suffix));
    }

}
