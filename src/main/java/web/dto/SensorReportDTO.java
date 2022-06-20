package web.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;
import java.util.Objects;

@XmlRootElement
public class SensorReportDTO {

    @XmlElement
    private String sensorId = "";

    @XmlElement
    private String name = "";

    @XmlElement
    private float celsius = -999f;

    @XmlElement
    private float offset = -999f;

    @XmlElement
    private float humidity = -999f;

    @XmlElement
    private float pressure = -999f;

    @XmlElement
    private float illuminance = -999f;

    @XmlElement
    private Date reportTime = null;

    @XmlElement
    private int expectedUpdateRateSeconds;

    public String getSensorId() {
        return sensorId;
    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getCelsius() {
        return celsius;
    }

    public void setCelsius(float celsius) {
        this.celsius = celsius;
    }

    public float getOffset() {
        return offset;
    }

    public void setOffset(float offset) {
        this.offset = offset;
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

    public Date getReportTime() {
        return reportTime;
    }

    public void setReportTime(long reportTime) {
        this.reportTime = new Date(reportTime);
    }

    public void setReportTime(Date reportTime) {
        this.reportTime = reportTime;
    }

    public int getExpectedUpdateRateSeconds() {
        return expectedUpdateRateSeconds;
    }

    public void setExpectedUpdateRateSeconds(int expectedUpdateRateSeconds) {
        this.expectedUpdateRateSeconds = expectedUpdateRateSeconds;
    }

    public float getIlluminance() {
        return illuminance;
    }

    public void setIlluminance(float illuminance) {
        this.illuminance = illuminance;
    }

    @Override
    public String toString() {
        return "SensorReportDTO{" +
                "sensorId='" + sensorId + '\'' +
                ", name='" + name + '\'' +
                ", celsius=" + celsius +
                ", offset=" + offset +
                ", humidity=" + humidity +
                ", pressure=" + pressure +
                ", illuminance=" + illuminance +
                ", reportTime=" + reportTime +
                ", expectedUpdateRateSeconds=" + expectedUpdateRateSeconds +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SensorReportDTO that = (SensorReportDTO) o;
        return Float.compare(that.celsius, celsius) == 0 && Float.compare(that.offset, offset) == 0 && Float.compare(that.humidity, humidity) == 0 && Float.compare(that.pressure, pressure) == 0 && Float.compare(that.illuminance, illuminance) == 0 && expectedUpdateRateSeconds == that.expectedUpdateRateSeconds && Objects.equals(sensorId, that.sensorId) && Objects.equals(name, that.name) && Objects.equals(reportTime, that.reportTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sensorId, name, celsius, offset, humidity, pressure, illuminance, reportTime, expectedUpdateRateSeconds);
    }
}
