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
        Downloader downloader = new Downloader(new URL("https://maijian-prod.oss-cn-beijing.aliyuncs.com/XYD/201807/PaymentDetails.csv"), 5);
        File download = downloader.download();
    }

}
