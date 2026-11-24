package config;

import account.Account;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import user.User;

@Configuration
@ComponentScan({"account", "user", "console", "operation", "config"})
@PropertySource("classpath:application.properties")
public class ApplicationConfig {
    @Bean(destroyMethod = "close")
    public SessionFactory sessionFactory(
            @Value("${db.driver}") String driver,
            @Value("${db.url}") String url,
            @Value("${db.username}") String username,
            @Value("${db.password}") String password,
            @Value("${db.dialect}") String dialect,
            @Value("${hibernate.hbm2ddl.auto}") String hbm2ddl,
            @Value("${hibernate.show_sql}") String showSql,
            @Value("${hibernate.format_sql}") String formatSql,
            @Value("${hibernate.current_session_context_class}") String sessionContext
    ) {
        org.hibernate.cfg.Configuration configuration = new org.hibernate.cfg.Configuration();
        configuration
                .addAnnotatedClass(Account.class)
                .addAnnotatedClass(User.class)
                .setProperty("hibernate.connection.driver_class", driver)
                .setProperty("hibernate.connection.url", url)
                .setProperty("hibernate.connection.username", username)
                .setProperty("hibernate.connection.password", password)
                .setProperty("hibernate.dialect", dialect)
                .setProperty("hibernate.hbm2ddl.auto", hbm2ddl)
                .setProperty("hibernate.show_sql", showSql)
                .setProperty("hibernate.format_sql", formatSql)
                .setProperty("hibernate.current_session_context_class", sessionContext);

        return configuration.buildSessionFactory();
    }
}
