package com.gathin.tvhome;

import java.util.HashMap;
import java.util.Map;

/**
 * Curated content showcase: rich titles, taglines, and descriptions for major streaming
 * and local Malaysian apps, transforming the empty middle area into an Apple TV-style
 * cinematic content preview.
 */
public final class ShowcaseContent {

    public static final class Item {
        public final String tagline;
        public final String description;
        public final int accentColor;

        public Item(String tagline, String description, int accentColor) {
            this.tagline = tagline;
            this.description = description;
            this.accentColor = accentColor;
        }
    }

    private static final Map<String, Item> ITEMS = new HashMap<>();

    static {
        ITEMS.put("com.google.android.youtube.tv", new Item(
                "Trending & Video Pilihan Hari Ini",
                "Tonton video muzik terkini, podcast, ulasan gajet, dan siaran langsung komuniti di YouTube.",
                0xFFE50914
        ));

        ITEMS.put("ott.durioo1", new Item(
                "Selamat & Mendidik Jiwa Anak",
                "Mina Mila, Little Ammar, Abata, Polisi Patrol — animasi Islamik & ilmiah kegemaran keluarga.",
                0xFFF59E0B
        ));

        ITEMS.put("com.netflix.ninja", new Item(
                "Originals & Blockbuster Dunia",
                "Terokai ribuan filem antarabangsa, K-Drama hangat, anime terhebat, dan siri dokumentari eksklusif.",
                0xFFE50914
        ));

        ITEMS.put("in.startv.hotstar.dplus.tv", new Item(
                "Marvel, Star Wars, Pixar & Disney",
                "Koleksi filem pawagam, aksi adiwira terhebat, dan animasi fantasi untuk tontonan seisi keluarga.",
                0xFF0063E5
        ));

        ITEMS.put("com.apple.atve.androidtv.appletv", new Item(
                "Apple Originals Berprestij",
                "Karya drama bersiri dan filem pemenang anugerah antarabangsa dalam kualiti 4K HDR yang memukau.",
                0xFF94A3B8
        ));

        ITEMS.put("com.astro.sott", new Item(
                "Sukan Langsung & Drama Melayu",
                "Siaran langsung sukan EPL, Liga Malaysia, badminton dunia, dan drama Melayu terkini di Sooka & Astro.",
                0xFFEC4899
        ));

        ITEMS.put("com.aliftech.tvmalaysia", new Item(
                "Saluran Televisyen Malaysia",
                "Tonton siaran langsung saluran televisyen utama Malaysia secara percuma, bila-bila masa.",
                0xFF3B82F6
        ));

        ITEMS.put("com.wbd.stream", new Item(
                "HBO, Warner Bros & DC Universe",
                "Filem pawagam terhangat, siri fenomena dunia, dan alam sinematik adiwira DC terunggul.",
                0xFF6366F1
        ));

        ITEMS.put("com.iqiyi.i18n.tv", new Item(
                "Drama Asia & Anime Pilihan",
                "Siri drama China, drama Korea popular, dan anime terhangat dengan sari kata Bahasa Melayu rasmi.",
                0xFF10B981
        ));
    }

    private ShowcaseContent() {
    }

    public static Item get(String packageName) {
        if (packageName == null) return null;
        return ITEMS.get(packageName);
    }
}
