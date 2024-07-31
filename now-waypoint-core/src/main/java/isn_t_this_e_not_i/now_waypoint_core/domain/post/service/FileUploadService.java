package isn_t_this_e_not_i.now_waypoint_core.domain.post.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileUploadService {

    private final AmazonS3Client amazonS3Client;

    @Value("${aws.cloud.s3.bucket}")
    private String bucket;

    @Value("${file.storage.path}")
    private String fileStoragePath;

    public String fileUpload(MultipartFile file) {

        UUID uuid = UUID.randomUUID();

        String fileName = uuid + "_" + file.getOriginalFilename();

        File saveFile = new File(fileStoragePath, fileName);

        try {
            file.transferTo(saveFile);
        } catch (IOException e) {
            throw new RuntimeException("파일을 저장하는 동안 오류가 발생했습니다.");
        }

        amazonS3Client.putObject(new PutObjectRequest(bucket, fileName, saveFile).withCannedAcl(CannedAccessControlList.PublicRead));
        String fileUrl = amazonS3Client.getUrl(bucket, fileName).toString();

        saveFile.delete();
        return fileUrl;
    }

    public void deleteFile(String fileUrl) {
        String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        amazonS3Client.deleteObject(bucket, fileName);
    }
}
