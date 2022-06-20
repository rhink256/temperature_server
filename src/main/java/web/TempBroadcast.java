package web;

import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.fasterxml.jackson.core.JsonProcessingException;
import web.dto.SensorReportDTO;

@Singleton
@ServerEndpoint("/temp/broadcast")
public class TempBroadcast {

	@EJB
	private SensorFacade facade;
	private Broadcast broadcast = new Broadcast();

	public TempBroadcast() {

	}

	public TempBroadcast(SensorFacade facade, Broadcast broadcast) {
		this.facade = facade;
		this.broadcast = broadcast;
	}

	@OnOpen
	public void onOpen(Session session) {
		System.out.println("got websocket connection");
		broadcast.addSession(session);
		for (SensorReportDTO report : facade.getLatest()) {
			try {
				send(report);
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

	public void send(SensorReportDTO status) throws JsonProcessingException {
System.out.println("tempbroadcast sending::"+status);
		broadcast.send(status);
	}
}
