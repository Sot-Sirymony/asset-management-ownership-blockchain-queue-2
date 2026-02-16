Gateway.Builder builder = Gateway.createBuilder()
.identity(wallet, "admin")
.networkConfig(Paths.get("src/main/resources/connection.yaml"))
.discovery(true);




rm -rf target
./mvnw clean package -DskipTests
./mvnw spring-boot:run_
