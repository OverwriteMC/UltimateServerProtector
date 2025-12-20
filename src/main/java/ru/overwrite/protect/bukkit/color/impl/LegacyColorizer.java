package ru.overwrite.protect.bukkit.color.impl;

import ru.overwrite.protect.bukkit.color.Colorizer;

public class LegacyColorizer implements Colorizer {

    private static final char COLOR_CHAR = '§';
    private static final char ALT_COLOR_CHAR = '&';

    private static final boolean[] HEX = new boolean[128];
    private static final boolean[] COLOR = new boolean[128];

    static {
        for (int c = '0'; c <= '9'; c++) {
            HEX[c] = true;
            COLOR[c] = true;
        }
        for (int c = 'a'; c <= 'f'; c++) {
            HEX[c] = true;
            COLOR[c] = true;
        }
        for (int c = 'A'; c <= 'F'; c++) {
            HEX[c] = true;
            COLOR[c] = true;
        }
        for (int c : new int[]{'r', 'R', 'k', 'K', 'l', 'L', 'm', 'M', 'n', 'N', 'o', 'O'}) {
            COLOR[c] = true;
        }
    }

    @Override
    public String colorize(String message) {
        if (message == null) {
            return null;
        }
        final char[] s = message.toCharArray();
        final int len = s.length;
        if (len == 0) {
            return message;
        }

        final int firstAmp = message.indexOf(ALT_COLOR_CHAR);
        if (firstAmp < 0) {
            return message;
        }

        final char[] d = new char[len * 2];
        int w = 0;
        int i = 0;

        if (firstAmp > 8) {
            System.arraycopy(s, 0, d, 0, firstAmp);
            w = firstAmp;
            i = firstAmp;
        } else {
            while (i < firstAmp) d[w++] = s[i++];
        }

        while (i < len) {
            final char c = s[i];

            if (c != ALT_COLOR_CHAR) {
                d[w++] = c;
                i++;
                continue;
            }

            final int remaining = len - i;

            if (remaining >= 8 && s[i + 1] == '#') {
                final char h0 = s[i + 2], h1 = s[i + 3], h2 = s[i + 4], h3 = s[i + 5], h4 = s[i + 6], h5 = s[i + 7];
                if (h0 < 128 && HEX[h0] && h1 < 128 && HEX[h1] && h2 < 128 && HEX[h2] && h3 < 128 && HEX[h3] && h4 < 128 && HEX[h4] && h5 < 128 && HEX[h5]) {
                    d[w] = COLOR_CHAR;
                    d[w + 1] = 'x';
                    d[w + 2] = COLOR_CHAR;
                    d[w + 3] = h0;
                    d[w + 4] = COLOR_CHAR;
                    d[w + 5] = h1;
                    d[w + 6] = COLOR_CHAR;
                    d[w + 7] = h2;
                    d[w + 8] = COLOR_CHAR;
                    d[w + 9] = h3;
                    d[w + 10] = COLOR_CHAR;
                    d[w + 11] = h4;
                    d[w + 12] = COLOR_CHAR;
                    d[w + 13] = h5;
                    w += 14;
                    i += 8;
                    continue;
                }
            }

            if (remaining >= 14 && (s[i + 1] == 'x' || s[i + 1] == 'X')) {
                boolean isHex = true;
                for (int k = 0; k < 6; k++) {
                    if (s[i + 2 + (k * 2)] != ALT_COLOR_CHAR) {
                        isHex = false;
                        break;
                    }
                    char hexChar = s[i + 3 + (k * 2)];
                    if (hexChar >= 128 || !HEX[hexChar]) {
                        isHex = false;
                        break;
                    }
                }

                if (isHex) {
                    d[w++] = COLOR_CHAR;
                    d[w++] = 'x';
                    for (int k = 0; k < 6; k++) {
                        d[w++] = COLOR_CHAR;
                        d[w++] = s[i + 3 + (k * 2)];
                    }
                    i += 14;
                    continue;
                }
            }

            if (remaining >= 2) {
                final char next = s[i + 1];
                if (next < 128 && COLOR[next]) {
                    d[w] = COLOR_CHAR;
                    d[w + 1] = next;
                    w += 2;
                    i += 2;
                    continue;
                }
            }

            d[w++] = ALT_COLOR_CHAR;
            i++;
        }

        return new String(d, 0, w);
    }
}