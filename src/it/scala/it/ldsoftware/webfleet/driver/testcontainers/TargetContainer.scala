package it.ldsoftware.webfleet.driver.testcontainers

import com.dimafeng.testcontainers.FixedHostPortGenericContainer
import com.typesafe.scalalogging.LazyLogging
import org.testcontainers.containers.Network
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.containers.wait.strategy.Wait

class TargetContainer(
    val jdbcUrl: String,
    val globalNet: Network,
    val appVersion: String = sys.env.getOrElse("APP_VERSION", "latest"),
    val targetPort: Int = 8080
) extends FixedHostPortGenericContainer(
      imageName = s"index.docker.io/webfleet-driver:$appVersion",
      waitStrategy = Some(Wait.forLogMessage(".*listening on http port.*\n", 1)),
      exposedHostPort = targetPort,
      exposedContainerPort = 8080,
      env = Map(
        "JDBC_DATABASE_URL" -> jdbcUrl,
        "AUTH_ISSUER" -> "auth0",
        "AUTH_AUDIENCE" -> "",
        "AUTH_DOMAIN" -> "ldsoftware.it",
        "LOG_FORMAT" -> "HEROKU"
      )
    )
    with LazyLogging {

  configure { c =>
    c.setNetwork(globalNet)
    c.withLogConsumer(new Slf4jLogConsumer(logger.underlying))
  }

}