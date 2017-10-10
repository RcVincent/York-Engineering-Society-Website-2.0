package yorkEngineeringSociety.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import yorkEngineeringSociety.models.User;
import yorkEngineeringSociety.repos.UserRepository;
import yorkEngineeringSociety.services.UserService;

@Service("userService")
public class UserServerImpl implements UserService {
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	public User findUserByEmail(String email) {
		return this.userRepository.findByEmail(email);
	}

	public void saveUser(User user) {
		user.setPassword(this.bCryptPasswordEncoder.encode(user.getPassword()));
		this.userRepository.save(user);
	}

	public User userLogin(String email, String password) {
		User user = this.userRepository.findByEmail(email);
		return user == null ? null : (!this.bCryptPasswordEncoder.matches(password, user.getPassword()) ? null : user);
	}
}