package com.senacor.service;

import com.senacor.controller.EventController;
import com.senacor.model.Event;
import com.senacor.model.Speech;
import com.senacor.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.util.List;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

/**
 * Created by saba on 09.12.16.
 */
@Service
public class SpeechService {

    EventRepository eventRepository;
    RatingService ratingService;
    ValidationService validationService;

    @Autowired
    public SpeechService(EventRepository eventRepository, RatingService ratingService, ValidationService validationService) {
        this.eventRepository = eventRepository;
        this.ratingService = ratingService;
        this.validationService = validationService;
    }

    //TODO inform rating service of updates

    public void deleteSpeech(String eventId, String speechId) {
        Event event = eventRepository.findByEventId(eventId);
        List<Speech> speechList = event.getSpeeches();
        for (int i = 0; i < speechList.size(); i++) {
            if (speechList.get(i).getSpeechId().equals(speechId)) {
                speechList.remove(i);
            }
        }
        event.setSpeeches(speechList);
        try {
            ratingService.deleteSpeech(speechId);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        eventRepository.save(event);
    }

    public List<Speech> getAllSpeechesForEvent(String eventId) {
        Event event = eventRepository.findOne(eventId);
        for (Speech speech : event.getSpeeches()) {
            Link selflink = linkTo(EventController.class).slash(eventId + "/speeches/" + speech.getSpeechId()).withSelfRel();
            speech.add(selflink);
        }
        return event.getSpeeches();
    }

    public Speech getSpeech(String eventID, String speechID) {
        Event event = eventRepository.findOne(eventID);
        Speech selectedSpeech = new Speech();
        for (Speech speech : event.getSpeeches()) {
            if (speech.getSpeechId().equals(speechID)) {
                selectedSpeech = speech;
                break;
            }
        }
        Link selflink = linkTo(EventController.class).slash(eventID + "/speeches/" + speechID).withSelfRel();
        selectedSpeech.add(selflink);
        return selectedSpeech;
    }

    public Speech addSpeech(String eventId, Speech speech) {
        System.out.println("in speechservice: add speech method");
        Event event = eventRepository.findByEventId(eventId);
        if (event != null) {
            speech.setEventID(eventId);
            System.out.println(speech.getSpeechId());
            List<Speech> speeches = speech.insertSpeechSorted(event.getSpeeches());
            event.setSpeeches(speeches);
            eventRepository.save(event);
            try {
                ratingService.createSpeech(speech);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return speech;
        }
        return null;
    }

    public Speech editSpeech(String eventID, Speech speech) {
        System.out.println("in speechservice: editSpeech");
        Event event = eventRepository.findByEventId(eventID);
        if (event != null) {
            List<Speech> speeches = event.getSpeeches();
            //search for speech in speeches of event
            for (int i = 0; i < speeches.size(); i++) {
                //if speech in speeches has same id as the edited speech-> speech found
                if (speeches.get(i).getSpeechId().equals(speech.getSpeechId())) {
                    //remove the old speech
                    speeches.remove(i);
                    speech.setEventID(eventID);
                    //add the edited speech and insert at the right spot
                    speeches = speech.insertSpeechSorted(speeches);
                    //add the amended list of speeches to the event
                    event.setSpeeches(speeches);
                    //save the event
                    eventRepository.save(event);
                    try {
                        ratingService.editSpeech(speech);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                    return speech;
                }
            }
        }
        return null;
    }


}
