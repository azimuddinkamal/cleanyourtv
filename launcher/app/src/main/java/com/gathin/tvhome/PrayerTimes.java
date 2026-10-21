package com.gathin.tvhome;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * JAKIM e-Solat integration with automatic location detection (IP-based geolocation)
 * and intelligent daily caching. Generates full prayer widget data for all daily prayers.
 */
public final class PrayerTimes {
    private static final String TAG = "TvHomePrayerTimes";
    private static final String PREFS_NAME = "tvhome_prayer_prefs";
    private static final String KEY_CACHE_JSON = "prayer_cache_json";
    private static final String KEY_CACHE_MONTH = "prayer_cache_month";
    private static final String KEY_CACHE_ZONE = "prayer_cache_zone";
    private static final String KEY_CACHE_CITY = "prayer_cache_city";

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    public static final class WidgetData {
        public final String location;
        public final String nextPrayerSummary;
        public final int nextIndex; // 0=Subuh, 1=Syuruk, 2=Zohor, 3=Asar, 4=Maghrib, 5=Isyak
        public final String subuh;
        public final String syuruk;
        public final String zohor;
        public final String asar;
        public final String maghrib;
        public final String isyak;

        public WidgetData(String location, String nextPrayerSummary, int nextIndex,
                          String subuh, String syuruk, String zohor,
                          String asar, String maghrib, String isyak) {
            this.location = location;
            this.nextPrayerSummary = nextPrayerSummary;
            this.nextIndex = nextIndex;
            this.subuh = subuh;
            this.syuruk = syuruk;
            this.zohor = zohor;
            this.asar = asar;
            this.maghrib = maghrib;
            this.isyak = isyak;
        }
    }

    public interface Listener {
        void onPrayerTimesUpdated(WidgetData data);
    }

    private PrayerTimes() {
    }

