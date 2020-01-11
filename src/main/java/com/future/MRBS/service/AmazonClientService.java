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
import rx.Single;
import rx.schedulers.Schedulers;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

@Service public class AmazonClientService {

    public static final String THUMBNAIL = "thumbnail";

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

    public String saveFile(MultipartFile multipartFile, String prefixName) {
        String fileName = generateFileName(multipartFile.getOriginalFilename(), prefixName);
        String fileUrl = endpointUrl + "/" + bucketName + "/" + fileName;
        convertAndUploadFile(fileName, multipartFile);
        return fileUrl;
    }

    private void convertAndUploadFile(String fileName, MultipartFile multipartFile) {
        try {
            convertMultiPartToFile(multipartFile, fileName).subscribeOn(Schedulers.io())
                .subscribe(file -> {
                    uploadFileTos3bucket(fileName, file);
                    if (file != null) {
                        file.delete();
                    }
                }, Throwable::printStackTrace);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Single<File> convertMultiPartToFile(MultipartFile multipartFile, String fileName) {
        return Single.create(singleSubscriber -> {
            if (multipartFile.getOriginalFilename() != null) {
                try {
                    File file = new File(fileName + multipartFile.getOriginalFilename());
                    FileOutputStream fos = new FileOutputStream(file);
                    ByteArrayInputStream bis = new ByteArrayInputStream(multipartFile.getBytes());
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    if (fileName.contains(THUMBNAIL)) {
                        Thumbnails.of(bis).size(160, 160).outputQuality(0.75).toOutputStream(bos);
                    } else {
                        Thumbnails.of(bis).scale(0.5).outputQuality(0.8).toOutputStream(bos);
                    }
                    fos.write(bos.toByteArray());
                    fos.close();
                    singleSubscriber.onSuccess(file);
                } catch (Exception e) {
                    singleSubscriber.onError(e);
                    e.printStackTrace();
                }
            }
        });
    }

    private String generateFileName(String originalFileName, String prefixName) {
        String fileName = prefixName + "_" + System.currentTimeMillis() + "_" + originalFileName;
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
