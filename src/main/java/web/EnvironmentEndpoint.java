package web;

import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import web.dto.SensorReportDTO;
import web.dto.StatusDTO;
import web.dto.TemperatureDataDto;
import web.entities.CalibrationData;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Path("")
@Singleton
public class EnvironmentEndpoint {

	private static final Logger LOG = LogManager.getLogger(EnvironmentEndpoint.class);

	@EJB
	private SensorFacade facade;

	@EJB
	private TempBroadcast tempBroadcast;

	@EJB
	private StatusBroadcast statusBroadcast;

	public EnvironmentEndpoint() {

	}

	public EnvironmentEndpoint(SensorFacade facade, TempBroadcast tempBroadcast, StatusBroadcast statusBroadcast) {
		this.facade = facade;
		this.tempBroadcast = tempBroadcast;
		this.statusBroadcast = statusBroadcast;
	}

	@PUT
	@Path("/temp")
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response putTemp(@Context HttpServletRequest request, SensorReportDTO env) throws JsonProcessingException {
		String addr = request.getRemoteAddr();

		env.setSensorId(addr);

		env.setReportTime(new Date());

		tempBroadcast.send(facade.report(env));
		LOG.info("====>got request PUT::" + env);
		return Response.ok().build();
	}

	@PUT
	@Path("/temp/name")
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response setName(NameChange name) throws JsonProcessingException {
		LOG.info("====>name change:" + name);
		facade.setName(name.id, name.name);
		SensorReportDTO report = facade.getLatestReportById(name.id);

		tempBroadcast.send(report);

		statusBroadcast.send(facade.getStatus(name.id));
		return Response.ok().build();
	}

	@PUT
	@Path("/status")
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response putStatus(@Context HttpServletRequest request, StatusDTO status) throws JsonProcessingException {
		status.id = request.getRemoteAddr();

		StatusDTO out = facade.setStatus(status);
		if (out != null) {
			statusBroadcast.send(facade.setStatus(out));
		}

		LOG.info("====>status update::" + status);
		return Response.status(200).build();
	}

	@PUT
	@Path("temp/calibration")
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response setCalibration(CalibrationData calibration)	throws Exception {
		LOG.info("====>calibration update::" + calibration);
		facade.setCalibration(calibration);

		SensorReportDTO report = facade.getLatestReportById(calibration.getSensorId());

		tempBroadcast.send(report);

		return Response.status(200).build();
	}

	@GET
	@Path("temp/range/{sensorId}/{startDate}/{endDate}/{intervalType}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.TEXT_PLAIN)
	public Response getTemperatureOverInterval(
			@PathParam("sensorId") String sensorId,
			@PathParam("startDate") String startDate,
			@PathParam("endDate") String endDate,
			@PathParam("intervalType") String intervalType) throws Exception {
		LOG.info("=====> get interval for sensorId:" + sensorId
				+ " from: \'" + startDate + "\' to: \'" + endDate
				+ "\' with interval of: " + intervalType);

		SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy", Locale.ENGLISH);

		Date start = formatter.parse(startDate);
		Date end = formatter.parse(endDate);

		List<TemperatureDataDto> result =
				facade.getTemperatureForRange(sensorId, start, end, intervalType);

		return Response.status(200).entity(result).build();
	}

//	@GET
//	@Path("temp/min/{sensor_ip}/{date}")
//	@Produces(MediaType.TEXT_PLAIN)
//	@Consumes(MediaType.TEXT_PLAIN)
//	public Response getMinTemp(
//			@PathParam("sensor_ip") String sensorId,
//			@PathParam("startDate") String startDateString,
//			@PathParam("endDate") String endDateString) {
//		LOG.info("====>get min temp for::" + sensorId + "/" + startDateString);
//		SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy", Locale.ENGLISH);
//
//		try {
//			Date start = formatter.parse(startDateString);
//			Date end = formatter.parse(endDateString);
//
//			float result = facade.getMinTemperature(sensorId, start, end);
//
//			LOG.info("result::" + result);
//
//			return Response.status(200).entity(result).build();
//		} catch (ParseException ex) {
//			throw new RuntimeException("Date Parse Exception: " + startDateString + " to: " + endDateString, ex);
//		}
//	}
//
//	@GET
//	@Path("temp/max/{sensor_ip}/{date}")
//	@Produces(MediaType.TEXT_PLAIN)
//	@Consumes(MediaType.TEXT_PLAIN)
//	public Response getMaxTemp(
//			@PathParam("sensor_ip") String sensorId,
//			@PathParam("startDate") String startDateString,
//			@PathParam("endDate") String endDateString) {
//		LOG.info("====>get max temp for::" + sensorId + "/" + startDateString);
//		SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy", Locale.ENGLISH);
//
//		try {
//			Date start = formatter.parse(startDateString);
//			Date end = formatter.parse(endDateString);
//
//			float result = facade.getMaxTemperature(sensorId, start, end);
//
//			LOG.info("result::" + result);
//
//			return Response.status(200).entity(result).build();
//		} catch (ParseException ex) {
//			throw new RuntimeException("Date Parse Exception: " + startDateString + " to: " + endDateString, ex);
//		}
//	}
}
