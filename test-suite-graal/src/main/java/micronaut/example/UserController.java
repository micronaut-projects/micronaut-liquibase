package micronaut.example;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;

import java.util.List;

@Controller("/users")
@ExecuteOn(TaskExecutors.IO)
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Get
    public List<User> listUsers() {
        List<User> users = userRepository.findAll();
        return users;
    }
}
