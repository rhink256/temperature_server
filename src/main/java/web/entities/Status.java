package web.entities;

import web.dto.StatusDTO;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name="status")
public class Status {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pkey")
    private int id;

    @Column(name = "ssid")
    private String ssid;

    @Column(name = "state")
    private String state;

    @Column(name = "signal")
    private int signal;

    @OneToOne(mappedBy = "status", fetch = FetchType.LAZY)
    private Sensor sensor;

    public Status() {

    }

    public Status(String ssid, String state, int signal) {
        this.ssid = ssid;
        this.state = state;
        this.signal = signal;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getSignal() {
        return signal;
    }

    public void setSignal(int signal) {
        this.signal = signal;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public void setSensor(Sensor sensor) {
        this.sensor = sensor;
    }

    public void updateFromDto(StatusDTO dto) {
        ssid = dto.ssid;
        state = dto.state;
        signal = dto.signal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Status status = (Status) o;
        return signal == status.signal
                && Objects.equals(ssid, status.ssid)
                && Objects.equals(state, status.state)
                && Objects.equals(sensor, status.sensor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ssid, state, signal, sensor);
    }

    @Override
    public String toString() {
        return "Status [ssid=" + ssid + ", state=" + state + ", signal=" + signal + ", id=" + id + "]";
    }
}
