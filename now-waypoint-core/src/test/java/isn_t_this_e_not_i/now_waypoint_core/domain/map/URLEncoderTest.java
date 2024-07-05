package isn_t_this_e_not_i.now_waypoint_core.domain.map;

import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class URLEncoderTest {

    @Test
    public void testURLEncoder() throws UnsupportedEncodingException {
        String address = "판교동";
        String encodedAddress = URLEncoder.encode(address, "UTF-8");
        assertEquals("%ED%8C%90%EA%B5%90%EB%8F%99", encodedAddress);
    }
}
