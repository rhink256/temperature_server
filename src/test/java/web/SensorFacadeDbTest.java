//package web;
//
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.Test;
//import web.dto.SensorReportDTO;
//import web.entities.Sensor;
//import web.entities.SensorReport;
//
//import javax.persistence.EntityManager;
//import javax.persistence.EntityManagerFactory;
//import java.util.Date;
//import java.util.List;
//
//
//public class SensorFacadeDbTest {
//    private static EntityManagerFactory emf;
//    private static EntityManager entityManager;
//
//    @BeforeAll
//    public static void init() {
//        emf = DatabaseTestUtilities.createInMemoryEmf();
//
//        entityManager = emf.createEntityManager();
//    }
//
////    @Test
//    public void testReport_createSensor() {
//
//        SensorFacade facade = new SensorFacade(()->new Date(1234L), entityManager);
//
//        String sensorId = "123.45.67";
//
//        SensorReportDTO dto = new SensorReportDTO();
//        dto.setSensorId(sensorId);
//        dto.setName("test");
//        dto.setReportTime(new Date(1234L));
//        dto.setExpectedUpdateRateSeconds(42);
//        facade.report(dto);
//
//        // see if sensor is stored in database
//        Sensor sensor = facade.getSensorById(sensorId);
//
//        Assertions.assertEquals(sensorId, sensor.getSensorId());
//
//        List<SensorReport> reports = sensor.getReports();
//        Assertions.assertEquals(1, reports);
//    }
//}
