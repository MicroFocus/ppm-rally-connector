package com.hp.ppm.integration.rally.model;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class EntityCollectionV1<T> implements Iterable<T>{
	private final List<T> container = new LinkedList<T>();

	public List<T> getCollection(){
		return container;
	}

	public EntityCollectionV1<T> add(T p){
		container.add(p);
		return this;
	}

	@Override
	public Iterator<T> iterator() {
		return container.iterator();
	}
}
