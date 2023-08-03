package com.julianduru.cdc.examples;

import com.julianduru.cdc.BaseServiceIntegrationTest;
import com.julianduru.cdc.CdcConnectorLibAutoConfiguration;
import com.julianduru.cdc.DataCaptureMap;
import com.julianduru.cdc.config.SourceConnector;
import com.julianduru.cdc.config.TestConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@Slf4j
@SpringBootTest(
	classes = {
		TestConfig.class,
		CdcConnectorLibAutoConfiguration.class
	}
)
@EnableAutoConfiguration
class CdcConnectorLibApplicationTests extends BaseServiceIntegrationTest {


	@Value("classpath:users.sql")
	private Resource usersData;


	@Autowired
	private JdbcTemplate jdbcTemplate;


	@Autowired
	private DataCaptureMap dataCaptureMap;


	@Test
	void dbConnectIntegration() throws Exception {
		Thread.currentThread().join(2000L);

		try (InputStream inputStream = usersData.getInputStream()) {
			String sql = IOUtils.toString(inputStream, StandardCharsets.UTF_8.toString());
			log.debug("About to execute SQL: {}", sql);

			jdbcTemplate.batchUpdate(
				Arrays
					.stream(sql.split("\n"))
					.filter(s -> !s.trim().isEmpty())
					.toArray(String[]::new)
			);

			await()
				.atMost(Duration.ofSeconds(2))
				.until(() -> !dataCaptureMap.isEmpty());

			Thread.sleep(2000L);

			assertThat(dataCaptureMap.size()).isEqualTo(2);

			//TODO: add extra validation for content of dataCaptureMap.
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		finally {
			log.info("Bootstrap Server: " + SourceConnector.KAFKA_BOOTSTRAP_SERVERS_OVERRIDE);
		}

		if ("true".equalsIgnoreCase(System.getenv("INSPECTION_ENABLED"))) {
			Thread.currentThread().join();
		}
	}


}

