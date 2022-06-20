package web;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import web.dto.SensorReportDTO;
import web.dto.StatusDTO;
import web.dto.TemperatureDataDto;
import web.entities.CalibrationData;
import web.entities.Sensor;
import web.entities.SensorReport;
import web.entities.Status;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.*;

public class SensorFacadeTest {

	@Test
	public void reportIfSensorExists() {
		var reportId = "0.0.0.0.0";
		var entityManagerMock = Mockito.mock(EntityManager.class);

		var facade = new SensorFacade(() -> new Date(1234L), entityManagerMock);

		var spy = Mockito.spy(facade);

		var calibrationData = new CalibrationData();
		calibrationData.setOffset(-999f);

		var sensor = new Sensor();
		sensor.setSensorId(reportId);
		sensor.setName("a_sensor");
		sensor.setCalibrationData(calibrationData);

		Mockito.doReturn(Optional.of(sensor)).when(spy).getSensorOptById(entityManagerMock, reportId);

		var report = new SensorReportDTO();
		report.setSensorId(reportId);
		report.setCelsius(42.3f);
		report.setHumidity(12.3f);

		var result = spy.report(report);

		Assertions.assertNotNull(result);
		Assertions.assertEquals(reportId, result.getSensorId());
		Assertions.assertEquals("a_sensor", result.getName());
		Assertions.assertEquals(42.3f, result.getCelsius());
		Assertions.assertEquals(12.3f, result.getHumidity());
		Assertions.assertEquals(reportId, result.getSensorId());
		Assertions.assertEquals(-999f, report.getOffset());
		Assertions.assertEquals(new Date(1234L), result.getReportTime(), "If time not set in report, replace with current time");
	}

	@Test
	public void reportForNewSensor() {
		var reportId = "0.0.0.0.0";
		var entityManagerMock = Mockito.mock(EntityManager.class);

		var facade = new SensorFacade(() -> new Date(1234L), entityManagerMock);

		var spy = Mockito.spy(facade);
		Mockito.doReturn(Optional.empty()).when(spy).getSensorOptById(entityManagerMock, reportId);

		var report = new SensorReportDTO();
		report.setSensorId(reportId);
		report.setName("This is a test");
		report.setCelsius(42.3f);
		report.setHumidity(12.3f);
		report.setReportTime(new Date(4321L));

		var result = spy.report(report);

		Assertions.assertNotNull(result);
		Assertions.assertEquals("This is a test", result.getName(), "Name shouldn't be changed from report");
		Assertions.assertEquals(42.3f, result.getCelsius());
		Assertions.assertEquals(12.3f, result.getHumidity());
		Assertions.assertEquals(reportId, result.getSensorId());
		Assertions.assertEquals(-999f, result.getOffset());
		Assertions.assertEquals(new Date(4321L), result.getReportTime(), "Time shouldn't be updated if valid in report");
	}
	
	@Test
	public void getMostRecentSensorReport() {
		var reportId = "0.0.0.0.0";

		var facade = new SensorFacade(() -> new Date(1234L), null);

		var sensor = new Sensor();
		sensor.setSensorId(reportId);

		var calibrationData = new CalibrationData();
		calibrationData.setOffset(234f);

		sensor.setCalibrationData(calibrationData);

		var spy = Mockito.spy(facade);

		var report = new SensorReport();
		report.setCelsius(123f);
		report.setSensor(sensor);

		Mockito.doReturn(report).when(spy).getLatestReport(reportId);

		var result = spy.getLatestReportById(reportId);

		Assertions.assertEquals(sensor.getSensorId(), result.getSensorId());
		Assertions.assertEquals(sensor.getCalibrationData().getOffset(), result.getOffset());
		Assertions.assertEquals(report.getCelsius(), result.getCelsius());
	}

