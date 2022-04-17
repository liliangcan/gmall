package com.atwendu.gmall.manage;

import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;

@SpringBootTest
class GmallManageWebApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    public void textFileUpload() throws IOException, MyException {
        String file = this.getClass().getResource("/tracker.conf").getFile();
        ClientGlobal.init(file);
        TrackerClient trackerClient=new TrackerClient();
        //获取连接
        TrackerServer trackerServer=trackerClient.getTrackerServer();
        StorageClient storageClient=new StorageClient(trackerServer,null);
        String orginalFilename="C://Users//86136//Documents//fdfs//001.jpg";
        //上传图片
        String[] upload_file = storageClient.upload_file(orginalFilename, "jpg", null);
        for (int i = 0; i < upload_file.length; i++) {
            String s = upload_file[i];
            System.out.println("s = " + s);
        }
    }


}
