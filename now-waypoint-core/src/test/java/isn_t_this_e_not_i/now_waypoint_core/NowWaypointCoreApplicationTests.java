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
		String fileStoragePath = System.getenv("FILE_STORAGE_PATH");

		if (accessKeyId == null || secretAccessKey == null || fileStoragePath == null) {
			throw new IllegalArgumentException("Required environment variables are not set");
		}

		System.setProperty("cloud.aws.credentials.access-key", accessKeyId);
		System.setProperty("cloud.aws.credentials.secret-key", secretAccessKey);
		System.setProperty("file.storage.path", fileStoragePath);
	}

	@Test
	void contextLoads() {
		// Your test code here
	}
}