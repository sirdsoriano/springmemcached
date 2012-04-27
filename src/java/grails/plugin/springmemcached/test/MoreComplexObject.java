package grails.plugin.springmemcached.test;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class MoreComplexObject implements Serializable {
	public Long getIdRef() {
		return idRef;
	}

	public void setIdRef(Long idRef) {
		this.idRef = idRef;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<String> getOtherInfo() {
		return otherInfo;
	}

	public void setOtherInfo(List<String> otherInfo) {
		this.otherInfo = otherInfo;
	}

	Long idRef;
	String description;
	List<String> otherInfo;
	Map<String,Long> numbers;
	
	@Override
	public String toString(){
		return "MoreComplexObject@$"+idRef+"[description:"+description+", otherInfo:"+otherInfo+", numbers:"+numbers+"]";
	}

}