	@Test
	public void testUpdateCalibration() {
		var reportId = "0.0.0.0.0";

		var facade = new SensorFacade(() -> new Date(1234L), null);

		var calibrationData = new CalibrationData();
		calibrationData.setOffset(-999f);

		var sensor = new Sensor();
		sensor.setCalibrationData(calibrationData);

		var spy = Mockito.spy(facade);
		Mockito.doReturn(sensor).when(spy).getSensorById(reportId);

		var data = new CalibrationData();
		data.setOffset(12);
		data.setSensorId(reportId);

		// set calibration data
		spy.setCalibration(data);

		Assertions.assertEquals(data.getOffset(), 12f);
	}

	@Test
	public void testGetLatest()  {

		// configure sensors
		var sensor1 = new Sensor();
		sensor1.setSensorId("0");
		var sensor2 = new Sensor();
		sensor2.setSensorId("1");
		var sensors = Arrays.asList(sensor1, sensor2);

		var entityManagerMock = Mockito.mock(EntityManager.class);

		var facade = new SensorFacade(() -> new Date(1234L), entityManagerMock);

		var spy = Mockito.spy(facade);
		Mockito.doReturn(sensors).when(spy).getSensors(entityManagerMock);

		// create sensor reports
		var report1 = new SensorReport();
		report1.setSensor(sensor1);
		sensor1.getReports().add(report1);
		sensor1.setCalibrationData(new CalibrationData());

		var report2 = new SensorReport();
		report2.setSensor(sensor2);
		sensor2.getReports().add(report2);
		sensor2.setCalibrationData(new CalibrationData());

		// spy the facade, configure getLatest to return sensor reports
		Mockito.doReturn(report1).when(spy).getLatestReport("0");
		Mockito.doReturn(report2).when(spy).getLatestReport("1");

		var result = spy.getLatest();

		// verify the reports are returned
		Assertions.assertTrue(result.stream().map(SensorReportDTO::getSensorId).anyMatch(s -> s.equals("0")));
		Assertions.assertTrue(result.stream().map(SensorReportDTO::getSensorId).anyMatch(s -> s.equals("1")));
		Assertions.assertEquals(2, result.size());
	}

	@Test
	public void testGetLatestReport() {
		SensorReport sensorReport = new SensorReport();

		Sensor sensor = new Sensor();
		sensor.getReports().add(sensorReport);

		SensorFacade facade = Mockito.spy(new SensorFacade(() -> new Date(1234L), null));
		Mockito.doReturn(sensor).when(facade).getSensorById("0");

		SensorReport result = facade.getLatestReport("0");

		Assertions.assertEquals(sensorReport, result);
	}

	@Test
	public void getSensorById_GoPath() {
		Sensor data = new Sensor();
		data.setSensorId("1");

		EntityManager entityManagerMock = Mockito.mock(EntityManager.class);

		SensorFacade facade = new SensorFacade(() -> new Date(1234L), entityManagerMock);
		SensorFacade spy = Mockito.spy(facade);

		Mockito.doReturn(Optional.of(data)).when(spy)
				.getSensorOptById(Mockito.same(entityManagerMock), Mockito.eq("1"));

		Sensor result = spy.getSensorById("1");

		Assertions.assertEquals(data, result);
	}

	@Test
	public void getSensorById_NoResult() {
		// mock the entity manager, configure to return the query
		EntityManager entityManagerMock = Mockito.mock(EntityManager.class);

		SensorFacade facade = new SensorFacade(() -> new Date(1234L), entityManagerMock);

		SensorFacade spy = Mockito.spy(facade);

		Mockito.doReturn(Optional.empty()).when(spy)
				.getSensorOptById(entityManagerMock, "1");

		Assertions.assertThrows(IllegalStateException.class, () -> spy.getSensorById("1"));
	}

	@Test
	public void getSensors_empty() {
		EntityManager em = DatabaseTestUtilities.init();
		EntityTransaction txn = em.getTransaction();
		txn.begin();

		SensorFacade facade = new SensorFacade(() -> new Date(1234L), em);
		List<Sensor> sensors = facade.getSensors(em);

		Assertions.assertTrue(sensors.isEmpty());

		txn.commit();

		DatabaseTestUtilities.clearDatabase(em);
		em.close();
	}

