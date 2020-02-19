/*
 * Copyright 2017-2018 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.micronaut.configuration.dbmigration.liquibase

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
