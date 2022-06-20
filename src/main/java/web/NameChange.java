package web;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class NameChange {
	@XmlElement
	public String id = "";
	
	@XmlElement
	public String name = "";

	@Override
	public String toString() {
		return "NameChange [id=" + id + ", name=" + name + "]";
	}
}
