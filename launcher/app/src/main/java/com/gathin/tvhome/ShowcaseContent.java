package com.gathin.tvhome;

import java.util.HashMap;
import java.util.Map;

/**
 * Curated content showcase: premium taglines and descriptions for streaming
 * and TV apps, delivering an Apple TV-grade cinematic content preview.
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
        // YouTube
        ITEMS.put("com.google.android.youtube.tv", new Item(
                "Stream Videos, Music & Live Creators",
                "Explore trending videos, live gaming, podcasts, and personalized recommendations from global creators.",
                0xFFEF4444
        ));

        // Durioo+
        ITEMS.put("ott.durioo1", new Item(
                "Safe Islamic & Educational Shows for Kids",
                "High-quality cartoons, inspiring values, and fun learning series designed for the whole family.",
                0xFFF59E0B
        ));

        // Netflix
        ITEMS.put("com.netflix.ninja", new Item(
                "Unlimited Movies, TV Shows & Originals",
                "Stream award-winning Netflix originals, Hollywood blockbusters, trending series, and documentaries.",
                0xFFE50914
        ));

        // Disney+ Hotstar
        ITEMS.put("in.startv.hotstar.dplus.tv", new Item(
                "Disney, Marvel, Star Wars & Blockbusters",
                "Experience cinematic superhero adventures, timeless family classics, and hit Asian entertainment.",
                0xFF00D2D3
        ));

        // Apple TV
        ITEMS.put("com.apple.atve.androidtv.appletv", new Item(
                "Award-Winning Apple Original Stories",
                "Stream critically acclaimed dramas, hit comedies, and groundbreaking films in 4K HDR.",
                0xFFA855F7
        ));

        // Sooka / Astro GO
        ITEMS.put("com.astro.sott", new Item(
                "Live Sports & Asian Entertainment",
                "Watch live Premier League matches, local blockbuster dramas, and hit variety shows.",
                0xFFEC4899
        ));
        ITEMS.put("com.astro.astrogo", new Item(
                "Live Sports & Asian Entertainment",
                "Watch live Premier League matches, local blockbuster dramas, and hit variety shows.",
                0xFFEC4899
        ));
        ITEMS.put("my.com.astro.go", new Item(
                "Live Sports & Asian Entertainment",
                "Watch live Premier League matches, local blockbuster dramas, and hit variety shows.",
                0xFFEC4899
        ));

        // TV Malaysia
        ITEMS.put("com.aliftech.tvmalaysia", new Item(
                "Live National Channels & News",
                "Stream live Malaysian public broadcasts, daily news, and entertainment channels.",
                0xFF10B981
        ));
        ITEMS.put("com.ratus.tvmalaysia", new Item(
                "Live National Channels & News",
                "Stream live Malaysian public broadcasts, daily news, and entertainment channels.",
                0xFF10B981
        ));

        // Max / HBO GO
        ITEMS.put("com.wbd.stream", new Item(
                "Iconic Movies, HBO Series & DC Universe",
                "Stream the latest Warner Bros. films, acclaimed HBO series, and iconic DC hero franchises.",
                0xFF3B82F6
        ));
        ITEMS.put("com.hbo.hbonow", new Item(
                "Iconic Movies, HBO Series & DC Universe",
                "Stream the latest Warner Bros. films, acclaimed HBO series, and iconic DC hero franchises.",
                0xFF3B82F6
        ));

        // iQIYI
        ITEMS.put("com.iqiyi.i18n.tv", new Item(
                "Popular Asian Dramas & Anime",
                "Enjoy trending Asian dramas, popular anime series, and exclusive variety shows.",
                0xFF22C55E
        ));

        // Prime Video
        ITEMS.put("com.amazon.amazonvideo.livingroom", new Item(
                "Amazon Originals & Popular Cinema",
                "Watch exclusive Amazon Originals, popular Hollywood hits, and award-winning series.",
                0xFF00A8E1
        ));

        // Spotify
        ITEMS.put("com.spotify.tv.android", new Item(
                "Millions of Songs & Curated Podcasts",
                "Listen to your favorite artists, curated playlists, and trending podcasts on the big screen.",
                0xFF1DB954
        ));

        // SmartTube
        ITEMS.put("com.teamsmart.videomanager.tv", new Item(
                "Ad-Free Video Streaming & Podcasts",
                "Advanced community video player with sponsor skipping and high-bitrate playback.",
                0xFFFF0000
        ));
    }

    private ShowcaseContent() {
    }

    public static Item get(String packageName) {
        if (packageName == null) return null;
        Item item = ITEMS.get(packageName);
        if (item != null) return item;
        return new Item(
                "Ready to Launch",
                "Press OK on your remote to launch and explore this application.",
                0xFF818CF8
        );
    }
}
