package web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import web.dto.StatusDTO;

import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;

class BroadcastTest {

    private ObjectMapper mapperMock;
    private Broadcast impl;

    @BeforeEach
    public void setup() {
        mapperMock = Mockito.mock(ObjectMapper.class);
        impl = new Broadcast(mapperMock);
    }


    @Test
    public void sendBroadcastsToAllSessions() throws Exception {
        var status = new StatusDTO("ssid", "address", "state", 42, "id1", "name1");

        var sessionMock1 = mockSession();
        var sessionMock2 = mockSession();

        Mockito.when(mapperMock.writeValueAsString(Mockito.same(status)))
                .thenReturn("status json");

        // connect both sessions
        impl.addSession(sessionMock1.getLeft());
        impl.addSession(sessionMock2.getLeft());

        Mockito.reset(sessionMock1.getRight());
        Mockito.reset(sessionMock2.getRight());

        // run the test
        impl.send(status);

        Mockito.verify(sessionMock1.getRight(), Mockito.times(1)).sendText("status json");
        Mockito.verify(sessionMock2.getRight(), Mockito.times(1)).sendText("status json");
    }

    @Test
    public void removeSessionWorks() throws Exception {
        var status = new StatusDTO("ssid", "address", "state", 42, "id1", "name1");

        var sessionMock1 = mockSession();
        var sessionMock2 = mockSession();

        Mockito.when(mapperMock.writeValueAsString(Mockito.same(status)))
                .thenReturn("status json");

        // connect both sessions
        impl.addSession(sessionMock1.getLeft());
        impl.addSession(sessionMock2.getLeft());

        Mockito.reset(sessionMock1.getRight());
        Mockito.reset(sessionMock2.getRight());

        // run test - remove one session via onclose, prove it's gone with "send"
        impl.removeSession(sessionMock1.getLeft());
        impl.send(status);

        Mockito.verifyNoInteractions(sessionMock1.getRight());
        Mockito.verify(sessionMock2.getRight(), Mockito.times(1)).sendText("status json");
    }

    private Pair<Session, RemoteEndpoint.Basic> mockSession() {
        var remoteMock = Mockito.mock(RemoteEndpoint.Basic.class);

        var sessionMock = Mockito.mock(Session.class);
        Mockito.when(sessionMock.getBasicRemote())
                .thenReturn(remoteMock);

        return Pair.of(sessionMock, remoteMock);
    }
}