package site.ownw.download;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
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

    private String error;

    public DownloadFuture(File toSave, URL url, long start, long end, long total) {
        this.task = new FutureTask<>(() -> {
            RandomAccessFile raf = null;
            InputStream is = null;
            try {
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                if (Objects.equals(end, total)) {
                    //部分服务器最后一个分段不能设置结尾byte值否则状态码和返回的inputStream都不对
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
                is = connection.getInputStream();
                int len;
                byte[] buf = new byte[1024];
                raf = new RandomAccessFile(toSave, "rw");
                raf.seek(start);
                while ((len = is.read(buf)) > 0) {
                    raf.write(buf, 0, len);
                }
            } catch (Exception e) {
                this.error = e.getMessage();
            } finally {
                closeStream(raf, is);
            }
        }, null);
    }

    public void run(){
        this.task.run();
    }

    public void start() {
        EXECUTOR.execute(task);
    }

    public Boolean isDone() {
        return task.isDone();
    }

    public String getError() {
        return error;
    }

    private void closeStream(Closeable... closeables) {
        for (Closeable closeable : closeables) {
            try {
                closeable.close();
            } catch (IOException e) {
                if (this.error.isEmpty()) {
                    this.error = e.getMessage();
                } else {
                    this.error = "," + e.getMessage();
                }
            }
        }
    }
}
