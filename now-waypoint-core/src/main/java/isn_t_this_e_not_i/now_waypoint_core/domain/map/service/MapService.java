package isn_t_this_e_not_i.now_waypoint_core.domain.map.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

@Service
@Slf4j
public class MapService {

    @Value("${kakao.api.key}")
    private String apiKey;

    @Value("${kakao.api.url}")
    private String apiUrl;

    public String getMapInfo(String query) {
        String jsonString = null;

        try {
            // 1. URL 인코딩
            query = URLEncoder.encode(query, "UTF-8");
            log.info("Encoded query: {}", query);

            // 2. 요청 url을 만들기
            String addr;
            if (query.matches("-?\\d+(\\.\\d+)?%2C-?\\d+(\\.\\d+)?")) { // 좌표 형태인 경우
                log.info("Query is recognized as coordinates");
                String[] coords = query.split("%2C");
                addr = String.format("https://dapi.kakao.com/v2/local/geo/coord2address.json?x=%s&y=%s", coords[1], coords[0]);

                // 위도와 경도를 로그에 기록
                log.info("Received coordinates: latitude={}, longitude={}", coords[0], coords[1]);
            } else { // 주소 형태인 경우
                log.info("Query is recognized as an address");
                addr = apiUrl + "?query=" + query;
            }

            // 3. URL 객체 생성
            URL url = new URL(addr);
            log.info("Request URL: {}", url);

            // 4. URL Connection 객체 생성
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // 5. 헤더값 설정해주기
            conn.setRequestProperty("Authorization", "KakaoAK " + apiKey);

            // 6. StringBuffer에 값을 넣고 String 형태로 변환하고 jsonString을 return
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

            StringBuffer docJson = new StringBuffer();
            String line;

            while ((line = rd.readLine()) != null) {
                docJson.append(line);
            }

            jsonString = docJson.toString();
            rd.close();

            // 응답 로그
            log.info("mapInfo = {}", jsonString);

        } catch (UnsupportedEncodingException e) {
            log.error("Encoding error: ", e);
        } catch (MalformedURLException e) {
            log.error("Malformed URL: ", e);
        } catch (IOException e) {
            log.error("IO error: ", e);
        }

        return jsonString;
    }
}
