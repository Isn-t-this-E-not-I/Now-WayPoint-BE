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

    public String getMapInfo(String roadFullAddr){
        String jsonString = null;

        try {
            // 1. URL 인코딩
            roadFullAddr = URLEncoder.encode(roadFullAddr, "UTF-8");

            // 2. 요청 url을 만들기
            String addr = apiUrl + "?query=" + roadFullAddr;

            // 3. URL 객체 생성
            URL url = new URL(addr);

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
            log.info("mapInfo ={}", jsonString);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return jsonString;
    }
}
