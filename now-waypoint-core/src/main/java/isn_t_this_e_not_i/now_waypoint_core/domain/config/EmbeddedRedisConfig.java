package isn_t_this_e_not_i.now_waypoint_core.domain.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConfiguration;
import redis.embedded.RedisServer;

import java.io.IOException;

@Configuration
@Profile("dev")
public class EmbeddedRedisConfig {

    @Value("${spring.data.redis.port}")
    private int redisPort;

    private RedisServer redisServer;

    @PostConstruct
    public void startRedis() throws IOException {
        redisServer = new RedisServer(redisPort);
        try {
            redisServer.start();
        } catch (RuntimeException e) {
            throw new IllegalStateException("Embedded Redis 서버를 시작할 수 없습니다.", e);
        }
    }

    @PreDestroy
    public void stopRedis() {
        if (redisServer != null) {
            redisServer.stop();
        }
    }

    @Bean
    public RedisConfig redisConfig() {
        return new RedisConfig();
    }
}
