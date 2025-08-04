import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import groovy.json.JsonSlurper
import org.apache.hc.client5.http.fluent.Request
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Specification
import spock.lang.Unroll

class WeatherApiPositiveSpec extends Specification {

    private static final Logger log = LoggerFactory.getLogger(WeatherApiPositiveSpec)

    WireMockServer wireMockServer
    JsonSlurper slurper = new JsonSlurper()

    def setup() {
        wireMockServer = new WireMockServer(
                WireMockConfiguration.wireMockConfig()
                        .port(8089).withRootDirectory(".")
        )
        wireMockServer.start()
    }

    def cleanup() {
        wireMockServer.stop()
    }

    @Unroll
    def "Check weather #city"() {
        given:
        wireMockServer.resetAll()
        def response = Request.get("http://localhost:8089/v1/current.json?key=fake-key&q=$city").execute().returnContent().asString()
        def result = slurper.parseText(response)

        and: "Check and Log"
        boolean hasMismatch = false

        hasMismatch |= logIfMismatch(city, result.location?.name, "location.name")
        hasMismatch |= logIfMismatch(expectedTemp, result.current?.temp_c, "current.temp_c")
        hasMismatch |= logIfMismatch(expectedCondition, result.current?.condition?.text, "current.condition.text")

        expect: "all values are the same"
        !hasMismatch

        where:
        city        | expectedTemp | expectedCondition
        "London"    | 18.4         | "Partly cloudy"
        "Paris"     | 28.3         | "Partly Cloudy"
        "Tokyo"     | 26.2         | "Light rain shower"
        "Moscow"    | 26.1         | "Sunny"
    }

    boolean logIfMismatch(expected, actual, String path) {
        if (expected != actual) {
            log.warn("Mismatch $path expected: '${expected}' but got: '${actual}'")
            return true
        }
        return false
    }
}
