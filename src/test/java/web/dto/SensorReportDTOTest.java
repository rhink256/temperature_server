package web.dto;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

public class SensorReportDTOTest {
    @Test
    public void testEquals() {
        EqualsVerifier.simple().forClass(SensorReportDTO.class).usingGetClass().verify();
    }
}
