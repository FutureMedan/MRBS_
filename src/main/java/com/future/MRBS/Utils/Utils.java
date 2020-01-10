package com.future.MRBS.Utils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.activation.FileTypeMap;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static com.future.MRBS.service.ServiceImpl.RoomServiceImpl.MESSAGE;

public class Utils {
    public static final String SORT_ASC = "ASC";
    public static final String SORT_DESC = "DESC";
    public static final String IMAGE_URL = "http://localhost:8080/img/";
    public static final String IMAGE_LOCATION = "assets/";
    public static final String USER_PREFIX = "users/";
    public static final String ROOM_PREFIX = "rooms/";
    public static final String BASE_URL = "http://10.0.2.2:8080/";


    public static Pageable createPageRequest(String property, String direction, int page,
        int size) {
        if (direction.equals(SORT_ASC)) {
            return PageRequest.of(page, size, Sort.by(property).ascending());
        }
        return PageRequest.of(page, size, Sort.by(property).descending());
    }

    private static boolean isFileSaved(MultipartFile file, String name) throws IOException {
        return !file.isEmpty() && saveUploadedFile(file, name);
    }

    public static boolean saveUploadedFile(MultipartFile file, String name) throws IOException {
        byte[] bytes = file.getBytes();
        Path path = Paths.get(IMAGE_LOCATION + name);
        Files.write(path, bytes);
        return true;
    }

    private static boolean checkImageFile(MultipartFile file) {
        boolean isImageFile = false;
        if (file.getContentType() != null) {
            isImageFile = file.getContentType().equals("image/png") || file.getContentType()
                .equals("image/jpg") || file.getContentType().equals("image/jpeg") || file
                .getContentType().equals("image/bmp") || file.getContentType().equals("image/*");
        }
        return isImageFile;
    }

    public static String saveImageURL(String name, MultipartFile file) throws IOException {
        String imageURL = "";
        if (checkImageFile(file)) {
            String fileName = name + "_" + file.getOriginalFilename();
            imageURL = isFileSaved(file, fileName) ? IMAGE_LOCATION + fileName : "";
        }
        return imageURL;
    }

    public static void deleteImage(String imageURL) throws IOException {
        if (!imageURL.isEmpty()) {
            try {
                imageURL = imageURL.replace(IMAGE_URL, "");
                Path deletePath = Paths.get(imageURL);
                Files.delete(deletePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static ResponseEntity getImage(String imageName) {
        try {
            Path path = Paths.get(IMAGE_LOCATION + imageName);
            File img = new File(String.valueOf(path));
            String mimetype = FileTypeMap.getDefaultFileTypeMap().getContentType(img);
            return ResponseEntity.ok().contentType(MediaType.valueOf(mimetype))
                .body(Files.readAllBytes(img.toPath()));
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public static ResponseEntity createErrorResponse(String errorMessage, HttpStatus httpStatus) {
        Map<String, String> response = new HashMap<>();
        response.put(MESSAGE, errorMessage);
        return new ResponseEntity<>(response, httpStatus);
    }


}
