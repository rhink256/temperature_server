package web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import web.dto.StatusDTO;

import javax.websocket.Session;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class StatusBroadcastTest {

    private SensorFacade facadeMock;
    private Broadcast broadcast;
    private StatusBroadcast impl;

    @BeforeEach
    public void setup() {
        facadeMock = Mockito.mock(SensorFacade.class);
        broadcast = Mockito.mock(Broadcast.class);

        impl = new StatusBroadcast(facadeMock, broadcast);
    }

    @Test
    public void onOpenBroadcastsAllStatus() throws Exception {
        List<StatusDTO> status = Arrays.asList(
                new StatusDTO("ssid", "address", "state", 42, "id1", "name1"),
                new StatusDTO("ssid", "address", "state", 42, "id2", "name2")
        );

        Mockito.when(facadeMock.getAllStatus()).thenReturn(status);

        Session sessionMock = Mockito.mock(Session.class);

        impl.onOpen(sessionMock);

        Mockito.verify(broadcast, Mockito.times(1))
                .send(Mockito.same(status.get(0)));

        Mockito.verify(broadcast, Mockito.times(1))
                .send(Mockito.same(status.get(1)));
    }

    @Test
    public void onOpenRegistersSession() throws Exception {

        Mockito.when(facadeMock.getAllStatus()).thenReturn(Collections.emptyList());

        Session sessionMock = Mockito.mock(Session.class);

        impl.onOpen(sessionMock);

        Mockito.verify(broadcast, Mockito.times(1)).addSession(sessionMock);
    }

    @Test
    public void onCloseRemovesSession() {
        Session sessionMock1 = Mockito.mock(Session.class);
        impl.onOpen(sessionMock1);

        Session sessionMock2 = Mockito.mock(Session.class);
        impl.onOpen(sessionMock2);

        Mockito.reset(broadcast);

        impl.onClose(sessionMock1);

        Mockito.verify(broadcast).removeSession(sessionMock1);
        Mockito.verifyNoMoreInteractions(broadcast);
    }

    @Test
    public void onErrorRemovesSession() {

        Session sessionMock1 = Mockito.mock(Session.class);
        impl.onOpen(sessionMock1);

        Session sessionMock2 = Mockito.mock(Session.class);
        impl.onOpen(sessionMock2);

        Mockito.reset(broadcast);

        impl.error(sessionMock1, null);

        Mockito.verify(broadcast).removeSession(sessionMock1);
        Mockito.verifyNoMoreInteractions(broadcast);
    }

    @Test
    public void sendCallBroadcasts() throws Exception {
        StatusDTO status = new StatusDTO();
        impl.send(status);

        Mockito.verify(broadcast).send(status);
    }
}