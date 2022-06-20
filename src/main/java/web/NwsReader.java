package web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import web.dto.SensorReportDTO;

/**
 * reads current conditions from national weather service using their public REST API
 */
@Singleton
public class NwsReader {
	
	@EJB
	private SensorFacade facade;
	
	@EJB
	private TempBroadcast broadcast;

	private long lastTime = 0;

	private static final Logger LOG = LogManager.getLogger(NwsReader.class);

	@Schedule(hour = "*", minute = "*/1", info = "Poll current conditions from NWS")
	public void pollNws() {
		if (System.currentTimeMillis() - lastTime > 1000 * 60 * 30) {
			lastTime = System.currentTimeMillis();
			try {
				processNwsData();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void processNwsData() throws IOException {
		LOG.info("getting NWS data");
		URL url = new URL("https://api.weather.gov/stations/KEZF/observations/latest");
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		if (conn.getResponseCode() != 200) {
			LOG.info("Response::" + conn.getResponseCode());
			LOG.info(conn.getResponseMessage());
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

		StringBuilder builder = new StringBuilder();
		String output;
		while ((output = br.readLine()) != null) {
			builder.append(output).append('\n');
		}

		ObjectMapper mapper = new ObjectMapper();
		JsonNode root = mapper.readTree(builder.toString());
		JsonNode props = root.path("properties");
		
		if(!props.path("temperature").path("value").isNull()) {
			SensorReportDTO reportDto = new SensorReportDTO();
			reportDto.setCelsius((float) props.path("temperature").path("value").asDouble());
			reportDto.setHumidity((float)props.path("relativeHumidity").path("value").asDouble());
			reportDto.setPressure((float)props.path("barometricPressure").path("value").asDouble() / 1000f);
			reportDto.setName("NWS - Shannon Airport");
			reportDto.setSensorId("NWS");
			reportDto.setExpectedUpdateRateSeconds(30 * 60); // 30 minutes

			LOG.info("NWS report::"+reportDto);

			broadcast.send(facade.report(reportDto));
		} else {
			LOG.info("No Data Received from Shannon Airport");
		}
	}
}
