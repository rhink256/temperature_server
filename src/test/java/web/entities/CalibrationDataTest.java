package web.entities;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class CalibrationDataTest {
    @Test
    public void testEquals() {
        Sensor sensor1 = new Sensor();
        sensor1.setSensorId("1");

        Sensor sensor2 = new Sensor();
        sensor2.setSensorId("2");

        EqualsVerifier.forClass(CalibrationData.class)
                .withPrefabValues(Sensor.class, sensor1, sensor2)
                .usingGetClass().verify();
    }
}