	@Test
	public void getSensorOptional_goPath() {
		EntityManager em = DatabaseTestUtilities.init();
		EntityTransaction txn = em.getTransaction();
		txn.begin();

		// create some sensors, persist to database
		for (int i = 0; i < 3; i++) {
			Sensor sensor = new Sensor();
			sensor.setSensorId(Integer.toString(i));
			sensor.setName("sensor_" + i);
			sensor.setCalibrationData(new CalibrationData());
			sensor.setStatus(new Status());

			em.merge(sensor);
		}

		SensorFacade facade = new SensorFacade(() -> new Date(1234L), em);

		// query one of the sensors
		Optional<Sensor> sensorOpt = facade.getSensorOptById(em, "2");

		Assertions.assertTrue(sensorOpt.isPresent());

		Sensor sensor = sensorOpt.get();
		Assertions.assertEquals("sensor_2", sensor.getName());

		txn.commit();
		DatabaseTestUtilities.clearDatabase(em);

		em.close();
	}

//	@Test
//	public void getSensorOptional_noResult() throws Exception {
//		EntityManager em = DatabaseTestUtilities.init();
//		EntityTransaction txn = em.getTransaction();
//		txn.begin();
//
//		// create some sensors, persist to database
//		for (int i = 0; i < 3; i++) {
//			Sensor sensor = new Sensor();
//			sensor.setSensorId(Integer.toString(i));
//			sensor.setName("sensor_" + i);
//			sensor.setCalibrationData(new CalibrationData());
//			sensor.setStatus(new Status());
//
//			em.merge(sensor);
//		}
//
//		SensorFacade facade = new SensorFacade(() -> new Date(1234L), em);
//
//		// query one of the sensors
//		Optional<Sensor> sensorOpt = facade.getSensorOptById(em, "5");
//
//		Assertions.assertFalse(sensorOpt.isPresent());
//
//		txn.commit();
//		DatabaseTestUtilities.clearDatabase(em);
//		em.close();
//	}

//	@Test
//	public void getSensorOptional_invariantViolation() throws Exception {
//		EntityManager em = DatabaseTestUtilities.init();
//		EntityTransaction txn = em.getTransaction();
//		txn.begin();
//
//		// create some sensors, persist to database
//		for (int i = 0; i < 3; i++) {
//			// each sensor has same ID - note this is *not* the database primary key
//			Sensor sensor = new Sensor();
//			sensor.setSensorId(Integer.toString(1));
//			sensor.setName("sensor_1");
//			sensor.setCalibrationData(new CalibrationData());
//			sensor.setStatus(new Status());
//
//			em.merge(sensor);
//		}
//
//		SensorFacade facade = new SensorFacade(() -> new Date(1234L), em);
//
//		// query one of the sensors
//		Assertions.assertThrows(IllegalStateException.class, () -> facade.getSensorOptById(em, "1"));
//
//		txn.commit();
//		DatabaseTestUtilities.clearDatabase(em);
//		em.close();
//	}

	@Test
	public void testCreateSensor() {
		EntityManager em = DatabaseTestUtilities.init();
		EntityTransaction txn = em.getTransaction();
		txn.begin();

		SensorReportDTO dto1 = createSensorReportDto(
				"1",
				"test1",
				1,
				2,
				3,
				4,
				5,
				1234L);

		SensorReportDTO dto2 = createSensorReportDto(
				"2",
				"test2",
				2,
				3,
				4,
				5,
				6,
				4321L);

		SensorFacade facade = new SensorFacade(() -> new Date(1234L), em);

		em.merge(facade.createSensor(em, dto1));
		em.merge(facade.createSensor(em, dto2));

		List<Sensor> sensors = facade.getSensors(em);
		Assertions.assertEquals(2, sensors.size());

		Sensor sensor1 = sensors.stream().filter(sensor -> sensor.getSensorId().equals("1")).findAny().get();
		Assertions.assertEquals("test1", sensor1.getName());
		List<SensorReport> reports = sensor1.getReports();
		Assertions.assertEquals(1, reports.size());

		SensorReport report = reports.get(0);
		Assertions.assertEquals(1234L, report.getReportTime().getTime());
		Assertions.assertEquals(1, report.getCelsius());
		Assertions.assertEquals(3, report.getHumidity());
		Assertions.assertEquals(4, report.getPressure());
		Assertions.assertEquals(5, report.getIlluminance());

		CalibrationData calibration = sensor1.getCalibrationData();
		Assertions.assertEquals(2, calibration.getOffset());

		txn.commit();

		DatabaseTestUtilities.clearDatabase(em);
		em.close();
	}

