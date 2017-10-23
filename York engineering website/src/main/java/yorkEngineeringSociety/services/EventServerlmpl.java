package yorkEngineeringSociety.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import yorkEngineeringSociety.models.Event;
import yorkEngineeringSociety.repos.EventRepository;
import yorkEngineeringSociety.services.EventService;

@Service("eventService")
public class EventServerlmpl {
	@Autowired
	private EventRepository eventRepository;
	
	//@Autowired
	
	public Event findByName(String name) {
		return this.eventRepository.findByName(name);
	}
}