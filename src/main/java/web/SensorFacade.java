package web;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import web.dto.SensorReportDTO;
import web.dto.StatusDTO;
import web.dto.TemperatureDataDto;
import web.entities.CalibrationData;
import web.entities.Sensor;
import web.entities.SensorReport;
import web.entities.Status;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.ejb.*;
import javax.persistence.*;
import javax.transaction.Transactional;

@Singleton
public class SensorFacade {
	private Supplier<Date> timeProvider = Date::new;

	@PersistenceContext(unitName  = "tempserver")
	private EntityManager em;

	private static final Logger LOG = LogManager.getLogger(SensorFacade.class);

	public SensorFacade() {

	}

	public SensorFacade(Supplier<Date> timeProvider,
						EntityManager em) {
		this.timeProvider = timeProvider;
		this.em = em;
	}

	SensorReport dtoToReport(SensorReportDTO reportDto) {
		var report = new SensorReport();

		report.setReportTime(reportDto.getReportTime());
		report.setCelsius(reportDto.getCelsius());
		report.setHumidity(reportDto.getHumidity());
		report.setIlluminance(reportDto.getIlluminance());
		report.setPressure(reportDto.getPressure());
		report.setExpectedUpdateRateSeconds(reportDto.getExpectedUpdateRateSeconds());

		return report;
	}

	SensorReportDTO reportToDto(SensorReport report, float offsetDegreesC) {
		var sensor = report.getSensor();

		var dto = new SensorReportDTO();

		dto.setSensorId(sensor.getSensorId());
		dto.setName(sensor.getName());
		dto.setOffset(offsetDegreesC);
		dto.setReportTime(report.getReportTime());
		dto.setCelsius(report.getCelsius());
		dto.setHumidity(report.getHumidity());
		dto.setIlluminance(report.getIlluminance());
		dto.setPressure(report.getPressure());
		dto.setExpectedUpdateRateSeconds(report.getExpectedUpdateRateSeconds());

		return dto;
	}

	@Transactional
	public SensorReportDTO report(SensorReportDTO reportDto) {

		if (reportDto.getReportTime() == null) {
			reportDto.setReportTime(timeProvider.get());
		}

		// persist
		var sensorOptional = getSensorOptById(em, reportDto.getSensorId());

		if (sensorOptional.isPresent()) {
			updateSensor(sensorOptional.get(), reportDto);
		} else {
			Sensor sensor = createSensor(em, reportDto);
			em.merge(sensor);
		}

		return reportDto;
	}

	void updateSensor(Sensor sensor, SensorReportDTO reportDto) {
		var report = dtoToReport(reportDto);
		sensor.getReports().add(report);

		report.setSensor(sensor);

		reportDto.setName(sensor.getName());
		reportDto.setOffset(sensor.getCalibrationData().getOffset());
	}

	Sensor createSensor(EntityManager em, SensorReportDTO reportDto) {
		LOG.info("Creating new sensor::" + reportDto.getSensorId());

		var report = dtoToReport(reportDto);

		var sensor = new Sensor();
		var calibrationData = new CalibrationData();

		sensor.setCalibrationData(calibrationData);
		calibrationData.setSensor(sensor);
		calibrationData.setOffset(reportDto.getOffset());

		var status = new Status();
		sensor.setStatus(status);
		status.setSensor(sensor);

		sensor.setName(reportDto.getName());
		sensor.setSensorId(reportDto.getSensorId());

		sensor.getReports().add(report);
		report.setSensor(sensor);


		return sensor;
	}

	@Transactional
	public SensorReportDTO getLatestReportById(String id) {
		var report = getLatestReport(id);

		float calibrationDegreesC;
		var calibrationData = report.getSensor().getCalibrationData();
		if (calibrationData == null) {
			throw new IllegalStateException("Calibration Data is NULL for sensor with id::" + id);
		} else {
			calibrationDegreesC = report.getSensor().getCalibrationData().getOffset();
		}
		return reportToDto(report, calibrationDegreesC);
	}

	public StatusDTO getStatus(String id) {
		var sensor = getSensorById(id);
		return StatusDTO.fromEntity(sensor);
	}

	List<Sensor> getSensors(EntityManager em) {
		var sensorQuery = em.createQuery("SELECT s FROM Sensor s", Sensor.class);
		return sensorQuery.getResultList();
	}

