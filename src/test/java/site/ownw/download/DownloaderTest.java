package site.ownw.download;

import org.junit.Test;

import java.io.File;
import java.net.URL;

/**
 * @author sofior
 * @date 2018/8/20 23:08
 */
public class DownloaderTest {


    @Test
    public void downloadTest() throws Exception {
        Downloader downloader = new Downloader(new URL("https://maijian-prod.oss-cn-beijing.aliyuncs.com/XYD/201807/PaymentDetails.csv"), 1);
        File download = downloader.download();
        downloader = new Downloader(new URL("https://maijian-prod.oss-cn-beijing.aliyuncs.com/XYD/201807/PaymentDetails.csv"), 2);
        download = downloader.download();
        downloader = new Downloader(new URL("https://maijian-prod.oss-cn-beijing.aliyuncs.com/XYD/201807/PaymentDetails.csv"), 4);
        download = downloader.download();
        downloader = new Downloader(new URL("https://maijian-prod.oss-cn-beijing.aliyuncs.com/XYD/201807/PaymentDetails.csv"), 8);
        download = downloader.download();
        downloader = new Downloader(new URL("https://maijian-prod.oss-cn-beijing.aliyuncs.com/XYD/201807/PaymentDetails.csv"), 16);
        download = downloader.download();
    }

}
