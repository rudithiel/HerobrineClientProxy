package org.koekepan.herobrineproxy.behaviour;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BehaviourHandler<T> {
	
	private final Map<Class<? extends T>, Behaviour<T>> behaviours = new ConcurrentHashMap<Class<? extends T>, Behaviour<T>>();
	
	public void registerBehaviour(Class<? extends T> type, Behaviour<T> behaviour) {
		if (behaviour != null) {
			behaviours.put(type, behaviour);
		} else {
			behaviours.remove(type);
		}
	}
		
	
	public Behaviour<T> getBehaviour(Class<? extends T> type) {
		return behaviours.get(type);		
	}

	
	public boolean hasBehaviour(Class<? extends T> type) {
		return behaviours.containsKey(type);
	}

	
	public void clearBehaviours() {
		behaviours.clear();
	}

	
	public Set<Class<? extends T>> getTypes() {
		return new HashSet<Class<? extends T>>(behaviours.keySet());
	}

	public void process(T object) {
		Behaviour<T> behaviour = behaviours.get(object.getClass());
		if (behaviour != null) {
			behaviour.process(object);
		}	
	}

}
