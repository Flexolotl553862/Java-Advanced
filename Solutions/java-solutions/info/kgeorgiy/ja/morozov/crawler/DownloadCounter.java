package info.kgeorgiy.ja.morozov.crawler;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Custom class to control downloading pages.
 */
public class DownloadCounter {

    private static class HostInfo {
        public int downloads = 0;
        public Queue<Map.Entry<String, Integer>> waitingUrls = new ConcurrentLinkedQueue<>();
    }

    private final Map<String, HostInfo> hostInfo = new ConcurrentHashMap<>();
    private final int perHost;

    /**
     * Constructor with one argument.
     *
     * @param perHost maximal number of pages that downloads from one host at the same time.
     */
    public DownloadCounter(int perHost) {
        this.perHost = perHost;
    }

    /**
     * Check that number of downloading pages from given host < perHost.
     *
     * @param url   current url.
     * @param host  current host.
     * @param depth depth of given url.
     * @return true if limit for this host < perHost or false else.
     */
    public boolean tryDownload(String url, String host, int depth) {
        // :NOTE: atomic?
        AtomicBoolean ok = new AtomicBoolean(true);
        hostInfo.compute(host, (k, v) -> {
            if (v == null) {
                v = new HostInfo();
            }
            if (v.downloads >= perHost) {
                v.waitingUrls.add(new AbstractMap.SimpleEntry<>(url, depth));
                ok.set(false);
            } else {
                v.downloads++;
            }
            return v;
        });
        return ok.get();
    }

    /**
     * Decrement number of downloads for given host.
     *
     * @param host current host.
     */
    public void finishDownload(String host) {
        hostInfo.compute(host, (k, v) -> {
            if (v == null) {
                v = new HostInfo();
            }
            if (v.downloads > 0) {
                v.downloads--;
            }
            if (v.waitingUrls.isEmpty() && v.downloads == 0) {
                return null;
            }
            return v;
        });
    }

    /**
     * Extract the first waiting task from queue.
     *
     * @param host current host.
     * @return Pair containing url and depth.
     */
    public Map.Entry<String, Integer> getNextUrl(final String host) {
        AtomicReference<Map.Entry<String, Integer>> next = new AtomicReference<>();
        hostInfo.compute(host, (k, v) -> {
            if (v == null) {
                v = new HostInfo();
            }
            next.set(v.waitingUrls.poll());
            if (v.waitingUrls.isEmpty() && v.downloads == 0) {
                return null;
            }
            return v;
        });
        return next.get();
    }
}