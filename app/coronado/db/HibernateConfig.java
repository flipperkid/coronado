package coronado.db;

import org.hibernate.cfg.Configuration;

public class HibernateConfig {
	Configuration cfg = new Configuration()
	.setProperty("hibernate.dialect",  "org.hibernate.PostgreSQLDialect")
	.setProperty("hibernate.connection.datasource", "java:comp/env/jdbc/test")
	.setProperty("hibernate.order_updates", "true");
}
