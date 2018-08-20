package site.ownw.download;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * @author sofior
 * @date 2018/8/20 22:37
 */
@Slf4j
public class DownloadFuture {

    private static final ThreadPoolExecutor EXECUTOR;

    static {
        int cpuNum = Runtime.getRuntime().availableProcessors();
        EXECUTOR = new ThreadPoolExecutor(cpuNum, cpuNum << 1, 5L, TimeUnit.MINUTES, new ArrayBlockingQueue<>(100), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "download-thread");
            }
        });
    }

    private FutureTask<Void> task;

    private Long start;
    private Long end;
    private URL url;
    private String error;
    private File toSave;
    private Long total;

    public DownloadFuture(File toSave, URL url, long start, long end, long total) {
        this.url = url;
        this.start = start;
        this.end = end;
        this.toSave = toSave;
        this.total = total;
    }

    public void start() {
        task = new FutureTask<>(() -> {
            try {
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                if (Objects.equals(end, total)) {
                    connection.addRequestProperty("Range", String.format("bytes=%d-", start));
                } else {
                    connection.addRequestProperty("Range", String.format("bytes=%d-%d", start, end));
                }
                log.info("Range={}", connection.getRequestProperty("Range"));
                connection.connect();
                if (connection.getResponseCode() != HttpURLConnection.HTTP_PARTIAL) {
                    throw new IllegalStateException("状态码异常(code=" + connection.getResponseCode() + ")");
                }
                log.info("状态码正常开始下载{}---{}", start, end);
                InputStream is = connection.getInputStream();
                int len;
                byte[] buf = new byte[1024];
                RandomAccessFile raf = new RandomAccessFile(toSave, "rw");
                raf.seek(start);
                while ((len = is.read(buf)) > 0) {
                    raf.write(buf, 0, len);
                }
                raf.close();
                is.close();
            } catch (Exception e) {
                this.error = e.getMessage();
            }
        }, null);
        EXECUTOR.execute(task);
    }

    public Boolean isDone() {
        return task.isDone();
    }

    public String getError() {
        return error;
    }
}
