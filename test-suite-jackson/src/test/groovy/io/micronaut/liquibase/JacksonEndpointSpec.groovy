package io.micronaut.liquibase

import io.micronaut.context.annotation.Property
import io.micronaut.core.type.Argument
import io.micronaut.core.util.StringUtils
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest
class JacksonEndpointSpec extends Specification {

    @Inject
    @Client("/")
    HttpClient client

    void "can read endpoint using Jackson"() {
        when:
        def response = client.toBlocking().retrieve(HttpRequest.GET("/liquibase"), Argument.listOf(Map))

        then:
        response.name == ["default"]
        response.changeSets.author == [['sdelamo', 'sdelamo']]
        response.changeSets.description == [['createTable tableName=books', 'insert tableName=books; insert tableName=books']]
    }

    def "serialization still works as expected"() {
        when:
        def helloResponse = client.toBlocking().retrieve(HttpRequest.POST("/", new Dto(name: 'Tim')))

        then:
        helloResponse == "Hello Tim"
    }
}