	private SensorReportDTO createSensorReportDto(
			String id,
			String name,
			float tempC,
			float offsetC,
			float humidity,
			float pressure,
			float illuminance,
			long reportTime) {
		SensorReportDTO dto = new SensorReportDTO();
		dto.setSensorId(id);
		dto.setName(name);
		dto.setCelsius(tempC);
		dto.setOffset(offsetC);
		dto.setHumidity(humidity);
		dto.setPressure(pressure);
		dto.setIlluminance(illuminance);
		dto.setReportTime(new Date(reportTime));

		return dto;
	}

	@Test
	void temperatureRangeQueryTest() {

		EntityManager em = DatabaseTestUtilities.init();
		EntityTransaction txn = em.getTransaction();
		txn.begin();

		SensorFacade facade = new SensorFacade(() -> new Date(1234L), em);

		// create sensors over a range of time
		SensorReportDTO dto = createSensorReportDto(
				"1",
				"test1",
				1,
				0,
				0,
				0,
				0,
				10000);

		Sensor sensor = facade.createSensor(em, dto);

		for (int i = 2; i <= 10; i++) {
			dto = createSensorReportDto(
					"1",
					"test1",
					i,
					0,
					0,
					0,
					0,
					i*10000);
			facade.updateSensor(sensor, dto);
		}

		em.merge(sensor);

		System.out.println("SENSORS::");
		em.createQuery("SELECT s from Sensor s", Sensor.class).getResultList().forEach(s -> {
			System.out.println("NAME::" + s.getName());
			System.out.println("REPORTS::" + s.getReports().size());
		});
		System.out.println("AFTER");

		// query over a subset of that time range
		List<SensorReport> result = facade.temperatureRangeQuery(em, "1", new Date(11000), new Date(59000));

		Assertions.assertEquals(4, result.size());

		for (int i = 0; i < 4; i++) {
			SensorReport report = result.get(i);
			long time = report.getReportTime().getTime();
			Assertions.assertTrue(time > 11000 && time < 59000);
		}

		txn.commit();

		DatabaseTestUtilities.clearDatabase(em);
		em.close();
	}

	@Test
	public void computeIntervalStart_hour() {
		SensorFacade facade = new SensorFacade(() -> new Date(1234L), null);

		Calendar calendar = Calendar.getInstance();
		calendar.set(2000, 1, 1, 1, 0, 0);
		long result = facade.computeIntervalStart(calendar.getTime(), "hour");

		// input is on hour boundary, so no rounding necessary
		Assertions.assertEquals(calendar.getTimeInMillis(), result);
	}

	@Test
	public void computeIntervalStart_hourRoundDown() {
		SensorFacade facade = new SensorFacade(() -> new Date(1234L), null);

		Calendar calendar = Calendar.getInstance();
		calendar.set(2000, 1, 1, 1, 15, 38);
		long result = facade.computeIntervalStart(calendar.getTime(), "hour");

		// should round down to 0 minutes
		calendar.set(2000, 1, 1, 1, 0, 0);
		Assertions.assertEquals(calendar.getTimeInMillis(), result);
	}

	@Test
	public void computeIntervalStart_hourRoundDownBoundary() {
		SensorFacade facade = new SensorFacade(() -> new Date(1234L), null);

		Calendar calendar = Calendar.getInstance();
		calendar.set(2000, 1, 1, 1, 29, 59);
		long result = facade.computeIntervalStart(calendar.getTime(), "hour");

		// should round down to 0 minutes
		calendar.set(2000, 1, 1, 1, 0, 0);
		Assertions.assertEquals(calendar.getTimeInMillis(), result);
	}

