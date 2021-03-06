package yorkEngineeringSociety.controllers;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import yorkEngineeringSociety.models.Event;
import yorkEngineeringSociety.models.User;
import yorkEngineeringSociety.repos.EventRepository;
import yorkEngineeringSociety.repos.UserRepository;
import yorkEngineeringSociety.services.UserService;

@Controller
public class EventController {
	
	@Autowired
	public JavaMailSender emailSender;
	
	@Autowired
	private EventRepository eventRepository;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private UserRepository userRepository;
	
	@ModelAttribute("df")
	public DateFormat dateFormat() {
		DateFormat df = new SimpleDateFormat("MM/d/yy h:mm a");
		return df;
	}
	@ModelAttribute("admin")
	public String isAdmin() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		boolean hasUserRole = authentication.getAuthorities().stream()
		          .anyMatch(r -> r.getAuthority().equals("ROLE_ADMIN"));
		if (hasUserRole)
		{
			return "admin";
		}
		return "user";
		
	}
	
	@ModelAttribute("user")
	public User guestUser() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	      String name = auth.getName();
	      if (name != null) {
	    	  User user = userService.findUserByEmail(name);
	    	  if (user != null)
	    	  {
	    		  return user;
	    	  }
	      }
		User user = new User();
		user.setAdmin(false);
		user.setFirstname("guest");
		return user;
	}
	
	@GetMapping({"/createEvent"})
	public String eventCreate(Model model) {
		return "createEvent";
	}
	
	@PostMapping({"/createEvent"})
	public String eventSave(Model model, @RequestParam String editval,
			@RequestParam String name,  @RequestParam String address, @RequestParam String date) {
		Event event = new Event();
		event.setName(name);
		event.setAddress(address);
		event.setTemplate(editval);
		DateFormat df = new SimpleDateFormat("MM/d/yy h:mm a");
		Date dateobj = new Date();
		try {
			dateobj = df.parse(date);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(date + "endshere");
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(dateobj);
		event.setCalendar(calendar);
		eventRepository.save(event);
		return "redirect:/events";
		
		//find all in user repository
		
		//set up the mail call logic 
		//send it to email: use a for each loop 
		//user.getemail() 
		//email will contain event information
		//have information, just call it and add it to the email body 
		for(User user: userRepository.findAll()) {
			
			//need this to send the email
			//user.getEmail();  
			
			//send the automated email message to all users about the event being created 
			//this gives them the chance to rsvp to the newly created event 
			MimeMessage mimeMessage = emailSender.createMimeMessage();
			MimeMessageHelper helper;
				helper = new MimeMessageHelper(mimeMessage, false, "utf-8");
			helper.setTo(user.getEmail());
			helper.setSubject(event.getName() + "Reminder");
			mimeMessage.setText(event.getTemplate() + "<br></br>" + url, "UTF-8", "html");
			emailSender.send(mimeMessage);
			//return "redirect:/events/" + eventId;
		}
		
		
	}
	
	@GetMapping({"/events"})
	public String events(Model model) {
		model.addAttribute("events", eventRepository.findAll());
		return "events";
	}
	
	@GetMapping({"/events/{eventId}"})
	public String singleEvent(Model model, @PathVariable long eventId) {
		model.addAttribute("event", eventRepository.findOne(eventId));
		return "eventPage";
	}
	
	@GetMapping({"/events/{eventId}/editEvent"})
	public String editEventPage(Model model, @PathVariable long eventId) {
		model.addAttribute("event", eventRepository.findOne(eventId));
		return "editEvent";
	}
	
	@GetMapping({"/events/{eventId}/subscribe"})
	public String subscribeEvent(Model model, @PathVariable long eventId) throws MessagingException {
		Event event = eventRepository.findOne(eventId);
		String url;
		User user = guestUser();
		if (user.getFirstname().matches("guest"))
		{
			return "redirect:/events/" + eventId;
		}
		try {
		user.getSubscribed().add(eventId);
		}
		catch (NullPointerException exception) {
			ArrayList<Long> subscribed = new ArrayList<Long>();
			subscribed.add(eventId);
			user.setSubscribed(subscribed);
		}
		
		userRepository.save(user);
		url = "<a href=\"localhost:8080/events/" + eventId + "\"> Go to Event Page</a>";
		MimeMessage mimeMessage = emailSender.createMimeMessage();
		MimeMessageHelper helper;
			helper = new MimeMessageHelper(mimeMessage, false, "utf-8");
		helper.setTo(user.getEmail());
		helper.setSubject(event.getName() + "Reminder");
		mimeMessage.setText(event.getTemplate() + "<br></br>" + url, "UTF-8", "html");
		emailSender.send(mimeMessage);
		return "redirect:/events/" + eventId;
	}
	
	@PostMapping({"/events/{eventId}/editEvent"})
	public String editEvent(Model model, @PathVariable long eventId, @RequestParam String editval,
			@RequestParam String name,  @RequestParam String address, @RequestParam String date) {
		Event event = eventRepository.findOne(eventId);
		event.setAddress(address);
		event.setName(name);
		event.setTemplate(editval);
		DateFormat df = new SimpleDateFormat("MM/d/yy h:mm a");
		Date dateobj = new Date();
		try {
			dateobj = df.parse(date);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(dateobj);
		event.setCalendar(calendar);
		eventRepository.save(event);
		model.addAttribute("event", event);
		return "eventPage";
	}
}
