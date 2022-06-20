package web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import web.dto.SensorReportDTO;
import web.dto.StatusDTO;
import web.dto.TemperatureDataDto;
import web.entities.CalibrationData;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class EnvironmentEndpointTest {
    EnvironmentEndpoint endpoint;
    SensorFacade facadeMock;
    TempBroadcast tempBroadcastMock;
    StatusBroadcast statusBroadcastMock;

    @BeforeEach
    public void setup() {
        facadeMock = Mockito.mock(SensorFacade.class);
        tempBroadcastMock = Mockito.mock(TempBroadcast.class);
        statusBroadcastMock = Mockito.mock(StatusBroadcast.class);
        endpoint = new EnvironmentEndpoint(facadeMock, tempBroadcastMock, statusBroadcastMock);
    }

    @Test
    public void putTempBroadcastsAndReturnsOK() throws Exception {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getRemoteAddr()).thenReturn("address");

        SensorReportDTO report = new SensorReportDTO();
        Mockito.when(facadeMock.report(report)).thenReturn(report);

        Response response = endpoint.putTemp(request, report);

        assertEquals(response.getStatusInfo().getStatusCode(), 200);
        Mockito.verify(tempBroadcastMock).send(report);
    }

    @Test
    public void setNameBroadcastsAndReturnsOK() throws Exception {
        NameChange name = new NameChange();
        name.id = "ID";
        name.name = "NAME";

        Mockito.when(facadeMock.getLatestReportById("ID")).thenReturn(new SensorReportDTO());
        Mockito.when(facadeMock.getStatus("ID")).thenReturn(new StatusDTO());

        Response response = endpoint.setName(name);

        assertEquals(response.getStatusInfo().getStatusCode(), 200);
        Mockito.verify(tempBroadcastMock).send(Mockito.any(SensorReportDTO.class));
        Mockito.verify(statusBroadcastMock).send(Mockito.any(StatusDTO.class));
        Mockito.verify(facadeMock).setName("ID", "NAME");
    }

    @Test
    public void putStatusBroadcastsAndReturnsOK() throws Exception {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getRemoteAddr()).thenReturn("address");

        StatusDTO status = new StatusDTO();
        Mockito.when(facadeMock.setStatus(status)).thenReturn(status);

        Response response = endpoint.putStatus(request, status);

        assertEquals(response.getStatusInfo().getStatusCode(), 200);
        Mockito.verify(statusBroadcastMock).send(status);
    }

    @Test
    public void setCalbrationBroadcastsAndReturnsOK() throws Exception {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getRemoteAddr()).thenReturn("address");

        CalibrationData calibration = new CalibrationData();
        calibration.setSensorId("ID");
        calibration.setOffset(12.3f);

        Response response = endpoint.setCalibration(calibration);

        assertEquals(response.getStatusInfo().getStatusCode(), 200);
        Mockito.verify(facadeMock).setCalibration(Mockito.eq(calibration));
    }

    @Test
    public void getTemperatureOverInterval() throws Exception {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getRemoteAddr()).thenReturn("address");

        String startDate = "01-01-0001";
        String endDate = "12-12-2012";

        SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy", Locale.ENGLISH);

        Date start = formatter.parse(startDate);
        Date end = formatter.parse(endDate);

        Mockito.when(
                facadeMock.getTemperatureForRange(Mockito.eq("4321"), Mockito.eq(start), Mockito.eq(end), Mockito.eq("qwerty")))
                .thenReturn(Arrays.asList(new TemperatureDataDto("qwerty", -40)));

        Response response = endpoint.getTemperatureOverInterval("4321", startDate, endDate, "qwerty");

        assertEquals(response.getStatusInfo().getStatusCode(), 200);
    }
}