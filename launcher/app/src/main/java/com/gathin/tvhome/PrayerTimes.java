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
 * and intelligent daily caching.
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

    public interface Listener {
        void onPrayerTimesUpdated(String displayText);
    }

    private PrayerTimes() {
    }

    public static void update(Context context, Listener listener) {
        if (context == null) return;
        final Context appCtx = context.getApplicationContext();

        // 1. Immediately provide cached data if available
        String cachedDisplay = getCachedDisplay(appCtx);
        if (cachedDisplay != null && listener != null) {
            listener.onPrayerTimesUpdated(cachedDisplay);
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
                    String updated = getCachedDisplay(appCtx);
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

            String display = formatDisplay(data, zone, city);
            if (display != null && listener != null) {
                MAIN_HANDLER.post(() -> listener.onPrayerTimesUpdated(display));
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to refresh prayer times: " + e.getMessage());
        }
    }

    private static String getCachedDisplay(Context appCtx) {
        SharedPreferences prefs = appCtx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String cachedJson = prefs.getString(KEY_CACHE_JSON, null);
        String zone = prefs.getString(KEY_CACHE_ZONE, "SGR01");
        String city = prefs.getString(KEY_CACHE_CITY, "Selangor");
        if (cachedJson == null) return null;
        try {
            JSONObject data = new JSONObject(cachedJson);
            return formatDisplay(data, zone, city);
        } catch (Exception e) {
            return null;
        }
    }

    private static String formatDisplay(JSONObject data, String zone, String city) {
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

            if (nowSec < fajr) {
                nextName = "Subuh";
                nextSec = fajr;
            } else if (nowSec < syuruk) {
                nextName = "Syuruk";
                nextSec = syuruk;
            } else if (nowSec < dhuhr) {
                nextName = "Zohor";
                nextSec = dhuhr;
            } else if (nowSec < asr) {
                nextName = "Asar";
                nextSec = asr;
            } else if (nowSec < maghrib) {
                nextName = "Maghrib";
                nextSec = maghrib;
            } else if (nowSec < isha) {
                nextName = "Isyak";
                nextSec = isha;
            } else {
                nextName = "Subuh";
                nextSec = (tomorrowEntry != null) ? tomorrowEntry.optLong("fajr") : (fajr + 86400L);
            }

            SimpleDateFormat timeFmt = new SimpleDateFormat("hh:mm a", Locale.US);
            timeFmt.setTimeZone(TimeZone.getTimeZone("GMT+8"));
            String timeStr = timeFmt.format(new Date(nextSec * 1000L));

            return String.format(Locale.US, "🕌 %s %s · %s (%s)", nextName, timeStr, zone, city);
        } catch (Exception e) {
            Log.w(TAG, "formatDisplay error: " + e.getMessage());
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
