# java-rest-db-service
Simple exposed REST service to connect to a DB.



## Configuration
To run this project:
1. Clean and compile using Maven: `mvn clean package`
2. Run the application (using default in-memory database): `java -jar target/rest-db-service.jar`
2. _(Optional)_ If you want to point at an external database instead of launching the default in-memory one (H2),
set up an external configuration file as such (example for PostgreSQL database):
    ```yaml
    spring.jpa.properties.hibernate.dialect = org.hibernate.dialect.PostgreSQLDialect
    spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
    spring.datasource.username=postgres
    spring.datasource.password=admin
    ```
    You can then run the application as follows:
    ```bat
    java -jar target/rest-db-service.jar --spring.config.location=file:///C:/path/to/your/configuration.yaml
    ```
    Please check [SpringBoot's JPA DataSource configuration documentation](https://docs.spring.io/spring-boot/docs/3.2.x/reference/html/data.html#data.sql.datasource)
    for further details.