package de.sjsolutions.pipay.util;

public enum Rank {
    NONE(0.0, ""),
    BRONZE(100.0, new String(new int[]{128526}, 0, 1)), //sunglasses
    SILVER(200.0, new String(new int[]{11088}, 0, 1)), //star
    GOLD(500.0, new String(new int[]{127942}, 0, 1)), //trophy
    ADMIN(Double.POSITIVE_INFINITY, new String(new int[]{128176}, 0, 1)); //money bag

    public static final String EMOJIS;
    public static final int COUNT = values().length;

    static {
        StringBuilder sb = new StringBuilder();
        for (Rank r : values()) {
            sb.append(r.symbol);
        }
        EMOJIS = sb.toString();
    }

    public final double threshold;
    public final String symbol;

    Rank(double threshold, String symbol) {
        this.threshold = threshold;
        this.symbol = symbol;
    }

    public String formatUsername(String username) {
        return symbol + username.replaceAll("[" + EMOJIS + "]", "");
    }

    public static Rank forAmount(double amount) {
        for (int i = COUNT - 1; i >= 0; i--) {
            if (amount >= values()[i].threshold) {
                return values()[i];
            }
        }
        return NONE;
    }

    public static Rank fromUsername(String username) {
        for (int i = COUNT - 1; i >= 0; i--) {
            if (username.startsWith(values()[i].symbol)) {
                return values()[i];
            }
        }
        return NONE;
    }
}
