package isn_t_this_e_not_i.now_waypoint_core;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
class NowWaypointCoreApplicationTests {

	@DynamicPropertySource
	static void setup(DynamicPropertyRegistry registry) {
		String accessKeyId = System.getenv("CLOUD_AWS_CREDENTIALS_ACCESS_KEY");
		String secretAccessKey = System.getenv("CLOUD_AWS_CREDENTIALS_SECRET_KEY");
		String googleClientId = System.getenv("GOOGLE_CLIENT_ID");
		String googleClientSecret = System.getenv("GOOGLE_CLIENT_SECRET");
		String fileStoragePath = System.getenv("FILE_STORAGE_PATH");

		if (accessKeyId == null || secretAccessKey == null || fileStoragePath == null || googleClientId == null || googleClientSecret == null) {
			throw new IllegalArgumentException("Required environment variables are not set");
		}

		registry.add("cloud.aws.credentials.access-key", () -> accessKeyId);
		registry.add("cloud.aws.credentials.secret-key", () -> secretAccessKey);
		registry.add("file.storage.path", () -> fileStoragePath);
		registry.add("google.client.id", () -> googleClientId);
		registry.add("google.client.secret", () -> googleClientSecret);
	}

	@Test
	void contextLoads() {
		// Your test code here
	}
}