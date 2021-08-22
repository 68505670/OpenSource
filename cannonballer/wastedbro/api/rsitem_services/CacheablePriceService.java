package scripts.wastedbro.api.rsitem_services;

import lombok.Setter;
import org.tribot.api.General;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;

public abstract class CacheablePriceService<T> {

    private static final int CONNECTION_TIMEOUT = 5000;
    private static final int UPDATE_INTERVAL_MINUTES = 30;
    private static final int OUTDATED_HOURS = 24;

    protected final HashMap<Integer, T> dataCache;
    private final String url;
    private final String cacheFile;
    @Setter
    protected int outdatedHours;
    @Setter
    private int updateIntervalMinutes;

    protected CacheablePriceService(String url, String cacheFile) {
        this.dataCache = new HashMap<>();
        this.url = url;
        this.cacheFile = cacheFile;
        this.updateIntervalMinutes = UPDATE_INTERVAL_MINUTES;
        this.outdatedHours = OUTDATED_HOURS;
    }

    protected abstract void parseCache() throws IOException;

    protected boolean isCacheOlderThan(int minutes) {
        File cacheFile = new File(this.cacheFile);
        if (!cacheFile.exists() || cacheFile.length() == 0) {
            return true;
        }
        Date lastModified = new Date(cacheFile.lastModified());
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -minutes);
        Date updateTime = calendar.getTime();
        return lastModified.before(updateTime);
    }

    protected void updateCacheIfNecessary() {
        boolean cacheUpdate = isCacheOlderThan(updateIntervalMinutes);
        if (cacheUpdate) {
            try {
                saveUrl(cacheFile, url);
            } catch (Exception e) {
                General.println("Could not update prices data from url " + url);
            }
        }
        if (cacheUpdate || dataCache.isEmpty() && new File(cacheFile).exists()) {
            try {
                parseCache();
            } catch (Exception e) {
                General.println("Failed to update " + cacheFile + " cache: unable to parse cache file because " + e.getMessage());
            }
        }
    }

    protected Optional<T> tryGetItemData(int itemId) {
        updateCacheIfNecessary();
        return isCacheOlderThan(outdatedHours)
                ? Optional.empty()
                : Optional.ofNullable(dataCache.get(itemId));
    }

    /**
     * Saves a file from a URL into the specified file
     */
    private void saveUrl(String filename, String urlString) throws IOException {
        File file = new File(filename);
        if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
            return;
        }
        System.out.println(urlString);
        if (!file.exists() && !file.createNewFile()) {
            return;
        }
        URLConnection urlConnection = new URL(urlString).openConnection();
        urlConnection.setConnectTimeout(CONNECTION_TIMEOUT);
        try (BufferedInputStream in = new BufferedInputStream(urlConnection.getInputStream());
             FileOutputStream fout = new FileOutputStream(file)) {
            byte[] data = new byte[1024];
            int count;
            while ((count = in.read(data, 0, 1024)) != -1) {
                fout.write(data, 0, count);
            }
        }
    }

}
