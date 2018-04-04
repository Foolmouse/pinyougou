package com.pinyougou.manager.controller;

import com.pinyougou.utils.FastDFSClient;
import entity.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class UploadController {

    @Value("${FILE_SERVER_URL}")
    private String FILE_SERVER_URL;//文件服务器地址

    @RequestMapping("/upload")
    public Result upload(MultipartFile file) throws Exception {


        //得到文件名和扩展名
        String filename = file.getOriginalFilename();
        String extName = filename.substring(filename.lastIndexOf(".") + 1);

        try {
            //得到fastDFSClient
            FastDFSClient fastDFSClient = new FastDFSClient("classpath:config/fdfs_client.conf");
            //上传文件 , 通过字节流 , 并指定扩展名
            String path = fastDFSClient.uploadFile(file.getBytes(), extName);
            //拼接url , 前端访问这个地址
            String url =  FILE_SERVER_URL + path;
            System.err.println(url);
            return new Result(true,url);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false , "上传失败");
        }
    }
}
