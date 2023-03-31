package io.micronaut.liquibase

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post

@Controller
class MyController {

    @Post
    String index(Dto dto) {
        "Hello $dto.name"
    }
}
