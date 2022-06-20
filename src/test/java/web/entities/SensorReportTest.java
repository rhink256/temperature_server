package web.entities;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;
import web.entities.Sensor;
import web.entities.SensorReport;

class SensorReportTest {

    @Test
    public void equals() {
        Sensor sensor1 = new Sensor();
        sensor1.setName("sensor 1");

        Sensor sensor2 = new Sensor();
        sensor2.setName("sensor 2");

        EqualsVerifier.forClass(SensorReport.class)
                .usingGetClass()
                .withPrefabValues(Sensor.class, sensor1, sensor2)
                .verify();
    }
}