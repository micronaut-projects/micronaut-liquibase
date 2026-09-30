package io.micronaut.liquibase.docs

// tag::imports[]
import io.micronaut.runtime.Micronaut
import org.slf4j.bridge.SLF4JBridgeHandler
// end::imports[]

// tag::clazz[]
object Application {

    @JvmStatic
    fun main(args: Array<String>) {
        // Bridge JUL to Slf4j
        SLF4JBridgeHandler.removeHandlersForRootLogger()
        SLF4JBridgeHandler.install()

        Micronaut.run(Application::class.java, *args)
    }
}
// end::clazz[]