	@Test
	public void computeIntervalStart_hourRoundUp() {
		SensorFacade facade = new SensorFacade(() -> new Date(1234L), null);

		Calendar calendar = Calendar.getInstance();
		calendar.set(2000, 1, 1, 23, 30, 0);
		long result = facade.computeIntervalStart(calendar.getTime(), "hoUr");

		// should round down up to next hour
		calendar.set(2000, 1, 2, 0, 0, 0);
		Assertions.assertEquals(calendar.getTimeInMillis(), result);
	}

	@Test
	public void computeIntervalStart_day() {
		SensorFacade facade = new SensorFacade(() -> new Date(1234L), null);

		Calendar calendar = Calendar.getInstance();
		calendar.set(2000, 9, 1, 0, 0, 0);
		long result = facade.computeIntervalStart(calendar.getTime(), "day");

		// input is on the day boundary, no rounding necessary
		Assertions.assertEquals(calendar.getTimeInMillis(), result);
	}

	@Test
	public void computeIntervalStart_dayRoundDown() {
		SensorFacade facade = new SensorFacade(() -> new Date(1234L), null);

		Calendar calendar = Calendar.getInstance();
		calendar.set(2000, 9, 1, 11, 59, 59);
		long result = facade.computeIntervalStart(calendar.getTime(), "day");

		// output should be rounded down to nearest day
		calendar.set(2000, 9, 1, 0, 0, 0);
		Assertions.assertEquals(calendar.getTimeInMillis(), result);
	}

	@Test
	public void computeIntervalStart_dayRoundUp() {
		SensorFacade facade = new SensorFacade(() -> new Date(1234L), null);

		Calendar calendar = Calendar.getInstance();
		calendar.set(2000, 9, 1, 12, 00, 00);
		long result = facade.computeIntervalStart(calendar.getTime(), "day");

		// output should be rounded down to nearest day
		calendar.set(2000, 9, 2, 0, 0, 0);
		Assertions.assertEquals(calendar.getTimeInMillis(), result);
	}

	@Test
	public void computeIntervalThrows() {
		SensorFacade facade = new SensorFacade(() -> new Date(1234L), null);
		Assertions.assertThrows(RuntimeException.class, () -> facade.computeIntervalStart(new Date(0), "qwerty"));
	}

	@Test
	public void computeInterval_day() {
		SensorFacade facade = new SensorFacade(() -> new Date(1234L), null);
		Assertions.assertEquals(24 * 60 * 60 * 1000, facade.computeIntervalInMilliseconds("Day"));
	}

	@Test
	public void computeInterval_hour() {
		SensorFacade facade = new SensorFacade(() -> new Date(1234L), null);
		Assertions.assertEquals(60 * 60 * 1000, facade.computeIntervalInMilliseconds("hour"));
	}

	@Test
	public void computeInterval_throws() {
		SensorFacade facade = new SensorFacade(() -> new Date(1234L), null);
		Assertions.assertThrows(RuntimeException.class, () -> facade.computeIntervalInMilliseconds("_day"));
	}

	@Test
	public void computeIntervalCount() {
		SensorFacade facade = new SensorFacade(() -> new Date(1234L), null);
		int result = facade.computeIntervalCount(new Date(100), new Date(205), 10);

		Assertions.assertEquals(11, result);
	}

	@Test
	public void rangeToIntervals() {
		long interval = 1000*60*60; // 1 hour in milliseconds
		SensorFacade facade = new SensorFacade(() -> new Date(1234L), null);
		List<SensorReport> reports = Arrays.asList(
				createSensorReport(5, new Date(interval / 2)), // only report in 1st interval
				// no report in 2nd interval, i.e. temp will register as -100
				createSensorReport(25, new Date(interval*2 + 1000)), // 2 reports in 3rd interval, they should be averaged
				createSensorReport(75, new Date(interval*2 + 10001)));

		List<TemperatureDataDto> result = facade.rangeToIntervals(
				3, 0, 1000 * 60 * 60, "hour", reports);

		Assertions.assertEquals(3, result.size());
	}