	@Transactional
	public List<SensorReportDTO> getLatest() {
		var sensors = getSensors(em);

		// get latest report for each sensor
		var result = new ArrayList<SensorReportDTO>();
		for (var s : sensors) {
			SensorReport latest = getLatestReport(s);
			float calibrationDegreesC = s.getCalibrationData().getOffset();
			var dto = reportToDto(latest, calibrationDegreesC);
			result.add(dto);
		}

		return result;
	}

	private SensorReport getLatestReport(Sensor sensor) {
		return sensor.getReports().get(sensor.getReports().size() - 1);
	}

	@Transactional
	public List<StatusDTO> getAllStatus() {
		var sensors = getSensors(em);
		return sensors.stream().map(StatusDTO::fromEntity).collect(Collectors.toList());
	}

	@Transactional
	public StatusDTO setStatus(StatusDTO statusDTO) {
		var sensorOptional = getSensorOptById(em, statusDTO.id);

		if (!sensorOptional.isPresent()) {
			LOG.warn("No Sensor for ID:" + statusDTO);
			return null;
		}

		var sensor = sensorOptional.get();

		var status = sensor.getStatus();

		status.updateFromDto(statusDTO);

		statusDTO.name = sensor.getName();

		return statusDTO;
	}

	public void setName(String id, String name) {
		var sensor = getSensorById(id);
		sensor.setName(name);
	}

	@Transactional
	public void setCalibration(CalibrationData calibration) {
		var sensor = getSensorById(calibration.getSensorId());
		sensor.getCalibrationData().setOffset(calibration.getOffset());
	}

	SensorReport getLatestReport(String id) {
		var sensor = getSensorById(id);
		return getLatestReport(sensor);
	}

	@Transactional
	Sensor getSensorById(String id) {
		var sensorOptional = getSensorOptById(em, id);
		if (sensorOptional.isPresent()) {
			return sensorOptional.get();
		} else {
			throw new IllegalStateException("No sensor found for ID: " + id);
		}
	}

	Optional<Sensor> getSensorOptById(EntityManager em, String id) {
		var query = em.createQuery(
				"SELECT s FROM Sensor s WHERE s.sensorId=:id",
				Sensor.class);

		query.setParameter("id", id);

		var result = query.getResultList();
		if (result.size() == 0) {
			return Optional.empty();
		} else if(result.size() == 1) {
			return Optional.of(result.get(0));
		} else {
			throw new IllegalStateException("Invariant violation: multiple sensors for ID: " + id);
		}
	}

	@Transactional
	public float getMinTemperature(String sensorId, Date start, Date end) {
		return getMinTemperatureQuery(em, sensorId, start, end);
	}

	float getMinTemperatureQuery(EntityManager em, String sensorId, Date start, Date end) {
		var query = em.createQuery(
				"SELECT min(celsius) FROM SensorReport sr WHERE sr.sensor.sensorId=:id AND sr.reportTime BETWEEN :start AND :end",
				Float.class);
		query.setParameter("id", sensorId);
		query.setParameter("start", start);
		query.setParameter("end", end);

		return query.getSingleResult();
	}

	public float getMaxTemperature(String sensorId, Date start, Date end) {
		return getMaxTemperatureQuery(em, sensorId, start, end);
	}

	float getMaxTemperatureQuery(EntityManager em, String sensorId, Date start, Date end) {
		var query = em.createQuery(
				"SELECT max(celsius) FROM SensorReport sr WHERE sr.sensor.sensorId=:id AND sr.reportTime BETWEEN :start AND :end",
				Float.class);
		query.setParameter("id", sensorId);
		query.setParameter("start", start);
		query.setParameter("end", end);

		return query.getSingleResult();
	}

	@Transactional
	public List<TemperatureDataDto> getTemperatureForRange(String sensorId, Date start, Date end, String intervalType) {
		var queryResult = temperatureRangeQuery(em, sensorId, start, end);

		if (queryResult.isEmpty()) {
			return Collections.emptyList();
		}

		long intervalMilliseconds = computeIntervalInMilliseconds(intervalType);
		long intervalStart = computeIntervalStart(start, intervalType);
		int numIntervals = computeIntervalCount(start, end, intervalMilliseconds);

		var result = rangeToIntervals(
				numIntervals, intervalStart, intervalMilliseconds, intervalType, queryResult);

		return result;
	}

