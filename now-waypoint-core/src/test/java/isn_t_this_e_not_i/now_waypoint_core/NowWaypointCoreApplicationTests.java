package isn_t_this_e_not_i.now_waypoint_core;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class NowWaypointCoreApplicationTests {

	@BeforeAll
	static void setup() {
		String accessKeyId = System.getenv("CLOUD_AWS_CREDENTIALS_ACCESS_KEY");
		String secretAccessKey = System.getenv("CLOUD_AWS_CREDENTIALS_SECRET_KEY");
		String googleClientId = System.getenv("GOOGLE_CLIENT_ID");
		String googleClientSecret = System.getenv("GOOGLE_CLIENT_SECRET");
		String fileStoragePath = System.getenv("FILE_STORAGE_PATH");

		accessKeyId = accessKeyId != null ? accessKeyId : "defaultAccessKey";
		secretAccessKey = secretAccessKey != null ? secretAccessKey : "defaultSecretKey";
		googleClientId = googleClientId != null ? googleClientId : "defaultGoogleClientId";
		googleClientSecret = googleClientSecret != null ? googleClientSecret : "defaultGoogleClientSecret";
		fileStoragePath = fileStoragePath != null ? fileStoragePath : "/default/path";

		System.setProperty("cloud.aws.credentials.access-key", accessKeyId);
		System.setProperty("cloud.aws.credentials.secret-key", secretAccessKey);
		System.setProperty("file.storage.path", fileStoragePath);
		System.setProperty("google.client.id", googleClientId);
		System.setProperty("google.client.secret", googleClientSecret);
	}

	@Test
	void contextLoads() {
		// Your test code here
	}
}