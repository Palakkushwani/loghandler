package com.loghandler.component;

import static com.loghandler.utils.LogHandlerConstants.CONS_HOST;
import static com.loghandler.utils.LogHandlerConstants.CONS_ID;
import static com.loghandler.utils.LogHandlerConstants.CONS_STATE;
import static com.loghandler.utils.LogHandlerConstants.CONS_TS;
import static com.loghandler.utils.LogHandlerConstants.CONS_TYPE;
import static com.loghandler.utils.LogHandlerConstants.EVENT_ST_FINISHED;
import static com.loghandler.utils.LogHandlerConstants.EVENT_ST_STARTED;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.loghandler.dao.LogHandlerDao;
import com.loghandler.model.DBEvent;
import com.loghandler.model.Event;
import com.loghandler.utils.LogHandlerException;

public class EventHandler {
	private static final Logger LOGGER = Logger.getLogger("EventHandler");
	
	/*
	 * Reads the input file one line at a time
	 * and creates an eventMap with id as the key, and all the corresponding events as list in value
	 */
	public void prepareEventListMap(String inputFilePath) throws LogHandlerException {
		Map<String, List<Event>> eventMap = new HashMap<>();
		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFilePath))){
			JSONParser jsonParson = new JSONParser();
			String currentLine;
			while((currentLine =bufferedReader.readLine())!=null) {
				try {
					JSONObject jsonLine = (JSONObject)jsonParson.parse(currentLine);
					Event event = new Event((String)jsonLine.get(CONS_ID), (String)jsonLine.get(CONS_STATE),(String)jsonLine.get(CONS_TYPE),(String)jsonLine.get(CONS_HOST),(long)jsonLine.get(CONS_TS));
					if(eventMap.get(event.getId())!=null) {
						eventMap.get(event.getId()).add(event);
					} else {
						List<Event> eventList = new ArrayList<>();
						eventList.add(event);
						eventMap.put(event.getId(), eventList);
					} 
				}catch (ParseException e) {
					LOGGER.severe("Got error - while handling the json - skipping.." + e.getMessage());
				}
			}
		} catch (IOException e) {
			throw new LogHandlerException("Got error while handling the file - please check -" + e.getMessage());
		} 		
		parseEventListMap(eventMap);
	}
	
	/*
	 * Navigates through the list on basis of event id
	 * and calculates the duration
	 */
	private void parseEventListMap(Map<String, List<Event>> eventListMap) {
		List<DBEvent> dbEventList = new ArrayList<>();
		for(List<Event> listEvent : eventListMap.values()) {
			if(listEvent.size()!=2) {
				LOGGER.info("The id does not have start and finish event, skipping ");
			} else {
				Event startEvent =  null;
				Event finishEvent =null;
						
				for(Event event : listEvent) {
					if(EVENT_ST_STARTED.equalsIgnoreCase(event.getState())) {
						startEvent =event;
					} else if (EVENT_ST_FINISHED.equalsIgnoreCase(event.getState())){
						finishEvent=event;
					}
				}
				if(finishEvent!=null && startEvent!=null) {
					long duration = finishEvent.getTimeStamp()-startEvent.getTimeStamp();
					DBEvent dbEvent = new DBEvent(startEvent.getId(),duration,startEvent.getType(),startEvent.getHost(),duration>4?true:false);
					dbEventList.add(dbEvent);
				} else {
					LOGGER.info("Start or finish event not found - skipping");
				}
			}
		}
		if(dbEventList!=null && dbEventList.size()!=0) {
			LogHandlerDao logHandlerDao = new LogHandlerDao();
			logHandlerDao.handleSave(dbEventList);
		}
	}
	
}
