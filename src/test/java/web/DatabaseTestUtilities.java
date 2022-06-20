package web;

import org.hibernate.jpa.HibernatePersistenceProvider;

import javax.persistence.*;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;
import java.io.*;
import java.net.URL;

import java.util.*;

public class DatabaseTestUtilities {


    public static EntityManager init() {
        Map<String, Object> config = new HashMap<>();
        config.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQL10Dialect");
        config.put("hibernate.show_sql", "true");
        config.put("hibernate.hbm2ddl.auto", "update");
        config.put("hibernate.connection.driver_class", "org.h2.Driver");
        config.put("hibernate.connection.url", "jdbc:h2:mem:test");
        config.put("hibernate.connection.username", "sa");
        config.put("hibernate.connection.password", "sa");

        EntityManagerFactory emf = new HibernatePersistenceProvider().createContainerEntityManagerFactory(
                new CustomPersistenceUnitInfo(), config);

        EntityManager entityManager = emf.createEntityManager();
        return entityManager;
    }

    public static void clearDatabase(EntityManager em) {
        em.getTransaction().begin();

        em.createQuery("DELETE FROM SensorReport").executeUpdate();
        em.createQuery("DELETE FROM Sensor").executeUpdate();
        em.createQuery("DELETE FROM Status").executeUpdate();
        em.createQuery("DELETE FROM CalibrationData").executeUpdate();

        em.getTransaction().commit();
    }

    private static class CustomPersistenceUnitInfo implements PersistenceUnitInfo {

        @Override
        public String getPersistenceUnitName() {
            return "test";
        }

        @Override
        public String getPersistenceProviderClassName() {
            return "org.hibernate.jpa.HibernatePersistenceProvider";
        }

        @Override
        public PersistenceUnitTransactionType getTransactionType() {
            return PersistenceUnitTransactionType.RESOURCE_LOCAL;
        }

        @Override
        public DataSource getJtaDataSource() {
            return null;
        }

        @Override
        public DataSource getNonJtaDataSource() {
            return null;
        }

        @Override
        public List<String> getMappingFileNames() {
            return Collections.emptyList();
        }

        @Override
        public List<URL> getJarFileUrls() {
            try {
                return Collections.list(this.getClass()
                        .getClassLoader()
                        .getResources(""));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        @Override
        public URL getPersistenceUnitRootUrl() {
            return null;
        }

        @Override
        public List<String> getManagedClassNames() {
            return Arrays.asList(
                    "com.app.Entity1",
                    "com.app.Entity2"
            );
        }

        @Override
        public boolean excludeUnlistedClasses() {
            return true;
        }

        @Override
        public SharedCacheMode getSharedCacheMode() {
            return null;
        }

        @Override
        public ValidationMode getValidationMode() {
            return null;
        }

        @Override
        public Properties getProperties() {
            return null;
        }

        @Override
        public String getPersistenceXMLSchemaVersion() {
            return null;
        }

        @Override
        public ClassLoader getClassLoader() {
            return null;
        }

        @Override
        public void addTransformer(final ClassTransformer classTransformer) {

        }

        @Override
        public ClassLoader getNewTempClassLoader() {
            return null;
        }
    }
}
