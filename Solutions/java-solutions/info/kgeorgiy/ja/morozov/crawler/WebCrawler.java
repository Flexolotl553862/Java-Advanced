package info.kgeorgiy.ja.morozov.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.*;

/**
 * This class is implementation of {@link Crawler} and {@link NewCrawler}.
 * You can use this for parallel searching of sites.
 */
public class WebCrawler implements Crawler, NewCrawler {

    private final ExecutorService downloads;
    private final ExecutorService extractors;
    private final DownloadCounter downloadCounter;

    private final Downloader downloader;

    /**
     * This constructor creates a class with given characteristics.
     *
     * @param downloader  downloader that will be used to download pages.
     * @param downloaders maximal number of threads that downloads pages.
     * @param extractors  maximal number of threads that extracts links.
     * @param perHost     maximal number of pages that downloads from one host at the same time.
     */
    public WebCrawler(final Downloader downloader, final int downloaders, final int extractors, final int perHost) {
        this.downloader = downloader;
        this.downloads = Executors.newFixedThreadPool(downloaders);
        this.extractors = Executors.newFixedThreadPool(extractors);
        this.downloadCounter = new DownloadCounter(perHost);
    }

    private void extractLinks(
            final String url, final Map<String, IOException> errors, final Phaser phaser,
            final Set<String> visited, final Document document, final Queue<String> nextLevel) {
        phaser.register();
        extractors.execute(() -> {
            try {
                for (final String link : document.extractLinks()) {
                    if (!visited.contains(link)) {
                        nextLevel.add(link);
                    }
                }
            } catch (final IOException e) {
                errors.put(url, e);
                // :NOTE: copy-paste
            } finally {
                phaser.arriveAndDeregister();
            }
        });
    }

    // :NOTE: arg object
    private void downloadByUrl(
            final String url, final Queue<String> urls, final int depth, final Map<String, IOException> errors,
            final Phaser phaser, final Set<String> visited, final String host, final Queue<String> nextLevel) {
        final Document document;
        try {
            document = downloader.download(url);
        } catch (final IOException e) {
            errors.put(url, e);
            // :NOTE: no others
            return;
        }
        urls.add(url);
        if (depth > 1) {
            extractLinks(url, errors, phaser, visited, document, nextLevel);
        }
        final Map.Entry<String, Integer> next = downloadCounter.getNextUrl(host);
        if (next != null) {
            downloadByUrl(next.getKey(), urls, next.getValue(), errors, phaser, visited, host, nextLevel);
        }
    }

    private void download(
            final String url, final Queue<String> urls, final int depth, final Map<String, IOException> errors,
            final Phaser phaser, final List<String> excludes, final Set<String> visited, final Queue<String> nextLevel) {
        // :NOTE: ??
        if (depth == 0) {
            return;
        }
        final String host;
        try {
            // :NOTE: too late
            host = URLUtils.getHost(url);
            for (final String exclude : excludes) {
                if (host.contains(exclude)) {
                    return;
                }
            }
        } catch (final MalformedURLException ignored) {
            return;
        }
        phaser.register();
        downloads.execute(() -> {
            if (visited.add(url)) { // :NOTE: ??
                if (!downloadCounter.tryDownload(url, host, depth)) {
                    phaser.arriveAndDeregister();
                    return;
                }
                downloadByUrl(url, urls, depth, errors, phaser, visited, host, nextLevel);
                downloadCounter.finishDownload(host);
            }
            // :NOTE: finally
            phaser.arriveAndDeregister();
        });
    }

    /**
     * {@inheritDoc}
     */
    public Result download(final String url, int depth, final List<String> excludes) {
        final Map<String, IOException> errors = new ConcurrentHashMap<>();
        // :NOTE: newKeySet
        final Set<String> visited = ConcurrentHashMap.newKeySet();
        final Queue<String> urls = new ConcurrentLinkedQueue<>();

        Queue<String> currentLevel = new ConcurrentLinkedQueue<>();
        currentLevel.add(url);

        // :NOTE: for
        while (depth > 0) {
            final Phaser phaser = new Phaser(1);
            final Queue<String> nextLevel = new ConcurrentLinkedQueue<>();
            for (final String nextUrl : currentLevel) {
                download(nextUrl, urls, depth, errors, phaser, excludes, visited, nextLevel);
            }
            phaser.arriveAndAwaitAdvance();
            currentLevel = nextLevel;
            depth--;
        }

        return new Result(urls.stream().toList(), errors);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Result download(final String url, final int depth) {
        return download(url, depth, List.of());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        downloads.shutdown();
        extractors.shutdown();
        try {
            downloads.awaitTermination(1000, TimeUnit.MILLISECONDS);
            extractors.awaitTermination(1000, TimeUnit.MILLISECONDS);
        } catch (final InterruptedException ignored) {
            downloads.shutdownNow();
            extractors.shutdownNow();
        }
    }

    /**
     * Entry point for this class.
     *
     * @param args expected five arguments
     */
    public static void main(final String[] args) {
        if (args.length != 5) {
            System.out.println("Expected 5 arguments, but got " + args.length);
            return;
        }

        final int[] integerArgs = new int[4];
        for (int i = 0; i < integerArgs.length; i++) {
            try {
                integerArgs[i] = Integer.parseInt(args[i + 1]);
            } catch (final NumberFormatException ignored) {
                System.out.println("Argument " + i + 1 + " is not a number");
                return;
            }
        }

        try (final WebCrawler webCrawler = new WebCrawler(
                new CachingDownloader(1),
                integerArgs[1],
                integerArgs[2],
                integerArgs[3])) {
            final Result result = webCrawler.download(args[0], integerArgs[0]);
            System.out.println();
            System.out.println("-------Downloaded successfully:-------");
            for (final String url : result.getDownloaded()) {
                System.out.println(url);
            }
            System.out.println("-------Failed to download:-------");
            for (final Map.Entry<String, IOException> entry : result.getErrors().entrySet()) {
                System.out.println(entry.getKey() + " reason: " + entry.getValue().getMessage());
            }
        } catch (final IOException e) {
            System.out.println("Can't create caching downloader: " + e.getMessage());
        }
    }
}