	@Test
	public void setStatus_goPath() {
		EntityManager managerMock = Mockito.mock(EntityManager.class);

		SensorFacade facade = Mockito.spy(new SensorFacade(() -> new Date(1234L), managerMock));

		StatusDTO statusDto = new StatusDTO();
		statusDto.id = "1234";
		statusDto.state = "online";

		Status status = new Status();
		status.setState("offline");
		Sensor sensor = new Sensor();
		sensor.setStatus(status);

		Mockito.doReturn(Optional.of(sensor)).when(facade).getSensorOptById(managerMock, "1234");

		facade.setStatus(statusDto);

		// verify status has been updated from dto
		Assertions.assertEquals(statusDto.state, status.getState());
	}

	@Test
	public void setStatusTest_firstReportForId() {
		SensorFacade facade = Mockito.spy(new SensorFacade(() -> new Date(1234L), null));

		StatusDTO status = new StatusDTO();
		status.id = "1234";
		status.address = "4321";

		Mockito.doReturn(Optional.empty()).when(facade).getSensorOptById(null,"1234");

		StatusDTO result = facade.setStatus(status);

		Assertions.assertNull(result);
	}

	SensorReport createSensorReport(float tempC, Date reportTime) {
		SensorReport report = new SensorReport();
		report.setCelsius(tempC);
		report.setReportTime(reportTime);
		return report;
	}

	@Test
	public void testGetMinTemperature() {
		EntityManager em = DatabaseTestUtilities.init();
		EntityTransaction txn = em.getTransaction();
		txn.begin();

		// create sensor and some reports
		Sensor sensor = new Sensor();
		sensor.setStatus(new Status());
		sensor.setName("name");
		sensor.setSensorId("1234");
		sensor.setCalibrationData(new CalibrationData());

		List<SensorReport> reports = Arrays.asList(
				createSensorReport(0, new Date(5000)), // lowest temp but is outside the query range, and as such should be ignored
				createSensorReport(25, new Date(10000)),
				createSensorReport(15, new Date(20000)),
				createSensorReport(75, new Date(30000))
		);

		reports.forEach(r -> r.setSensor(sensor));

		sensor.getReports().addAll(reports);

		em.persist(sensor);


		// run min temp query; should return the temperature from the middle report
		SensorFacade facade = new SensorFacade(() -> new Date(1234L), null);

		float result = facade.getMinTemperatureQuery(em, "1234", new Date(9000), new Date(35000));

		Assertions.assertEquals(15, result);

		txn.commit();
		DatabaseTestUtilities.clearDatabase(em);
		em.close();
	}

	@Test
	public void testGetMaxTemperature() {
		EntityManager em = DatabaseTestUtilities.init();
		EntityTransaction txn = em.getTransaction();
		txn.begin();

		// create sensor and some reports
		Sensor sensor = new Sensor();
		sensor.setStatus(new Status());
		sensor.setName("name");
		sensor.setSensorId("1234");
		sensor.setCalibrationData(new CalibrationData());

		List<SensorReport> reports = Arrays.asList(
				createSensorReport(0, new Date(5000)), // lowest temp but is outside the query range, and as such should be ignored
				createSensorReport(115, new Date(10000)),
				createSensorReport(15, new Date(20000)),
				createSensorReport(75, new Date(30000))
		);

		reports.forEach(r -> r.setSensor(sensor));

		sensor.getReports().addAll(reports);

		em.persist(sensor);


		// run max temp query; should return the temperature from the second report
		SensorFacade facade = new SensorFacade(() -> new Date(1234L), null);

		float result = facade.getMaxTemperatureQuery(em, "1234", new Date(9000), new Date(35000));

		Assertions.assertEquals(115, result);

		txn.commit();

		DatabaseTestUtilities.clearDatabase(em);
		em.close();
	}
}