	/**
	 * Map an ordered range of sensor reports onto fixed time intervals.
	 * If no reports exist for an interval, the temperature is set to -100C
	 * If multiple reports are found for an interval, they are averaged.
	 * If the list of reports does not end on an interval boundary, the final partial interval is truncated.
	 * @param numIntervals
	 * @param intervalStart
	 * @param intervalMilliseconds
	 * @param intervalType
	 * @param orderedReports
	 */
	List<TemperatureDataDto> rangeToIntervals(
			int numIntervals,
			long intervalStart,
			long intervalMilliseconds,
			String intervalType,
			List<SensorReport> orderedReports) {

		var result = new ArrayList<TemperatureDataDto>();

		float accumulate = 0;
		int numReportsInInterval = 0;

		int resultIndex = 0;
		for (int i = 0; i < numIntervals; i++) {

			// average sensor report temperature data over the interval
			for (int j = resultIndex; j < orderedReports.size(); j++) {
				var report = orderedReports.get(j);
				long currentTime = report.getReportTime().getTime();

				if (currentTime - intervalStart > intervalMilliseconds) {
					resultIndex = j;
					break;
				} else {
					accumulate += report.getCelsius();
					numReportsInInterval++;
				}
			}

			var label = generateLabel(intervalStart, intervalType);

			TemperatureDataDto dto;
			if (numReportsInInterval == 0) {
				dto = new TemperatureDataDto(label, -100);
			} else {
				dto = new TemperatureDataDto(label, accumulate / numReportsInInterval);
			}
			result.add(dto);

			LOG.info("start new interval at::"+intervalStart);
			LOG.info("avg temp in interval::" + dto.temperatureCelsius);

			intervalStart += intervalMilliseconds;
			accumulate = 0;
			numReportsInInterval = 0;
		}

		return result;
	}

	String generateLabel(long intervalStart, String intervalType) {
		String label = "";
		var calendar = Calendar.getInstance();
		calendar.setTimeInMillis(intervalStart);
		LOG.info("calendar:" + calendar);
		if (intervalType.equalsIgnoreCase("hour")) {
			label = Integer.toString(calendar.get(Calendar.HOUR_OF_DAY));
			LOG.info("assigning interval:" + label);
		}
		return label;
	}

	long computeIntervalInMilliseconds(String intervalType) {
		long intervalMilliseconds;
		if (intervalType.equalsIgnoreCase("day")) {
			LOG.info("detected day");
			intervalMilliseconds = 24 * 60 * 60 * 1000;
		} else if (intervalType.equalsIgnoreCase("hour")) {
			LOG.info("detected hour");
			intervalMilliseconds = 60 * 60 * 1000;
		} else {
			throw new RuntimeException("Illegal interval type::" + intervalType);
		}
		return intervalMilliseconds;
	}

	long computeIntervalStart(Date start, String intervalType) {
		long intervalStart;
		if (intervalType.equalsIgnoreCase("day")) {
			var calendar = Calendar.getInstance();
			calendar.setTime(start);

			calendar = roundToDay(calendar);

			intervalStart = calendar.getTimeInMillis();
		} else if (intervalType.equalsIgnoreCase("hour")) {
			var calendar = Calendar.getInstance();
			calendar.setTime(start);
			calendar = roundToHour(calendar);

			intervalStart = calendar.getTimeInMillis();
		} else {
			throw new RuntimeException("Illegal interval type::" + intervalType);
		}
		return intervalStart;
	}

	Calendar roundToDay(Calendar calendar) {
		if (calendar.get(Calendar.HOUR_OF_DAY) >= 12) {
			calendar.add(Calendar.DAY_OF_MONTH, 1);
		}

		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		return calendar;
	}

	Calendar roundToHour(Calendar calendar) {
		if (calendar.get(Calendar.MINUTE) >= 30) {
			calendar.add(Calendar.HOUR, 1);
		}
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		return calendar;
	}

	int computeIntervalCount(Date start, Date end, long intervalMilliseconds) {
		int numIntervals = (int)((end.getTime() - start.getTime()) / intervalMilliseconds);
		if ((end.getTime() - start.getTime()) % intervalMilliseconds > 0) {
			numIntervals++;
		}
		return numIntervals;
	}

	List<SensorReport> temperatureRangeQuery(EntityManager em, String sensorId, Date start, Date end) {
		// get all the data for the entire range. We'll average values over each interval and return that to the caller
		var query = em.createQuery(
				"SELECT sr FROM SensorReport sr WHERE sr.sensor.sensorId=:id " +
						"AND sr.reportTime BETWEEN :start and :end ORDER BY sr.reportTime ASC",
				SensorReport.class);

		query.setParameter("id", sensorId);
		query.setParameter("start", start);
		query.setParameter("end", end);

		return query.getResultList();
	}
}
