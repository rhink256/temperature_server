package web.entities;

import org.hibernate.annotations.SortNatural;
import web.dto.StatusDTO;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name="sensor")
public class Sensor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sensor_database_id ")
    private int dbId;

    @Column(name = "sensor_ip")
    private String sensorId = "";

    @OneToMany(mappedBy = "sensor", cascade = CascadeType.ALL)
    @SortNatural
    @OrderBy("reportTime ASC")
    private List<SensorReport> reports = new ArrayList<>();

    @Column(name = "sensor_name")
    private String name;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    private CalibrationData calibrationData;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    private Status status;

    public int getDbId() {
        return dbId;
    }

    public String getSensorId() {
        return sensorId;
    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }

    public List<SensorReport> getReports() {
        return reports;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CalibrationData getCalibrationData() {
        return calibrationData;
    }

    public void setCalibrationData(CalibrationData calibrationData) {
        this.calibrationData = calibrationData;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sensor sensor = (Sensor) o;
        return Objects.equals(sensorId, sensor.sensorId)
                && Objects.equals(reports, sensor.reports)
                && Objects.equals(name, sensor.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sensorId, reports, name);
    }
}
