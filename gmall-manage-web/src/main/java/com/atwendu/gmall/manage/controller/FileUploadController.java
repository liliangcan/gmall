package com.atwendu.gmall.manage.controller;

import org.apache.commons.lang3.StringUtils;
import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@CrossOrigin
public class FileUploadController {

//    String ip = "http://192.168.253.219";//硬编码
    //服务器的IP地址作为一个配置文件放入项目中，软编码

    //@Value 注解使用的前提条件是，当前类必须在spring 容器中！
    @Value("${fileServer.url}")
    private String fileUrl; //fileUrl = http://192.168.253.219

    //获取上传文件，需要使用springmvc 技术中
    @RequestMapping("fileUpload")
    public String fileUpload(@RequestParam("file") MultipartFile file) throws IOException, MyException {

        String imgUrl = fileUrl;
        //当文件不为空时上传
        if(file != null) {
            String sonfigFile = this.getClass().getResource("/tracker.conf").getFile();
            ClientGlobal.init(sonfigFile);
            TrackerClient trackerClient=new TrackerClient();
            //获取连接
            TrackerServer trackerServer=trackerClient.getTrackerServer();
            StorageClient storageClient=new StorageClient(trackerServer,null);

            //获取上传文件名称
            String originalFilename = file.getOriginalFilename();
            //获取文件的后缀名
            String extName = StringUtils.substringAfterLast(originalFilename, ".");
//            String originalFilename="C://Users//86136//Documents//fdfs//001.jpg";
            //上传图片
//            String[] upload_file = storageClient.upload_file(originalFilename, extName, null); //获取本地文件
            String[] upload_file = storageClient.upload_file(file.getBytes(), extName, null);
            for (int i = 0; i < upload_file.length; i++) {
                String path = upload_file[i];
                System.out.println("s = " + path);

                imgUrl += "/" + path;
            }
        }

//        return "http://192.168.253.219/group1/M00/00/00/wKj922JIgtuAPQOIAAAl_GXv6Z4453.jpg";
        return imgUrl;
    }
}
