package web.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name="sensor_report")
@XmlRootElement
public class SensorReport implements Comparable<SensorReport> {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "sensor_report_id")
	private int dbId;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "sensor_database_id_fkey")
	private Sensor sensor;

	@Column(name = "CELSIUS")
	@XmlElement
	private float celsius = -999f;

	@Column(name = "HUMIDITY")
	@XmlElement
	private float humidity = -999f;

	@Column(name = "PRESSURE")
	@XmlElement
	private float pressure = -999f;

	@Column(name = "ILLUMINANCE")
	@XmlElement
	private float illuminance = -999f;
	
	@Column(name = "sensor_report_time")
	@Temporal(TemporalType.TIMESTAMP)
	private Date reportTime;

	@Column(name = "expected_update_rate_seconds")
	private int expectedUpdateRateSeconds;

	public float getCelsius() {
		return celsius;
	}

	public void setCelsius(float celsius) {
		this.celsius = celsius;
	}

	public float getHumidity() {
		return humidity;
	}

	public void setHumidity(float humidity) {
		this.humidity = humidity;
	}

	public float getPressure() {
		return pressure;
	}

	public void setPressure(float pressure) {
		this.pressure = pressure;
	}

	public float getIlluminance() {
		return illuminance;
	}

	public void setIlluminance(float illuminance) {
		this.illuminance = illuminance;
	}

	public Date getReportTime() {
		return reportTime;
	}

	public void setReportTime(Date reportTime) {
		this.reportTime = reportTime;
	}

	@JsonIgnore
	public Sensor getSensor() {
		return sensor;
	}

	@JsonIgnore
	public void setSensor(Sensor sensor) {
		this.sensor = sensor;
	}

	public int getExpectedUpdateRateSeconds() {
		return expectedUpdateRateSeconds;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SensorReport that = (SensorReport) o;
		return Float.compare(that.celsius, celsius) == 0
				&& Float.compare(that.humidity, humidity) == 0
				&& Float.compare(that.pressure, pressure) == 0
				&& Float.compare(that.illuminance, illuminance) == 0
				&& expectedUpdateRateSeconds == that.expectedUpdateRateSeconds
				&& Objects.equals(sensor, that.sensor)
				&& Objects.equals(reportTime, that.reportTime);
	}

	@Override
	public int hashCode() {
		return Objects.hash(sensor, celsius, humidity, pressure, illuminance, reportTime, expectedUpdateRateSeconds);
	}

	public void setExpectedUpdateRateSeconds(int expectedUpdateRateSeconds) {
		this.expectedUpdateRateSeconds = expectedUpdateRateSeconds;
	}

	@Override
	public int compareTo(SensorReport o) {
		return reportTime.compareTo(o.reportTime);
	}
}