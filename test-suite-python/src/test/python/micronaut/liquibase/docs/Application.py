# tag::imports[]
from micronaut.runtime import Micronaut
from org.slf4j.bridge import SLF4JBridgeHandler
# end::imports[]


# tag::clazz[]
class Application:

    @staticmethod
    def main(args: list[str]) -> None:
        # Bridge JUL to Slf4j
        SLF4JBridgeHandler.removeHandlersForRootLogger()
        SLF4JBridgeHandler.install()

        Micronaut.run(Application, args)
# end::clazz[]
