package yorkEngineeringSociety.services;

import yorkEngineeringSociety.models.Event;


public interface EventService {
	Event findByName(String arg0);
	void changeEventAddress(Event event, String arg0); 
	
	void changeEventDate(Event event, int arg0, int arg1, int arg2, int arg3, int arg4);
	
}
