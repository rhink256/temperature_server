package web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import web.dto.SensorReportDTO;

import javax.websocket.Session;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class TempBroadcastTest {

    private SensorFacade facadeMock;
    private Broadcast broadcast;
    private TempBroadcast impl;

    @BeforeEach
    public void setup() {
        facadeMock = Mockito.mock(SensorFacade.class);
        broadcast = Mockito.mock(Broadcast.class);

        impl = new TempBroadcast(facadeMock, broadcast);
    }

    @Test
    public void onOpenBroadcastsTemperature() throws Exception {
        var status = Arrays.asList(
                new SensorReportDTO(),
                new SensorReportDTO()
        );

        status.get(0).setSensorId("1");
        status.get(1).setSensorId("2");

        Mockito.when(facadeMock.getLatest()).thenReturn(status);

        var sessionMock = Mockito.mock(Session.class);

        impl.onOpen(sessionMock);

        Mockito.verify(broadcast, Mockito.times(1))
                .send(Mockito.same(status.get(0)));

        Mockito.verify(broadcast, Mockito.times(1))
                .send(Mockito.same(status.get(1)));
    }

    @Test
    public void onOpenRegistersSession() {

        Mockito.when(facadeMock.getAllStatus()).thenReturn(Collections.emptyList());

        var sessionMock = Mockito.mock(Session.class);

        impl.onOpen(sessionMock);

        Mockito.verify(broadcast, Mockito.times(1)).addSession(sessionMock);
    }

    @Test
    public void onCloseRemovesSession() {
        var sessionMock1 = Mockito.mock(Session.class);
        impl.onOpen(sessionMock1);

        var sessionMock2 = Mockito.mock(Session.class);
        impl.onOpen(sessionMock2);

        Mockito.reset(broadcast);

        impl.onClose(sessionMock1);

        Mockito.verify(broadcast).removeSession(sessionMock1);
        Mockito.verifyNoMoreInteractions(broadcast);
    }

    @Test
    public void onErrorRemovesSession() {

        var sessionMock1 = Mockito.mock(Session.class);
        impl.onOpen(sessionMock1);

        var sessionMock2 = Mockito.mock(Session.class);
        impl.onOpen(sessionMock2);

        Mockito.reset(broadcast);

        impl.error(sessionMock1, null);

        Mockito.verify(broadcast).removeSession(sessionMock1);
        Mockito.verifyNoMoreInteractions(broadcast);
    }

    @Test
    public void sendCallBroadcasts() throws Exception {
        var status = new SensorReportDTO();
        impl.send(status);

        Mockito.verify(broadcast).send(status);
    }
}