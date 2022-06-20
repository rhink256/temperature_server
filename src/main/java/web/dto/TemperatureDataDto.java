package web.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TemperatureDataDto {

    @XmlElement
    public String label;

    @XmlElement
    public float temperatureCelsius;

    public TemperatureDataDto(String label, float temperatureCelsius) {
        this.label = label;
        this.temperatureCelsius = temperatureCelsius;
    }
}
