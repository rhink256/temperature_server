package web;

import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.fasterxml.jackson.core.JsonProcessingException;
import web.dto.StatusDTO;

@Singleton
@ServerEndpoint("/status/broadcast")
public class StatusBroadcast {

	@EJB
	private SensorFacade facade;

	private Broadcast broadcast = new Broadcast();

	public StatusBroadcast() {

	}

	public StatusBroadcast(SensorFacade facade, Broadcast broadcast) {
		this.facade = facade;
		this.broadcast = broadcast;
	}

	@OnOpen
	public void onOpen(Session session) {
		System.out.println("got websocket connection");
		broadcast.addSession(session);
		for (StatusDTO report : facade.getAllStatus()) {
			try {
				broadcast.send(report);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		}
	}

	@OnClose
	public void onClose(Session session) {
		broadcast.removeSession(session);
	}

	@OnError
	public void error(Session session, Throwable throwable) {
		broadcast.removeSession(session);
	}

	public void send(StatusDTO status) throws JsonProcessingException {
		broadcast.send(status);
	}
}
