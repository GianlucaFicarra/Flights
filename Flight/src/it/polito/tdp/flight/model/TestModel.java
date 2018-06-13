package it.polito.tdp.flight.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TestModel {

	//(5.7) fatto per verificarese se le relazioni sono state fatte correttamente
	//consigliato perche previene erroriiiiiiiiiiiiiiiiii
	
	public static void main(String args[]) {
		
		Model m = new Model(); 
		
		Airport a = m.getAirports().get(0);// salvo il primo aereoporto della lista 
		System.out.println("Stampo l'aereoporto: "+a); //stampo aereoporte e le sue rotte
		System.out.println("Stampo le rotte: "+a.getRoutes()); //
		
		//creo il graf
		m.createGraph();
		
		//vedo quante componenti cnnesse del grafo
		m.printStats();
		
		//delle componenti connesse voglio il set di nodi più grosso tra le varie componenti
		Set<Airport> biggestSCC = m.getBiggestSCC();
		System.out.println("\nDimensione del set di nodi più grosso: "+biggestSCC.size());
		
		// sequenza di rotte da prendere per spostarmi tra due aereoporti
		try {
		
			//prendo gli id dei due aereoporti che mi interessano
			List<Airport> airportList = new ArrayList<Airport>(biggestSCC);
			int id1 = airportList.get(0).getAirportId();
			int id2 = airportList.get(15).getAirportId();
		
			//mi chiedo se i due sono collegati, se si mi dica il percorso più piccolo
			System.out.println("Stampo gli aereoporti intermedi: "+m.getShortestPath(id1, id2));
			
		} catch (RuntimeException e) {
			System.out.println("Airport code error.");
		}

	}
}
