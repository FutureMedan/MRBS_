package com.future.MRBS.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.*;

@Service public class AmazonClientService {

    private AmazonS3 s3Client;

    @Value("${amazonProperties.endpointUrl}") private String endpointUrl;
    @Value("${amazonProperties.bucketName}") private String bucketName;
    @Value("${amazonProperties.accessKey}") private String accessKey;
    @Value("${amazonProperties.secretKey}") private String secretKey;

    @PostConstruct private void initializeAmazon() {
        BasicAWSCredentials creds = new BasicAWSCredentials(this.accessKey, this.secretKey);
        this.s3Client = AmazonS3ClientBuilder.standard().withRegion(Regions.AP_SOUTHEAST_1)
            .withCredentials(new AWSStaticCredentialsProvider(creds)).build();
    }

    public String uploadFile(MultipartFile multipartFile, String prefixName) {
        String fileUrl = "";
        try {
            File file = convertMultiPartToFile(multipartFile);
            String fileName = generateFileName(multipartFile, prefixName);
            fileUrl = endpointUrl + "/" + bucketName + "/" + fileName;
            uploadFileTos3bucket(fileName, file);
            if (file != null) {
                file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileUrl;
    }

    private File convertMultiPartToFile(MultipartFile multipartFile) throws IOException {
        if (multipartFile.getOriginalFilename() != null) {
            File file = new File(multipartFile.getOriginalFilename());
            FileOutputStream fos = new FileOutputStream(file);
            ByteArrayInputStream bis = new ByteArrayInputStream(multipartFile.getBytes());
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            Thumbnails.of(bis).scale(0.5).outputQuality(0.9).toOutputStream(bos);
            fos.write(bos.toByteArray());
            fos.close();
            return file;
        }
        return null;
    }

    private String generateFileName(MultipartFile multiPart, String prefixName) {
        String fileName =
            prefixName + "_" + System.currentTimeMillis() + "_" + multiPart.getOriginalFilename();
        return fileName.replace(" ", "_");
    }

    private void uploadFileTos3bucket(String fileName, File file) {
        s3Client.putObject(new PutObjectRequest(bucketName, fileName, file)
            .withCannedAcl(CannedAccessControlList.PublicRead));
    }

    public void deleteFileFromS3Bucket(String fileUrl) {
        String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        try {
            s3Client.deleteObject(new DeleteObjectRequest(bucketName, fileName));
        } catch (Exception e) {
            e.printStackTrace(); // Gotcha!!
        }
    }
}
