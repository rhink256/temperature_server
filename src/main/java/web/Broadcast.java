package web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.websocket.Session;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Broadcast {
    private List<Session> sessions = new ArrayList<>();
    private ObjectMapper mapper = new ObjectMapper();

    private static final Logger LOG = LogManager.getLogger(Broadcast.class);

    public Broadcast() {

    }

    public Broadcast(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public void addSession(Session session) {
        this.sessions.add(session);
    }

    public void removeSession(Session session) {
        this.sessions.remove(session);
    }

    public <T> void send(T message) throws JsonProcessingException {
        String json = mapper.writeValueAsString(message);
        if (LOG.isDebugEnabled()) {
            LOG.debug("broadcast string::\n" + json);
        }
        for (Session session : sessions) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("sending to session::" + session.getId());
            }
            send(json, session);
        }
    }

    private void send(String json, Session session) {
        try {
            session.getBasicRemote().sendText(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
