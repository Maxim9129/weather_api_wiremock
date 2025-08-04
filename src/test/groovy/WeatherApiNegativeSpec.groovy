import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import groovy.json.JsonSlurper
import org.apache.hc.client5.http.fluent.Request
import spock.lang.Specification

class WeatherApiNegativeSpec extends Specification {

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

    def "error 1002"() {
        given:
        wireMockServer.resetAll()
        when:
        def response = Request.get("http://localhost:8089/v1/current.json").execute().returnResponse()

        def body = response.entity.content.text
        def json = slurper.parseText(body)

        then:
        response.code == 401
        json.error.code == 1002
        json.error.message == "API key not provided."
    }

    def "error 1005"() {
        given:
        wireMockServer.resetAll()
        when:
        def response = Request.get("http://localhost:8089/invalid-path").execute().returnResponse()

        def body = response.entity.content.text
        def json = slurper.parseText(body)

        then:
        response.code == 400
        json.error.code == 1005
        json.error.message == "API request url is invalid."
    }

    def "error 1006"() {
        given:
        wireMockServer.resetAll()
        when:
        def response = Request.get("http://localhost:8089/v1/current.json?key=fake-key&q=invalid").execute().returnResponse()

        def body = response.entity.content.text
        def json = slurper.parseText(body)

        then:
        response.code == 400
        json.error.code == 1006
        json.error.message == "No matching location found."
    }

    def "error 2006"() {
        given:
        wireMockServer.resetAll()
        when:
        def response = Request.get("http://localhost:8089/v1/current.json?key=invalid").execute().returnResponse()

        def body = response.entity.content.text
        def json = slurper.parseText(body)

        then:
        response.code == 401
        json.error.code == 2006
        json.error.message == "API key is invalid."

    }
}
