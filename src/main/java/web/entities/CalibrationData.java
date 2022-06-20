package web.entities;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlElement;
import java.util.Objects;

@Entity
@Table(name = "CALIBRATION_DATA")
public class CalibrationData {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "CalibrationDataID")
	private int dbId;

	@Column(name = "sensor_id")
	@XmlElement
	private String sensorId = "";

	@Column(name = "temp_offset")
	@XmlElement
	private float offset;

	@OneToOne(mappedBy="calibrationData", fetch = FetchType.LAZY)
	@JoinColumn(name="sensor_database_id_fkey")
	private Sensor sensor;

	public int getDbId() {
		return dbId;
	}

	public void setDbId(int dbId) {
		this.dbId = dbId;
	}

	public float getOffset() {
		return offset;
	}

	public void setOffset(float offset) {
		this.offset = offset;
	}

	public String getSensorId() {
		return sensorId;
	}

	public void setSensorId(String sensorId) {
		this.sensorId = sensorId;
	}

	public Sensor getSensor() {
		return sensor;
	}

	public void setSensor(Sensor sensor) {
		this.sensor = sensor;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CalibrationData that = (CalibrationData) o;
		return Float.compare(that.offset, offset) == 0 && Objects.equals(sensorId, that.sensorId) && Objects.equals(sensor, that.sensor);
	}

	@Override
	public int hashCode() {
		return Objects.hash(sensorId, offset, sensor);
	}

	@Override
	public String toString() {
		return "CalibrationData [id=" + sensorId + ", offset=" + offset + "]";
	}
}
