package io.micronaut.liquibase.docs;

// tag::imports[]
import io.micronaut.runtime.Micronaut;
import org.slf4j.bridge.SLF4JBridgeHandler;
// end::imports[]

// tag::clazz[]
public class Application {

    public static void main(String[] args) {
        // Bridge JUL to Slf4j
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        Micronaut.run(Application.class, args);
    }
}
// end::clazz[]
