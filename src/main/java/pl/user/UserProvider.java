package pl.user;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;

@Service
@AllArgsConstructor
public class UserProvider {

    private UserRepository userRepository;

    public User get(Principal principal) {
        return userRepository.findByEmail(principal.getName()).orElseThrow(() -> new UserException(UserError.NOT_FOUND));
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public List<User> getAll() {
        return userRepository.findAll();
    }
}
