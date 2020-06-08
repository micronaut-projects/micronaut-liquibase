package io.micronaut.liquibase

trait YamlAsciidocTagCleaner {

    String cleanYamlAsciidocTag(String str, String tagName = 'yamlconfig') {
        str.replaceAll('//tag::' + tagName + '\\[]', '').replaceAll('//end::' + tagName + '\\[]', '').trim()
    }

    Map<String, Object> flatten(Map m, String prefix = "", Map<String, Object> newMap = [:]) {
        m.forEach({key, value ->
            if (value instanceof Map) {
                flatten(value, prefix + key.toString() + ".", newMap)
            } else {
                newMap.put(prefix + key.toString(), value)
            }
        })

        return newMap
    }
}
