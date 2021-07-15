package io.micronaut.liquibase.docs
//tag::clazz[]
import grails.gorm.annotation.Entity

import io.micronaut.context.annotation.Requires
import io.micronaut.core.annotation.Introspected

@Introspected
@Entity
//end::clazz[]
@Requires(property = 'spec.name', value = 'GormDocSpec')
//tag::clazz[]
class Book {
    String name
}
//end::clazz[]
