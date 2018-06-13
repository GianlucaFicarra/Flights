package it.polito.tdp.flight.model;

import java.util.HashMap;
import java.util.Map;

public class AirlineIdMap {
	
	//schema valido per tutte e tre le mappe
	
	private Map<Integer, Airline> map;
	
	public AirlineIdMap() {
		map = new HashMap<>();
	}
	
	public Airline get(int airlineId) {//dato id torna oggetto della mappa corrispondente
		return map.get(airlineId);
	}
	
	public Airline get(Airline airline) { //passo oggetto e mappa controlla di averlo
		Airline old = map.get(airline.getAirlineId());
		if (old == null) {
			map.put(airline.getAirlineId(), airline);
			return airline; //se nuovo lo aggiunge e lo restituisce
		}
		return old; //se gia ce l'ha restituisce il vecchio
	}
	
	public void put(Airline airline, int airlineId) {//per inserire nuovo aereoporto
		map.put(airlineId, airline);
	}
}