    public static void update(Context context, Listener listener) {
        if (context == null) return;
        final Context appCtx = context.getApplicationContext();

        // 1. Immediately provide cached data if available
        WidgetData cached = getCachedData(appCtx);
        if (cached != null && listener != null) {
            listener.onPrayerTimesUpdated(cached);
        }

        // 2. Refresh in background if needed (once a day or if cache is stale)
        EXECUTOR.execute(() -> {
            try {
                Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
                int currentMonth = cal.get(Calendar.MONTH) + 1;
                SharedPreferences prefs = appCtx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                int cachedMonth = prefs.getInt(KEY_CACHE_MONTH, -1);
                String cachedJson = prefs.getString(KEY_CACHE_JSON, null);

                if (cachedJson == null || cachedMonth != currentMonth) {
                    refreshFromNetwork(appCtx, listener);
                } else {
                    WidgetData updated = getCachedData(appCtx);
                    if (updated != null && listener != null) {
                        MAIN_HANDLER.post(() -> listener.onPrayerTimesUpdated(updated));
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "Error checking prayer cache: " + e.getMessage());
            }
        });
    }

    private static void refreshFromNetwork(Context appCtx, Listener listener) {
        try {
            // Step 1: Detect location via IP Geolocation
            double lat = 3.0833;
            double lon = 101.5833;
            String city = "Selangor";

            try {
                String geoJson = fetchUrl("https://ipwho.is/");
                JSONObject geo = new JSONObject(geoJson);
                if (geo.optBoolean("success", false)) {
                    lat = geo.optDouble("latitude", lat);
                    lon = geo.optDouble("longitude", lon);
                    city = geo.optString("city", geo.optString("region", city));
                }
            } catch (Exception e) {
                Log.w(TAG, "IP Geolocation fallback: " + e.getMessage());
            }

            // Step 2: Query Waktu Solat JAKIM GPS API
            String apiUrl = String.format(Locale.US, "https://api.waktusolat.app/v2/solat/gps/%.4f/%.4f", lat, lon);
            String prayerJson = fetchUrl(apiUrl);
            JSONObject data = new JSONObject(prayerJson);
            String zone = data.optString("zone", "SGR01");
            int monthNumber = data.optInt("month_number", Calendar.getInstance(TimeZone.getTimeZone("GMT+8")).get(Calendar.MONTH) + 1);

            // Step 3: Save to SharedPreferences
            SharedPreferences prefs = appCtx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            prefs.edit()
                    .putString(KEY_CACHE_JSON, prayerJson)
                    .putInt(KEY_CACHE_MONTH, monthNumber)
                    .putString(KEY_CACHE_ZONE, zone)
                    .putString(KEY_CACHE_CITY, city)
                    .apply();

            WidgetData widgetData = parseWidgetData(data, zone, city);
            if (widgetData != null && listener != null) {
                MAIN_HANDLER.post(() -> listener.onPrayerTimesUpdated(widgetData));
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to refresh prayer times: " + e.getMessage());
        }
    }

    private static WidgetData getCachedData(Context appCtx) {
        SharedPreferences prefs = appCtx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String cachedJson = prefs.getString(KEY_CACHE_JSON, null);
        String zone = prefs.getString(KEY_CACHE_ZONE, "SGR01");
        String city = prefs.getString(KEY_CACHE_CITY, "Selangor");
        if (cachedJson == null) return null;
        try {
            JSONObject data = new JSONObject(cachedJson);
            return parseWidgetData(data, zone, city);
        } catch (Exception e) {
            return null;
        }
    }

    private static WidgetData parseWidgetData(JSONObject data, String zone, String city) {
        try {
            JSONArray prayers = data.optJSONArray("prayers");
            if (prayers == null || prayers.length() == 0) return null;

            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
            int todayDay = cal.get(Calendar.DAY_OF_MONTH);
            long nowSec = cal.getTimeInMillis() / 1000L;

            JSONObject todayEntry = null;
            JSONObject tomorrowEntry = null;

            for (int i = 0; i < prayers.length(); i++) {
                JSONObject item = prayers.getJSONObject(i);
                int d = item.optInt("day", -1);
                if (d == todayDay) {
                    todayEntry = item;
                    if (i + 1 < prayers.length()) {
                        tomorrowEntry = prayers.getJSONObject(i + 1);
                    }
                    break;
                }
            }

            if (todayEntry == null) return null;

            long fajr = todayEntry.optLong("fajr");
            long syuruk = todayEntry.optLong("syuruk");
            long dhuhr = todayEntry.optLong("dhuhr");
            long asr = todayEntry.optLong("asr");
            long maghrib = todayEntry.optLong("maghrib");
            long isha = todayEntry.optLong("isha");

            String nextName;
            long nextSec;
            int nextIndex;

            if (nowSec < fajr) {
                nextName = "Subuh";
                nextSec = fajr;
                nextIndex = 0;
            } else if (nowSec < syuruk) {
                nextName = "Syuruk";
                nextSec = syuruk;
                nextIndex = 1;
            } else if (nowSec < dhuhr) {
                nextName = "Zohor";
                nextSec = dhuhr;
                nextIndex = 2;
            } else if (nowSec < asr) {
                nextName = "Asar";
                nextSec = asr;
                nextIndex = 3;
            } else if (nowSec < maghrib) {
                nextName = "Maghrib";
                nextSec = maghrib;
                nextIndex = 4;
            } else if (nowSec < isha) {
                nextName = "Isyak";
                nextSec = isha;
                nextIndex = 5;
            } else {
                nextName = "Subuh";
                nextSec = (tomorrowEntry != null) ? tomorrowEntry.optLong("fajr") : (fajr + 86400L);
                nextIndex = 0;
            }

            SimpleDateFormat timeFmt = new SimpleDateFormat("h:mm a", Locale.US);
            timeFmt.setTimeZone(TimeZone.getTimeZone("GMT+8"));

            String subuhStr = timeFmt.format(new Date(fajr * 1000L));
            String syurukStr = timeFmt.format(new Date(syuruk * 1000L));
            String zohorStr = timeFmt.format(new Date(dhuhr * 1000L));
            String asarStr = timeFmt.format(new Date(asr * 1000L));
            String maghribStr = timeFmt.format(new Date(maghrib * 1000L));
            String isyakStr = timeFmt.format(new Date(isha * 1000L));

            String nextTimeStr = timeFmt.format(new Date(nextSec * 1000L));
            String nextSummary = "NEXT: " + nextName.toUpperCase(Locale.US) + " " + nextTimeStr;
            String locationStr = city.toUpperCase(Locale.US) + " · " + zone.toUpperCase(Locale.US);

            return new WidgetData(
                    locationStr,
                    nextSummary,
                    nextIndex,
                    subuhStr,
                    syurukStr,
                    zohorStr,
                    asarStr,
                    maghribStr,
                    isyakStr
            );
        } catch (Exception e) {
            Log.w(TAG, "parseWidgetData error: " + e.getMessage());
            return null;
        }
    }

    private static String fetchUrl(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(6000);
        conn.setReadTimeout(6000);
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 CleanYourTV-Android");
        try {
            int code = conn.getResponseCode();
            if (code != 200) {
                throw new RuntimeException("HTTP " + code + " from " + urlString);
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
            reader.close();
            return sb.toString();
        } finally {
            conn.disconnect();
        }
    }
}
