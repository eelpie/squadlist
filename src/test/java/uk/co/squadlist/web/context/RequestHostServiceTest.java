package uk.co.squadlist.web.context;

import org.junit.Test;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class RequestHostServiceTest {

    @Test
    public void shouldUseHostHeaderToIdentifyInstance() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("host")).thenReturn("a-club.squadlist.app");

        String requestHost = new RequestHostService(request).getRequestHost();

        assertEquals("a-club.squadlist.app", requestHost);
    }

    @Test
    public void shouldFallbackToXForwardHostIfHostNotSeen() {
        // ie. when running behind nginx proxy or similar.
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("x-forwarded-host")).thenReturn("another-club.squadlist.app");

        String requestHost = new RequestHostService(request).getRequestHost();

        assertEquals("another-club.squadlist.app", requestHost);
    }

}
