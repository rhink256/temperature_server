package web.dto;

import web.entities.Sensor;
import web.entities.Status;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class StatusDTO {
	@XmlElement
	public String ssid;
	@XmlElement
	public String address;
	@XmlElement
	public String state;
	@XmlElement
	public int signal;
	@XmlElement
	public String id = "";
	@XmlElement
	public String name = "";

	public StatusDTO() {

	}

	public static StatusDTO fromEntity(Sensor sensor) {
		Status status = sensor.getStatus();

		StatusDTO dto = new StatusDTO();
		dto.ssid = status.getSsid();
		dto.address = status.getState();
		dto.state = status.getState();
		dto.signal = status.getSignal();

		dto.name = sensor.getName();
		dto.id = sensor.getSensorId();

		return dto;
	}

	public StatusDTO(String ssid, String address, String state, int signal, String id, String name) {
		this.ssid = ssid;
		this.address = address;
		this.state = state;
		this.signal = signal;
		this.id = id;
		this.name = name;
	}

	@Override
	public String toString() {
		return "Status [ssid=" + ssid + ", address=" + address + ", state=" + state + ", signal=" + signal + ", id="
				+ id + ", name=" + name + "]";
	}
}