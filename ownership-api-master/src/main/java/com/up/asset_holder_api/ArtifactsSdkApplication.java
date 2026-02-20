package com.up.asset_holder_api;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import org.hyperledger.fabric.gateway.Gateway;

import com.up.asset_holder_api.helper.GatewayHelperV1;


@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "Ownership",
        version = "v1",
        description = "This is Ownership Blockchain spring SDK"
))
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        in = SecuritySchemeIn.HEADER
)

@EnableScheduling
public class ArtifactsSdkApplication {
    static {
         System.setProperty("org.hyperledger.fabric.sdk.service_discovery.as_localhost", "true");
    }
    public static void main(String[] args) {
        SpringApplication.run(ArtifactsSdkApplication.class, args);

    }
}
