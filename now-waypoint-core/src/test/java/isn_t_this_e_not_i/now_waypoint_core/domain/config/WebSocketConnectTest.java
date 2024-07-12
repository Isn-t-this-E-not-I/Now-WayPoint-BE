package isn_t_this_e_not_i.now_waypoint_core.domain.config;

import isn_t_this_e_not_i.now_waypoint_core.domain.auth.jwt.JwtUtil;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.service.UserDetailService;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.dto.UserRequest;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.dto.UserResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
public class WebSocketConnectTest {

    @LocalServerPort
    private int port;

    private WebSocketStompClient stompClient;
    @Autowired
    private TestRestTemplate testRestTemplate;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setup() {
        List<Transport> transports = new ArrayList<>();
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        WebSocketClient transport = new SockJsClient(transports);
        stompClient = new WebSocketStompClient(transport);
    }

    @AfterEach
    void afterEach() {
        //매 테스트마다 테이블 초기화
        //H2 Database
        List<String> truncateQueries = jdbcTemplate.queryForList(
                "SELECT CONCAT('TRUNCATE TABLE ', TABLE_NAME, ';') AS q FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC'", String.class);
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        truncateQueries.forEach(jdbcTemplate::execute);
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
        //MySQL
//        jdbcTemplate.execute("SET foreign_key_checks = 0;");
//        jdbcTemplate.execute("TRUNCATE TABLE member");
//        jdbcTemplate.execute("SET foreign_key_checks = 1;");
    }

    @Test
    @DisplayName("웹 소캣 연결 테스트")
    public void WebSocketConnectTestWithValidToken() throws Exception {
        registerUser();
        String accessToken = loginUser("imc2@test.com", "1234");
        System.out.println("Access Token: " + accessToken);

        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("Authorization", "Bearer " + accessToken);
        System.out.println("Connect Headers: " + connectHeaders.get("Authorization"));

        try {
            StompSession session = stompClient.connect(
                    "ws://localhost:" + port + "/ws",
                    new WebSocketHttpHeaders(),  // WebSocketHttpHeaders can be empty if not needed
                    connectHeaders,
                    new StompSessionHandlerAdapter() {
                        @Override
                        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                            System.out.println("Connected with valid token");
                        }
                    }
            ).get(10, TimeUnit.SECONDS);  // Adjust the timeout as needed
        } catch (Exception e) {
            e.printStackTrace();  // Log the exception for debugging
            throw e;  // Rethrow the exception to fail the test if needed
        }
    }

    ResponseEntity<UserResponse> registerUser() {
        UserRequest.registerRequest registerRequest = UserRequest.registerRequest.builder()
                .loginId("imc2@test.com").password("1234").nickname("imcc").build();

        HttpHeaders httpHeaders = new HttpHeaders();
        HttpEntity<UserRequest.registerRequest> request = new HttpEntity<>(registerRequest, httpHeaders);
        return testRestTemplate.postForEntity("/api/user/register",
                request,
                UserResponse.class);
    }

    String loginUser(String loginId, String password) {
        UserRequest.loginRequest loginRequest = UserRequest.loginRequest.builder()
                .loginId(loginId).password(password).build();

        HttpHeaders httpHeaders = new HttpHeaders();
        HttpEntity<UserRequest.loginRequest> request = new HttpEntity<>(loginRequest, httpHeaders);

        ResponseEntity<UserResponse.token> response = testRestTemplate.postForEntity("/api/user/login", request, UserResponse.token.class);
        return response.getBody().getToken();
    }
}
