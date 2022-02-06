package com.downscribble.quill;

import java.util.Collections;
import java.util.HashMap;

public class AttributeMap extends HashMap<String, Object>{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7775441411232169736L;

	//TODO implement
	public static AttributeMap compose(AttributeMap a, AttributeMap b, boolean keepNull) {
				
		if (a == null) {
			a = new AttributeMap();
		}
		if (b == null) {
			b = new AttributeMap();
		}
		
		AttributeMap attributes = (AttributeMap) b.clone();
		
		if (!keepNull) {
			attributes.values().removeAll(Collections.singleton(null));
		}
		
		for (String key : a.keySet()) {
			if (a.containsKey(key) && !b.containsKey(key)) {
				attributes.put(key, a.get(key));
			}
		}
		
		return attributes.keySet().size() > 0 ? attributes : null;
	}
	
}
