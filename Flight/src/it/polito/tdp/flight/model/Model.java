package it.polito.tdp.flight.model;

import java.util.ArrayList;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

//LIBRERIA IMPORTATA PER LA DISTANZA: latitudine e londitudine
import com.javadocmd.simplelatlng.LatLng; //crea i due punti
import com.javadocmd.simplelatlng.LatLngTool; //calcola la distanza tra i due punti con l'unita di misura che specifico
import com.javadocmd.simplelatlng.util.LengthUnit; //carica unita di misura

import it.polito.tdp.flight.db.FlightDAO;

public class Model {

	/*UTILIZZO METODO ORM
	 * utilizzo le mappe per evitare doppioni
	 * idmap è una classe che al suo interno ha una mappa che mette in relazione idoggetto
	 * con l'oggetto stesso, e ridefinisce metodo get che restituisce l'oggetto stesso
	 * e put che permette oggetto con attenzione di non inserire duplicai */
	
	FlightDAO fdao = null; //variabile globale per accedere al dao
	
	//(1)creo due liste una per tutte le compagnie una per gli aereoporti e rotte
	List<Airport> airports;
	List<Airline> airlines;
	List<Route> routes;

	//(2)creo identity map --> e relative classi
	AirlineIdMap airlineIdMap;
	AirportIdMap airportIdMap;
	RouteIdMap routeIdMap;

	/*(6)Dopo aver creato il pattern ORM voglio crerae il mio grafo:
	 * lo voglio diretto perche rotte hanno partenza e destinazion, e lo voglio pesato,
	 * con l'info della distanza, ottenibile con la longitudine e latitudine di distanza 
	 * tra i due aereoporti, uso aposita libreria simplelatlng.(vedi su) */
	
	//scelgo grafico in base alle richieste  tramite schema delle slide
	SimpleDirectedWeightedGraph<Airport, DefaultWeightedEdge> grafo;
                              //vertici   archi
	
	
	public Model() {
		fdao = new FlightDAO();//inizzializzo variabile per accedere al dao

		//(3)inizzializzo mappe prima delle lise
		airlineIdMap = new AirlineIdMap();
		airportIdMap = new AirportIdMap();
		routeIdMap = new RouteIdMap();

		//(4)aggiungo info sull IDmap
		//quando inserisco oggetto alla lista, viene anche inserito nella idmap 
		//sfrutto i metodi del dao implementati secondo pattern ORM
		airlines = fdao.getAllAirlines(airlineIdMap);
		System.out.println("Numero line: "+airlines.size()); //stampo dimensione dei dati come debug

		airports = fdao.getAllAirports(airportIdMap);
		System.out.println("Numero aereoporti: "+airports.size()); //stampo dimensione dei dati come debug

		//deve creare i riferimenti incrociati tra i due
		routes = fdao.getAllRoutes(airlineIdMap, airportIdMap, routeIdMap);
		System.out.println("Numero rotte: "+routes.size()); //stampo dimensione dei dati come debug
	}

	
	//(5.8)metodo fatto per il debug richiamata dal test model ma non influente  
	public List<Airport> getAirports() {
		if (this.airports == null) {
			return new ArrayList<Airport>();
		}
		return this.airports;
	}

	
	//(6.1)
	public void createGraph() {  
		//creo grafo.... dichiarazione standard
		grafo = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
		
		//creato grafo aggiungo i vertici dalla lista di aereoporti
		Graphs.addAllVertices(grafo, this.airports);
		
		//aggiungo i collegameti cioè gli archi ed itero sulle rotte
		for (Route r : routes) {
			//aereoporto sorgente e destinazione
			Airport sourceAirport = r.getSourceAirport();
			Airport destinationAirport = r.getDestinationAirport();
			
			//se sono diversi
			if (!sourceAirport.equals(destinationAirport)) {
				//calcolo il peso tramite le distanze in longitudine e latitudine:
				double weight = LatLngTool.distance(new LatLng(sourceAirport.getLatitude(), sourceAirport.getLongitude()),
						                            new LatLng(destinationAirport.getLatitude(), destinationAirport.getLongitude()),
						                            LengthUnit.KILOMETER);
				
				//vado ad inserire l'arco ed il peso appena calcolato
				Graphs.addEdge(grafo, sourceAirport, destinationAirport, weight);
			}
		}
		
		//stampo di default vertici e archi
		System.out.println("\n Numero di vertici per il grafo: "+grafo.vertexSet().size());
		System.out.println("\n Numero di archi per il grafo: "+grafo.edgeSet().size());
	}
	
	
	//(7) quante componenti connesse ha il grafico
	public void printStats() {
		if (grafo != null) {
			this.createGraph();
		}
		//ci dice se grafo è connesso e ci da i set dei nodi all'interno di ogni componenti conesse
		ConnectivityInspector<Airport, DefaultWeightedEdge> ci = new ConnectivityInspector(grafo);
		System.out.println("\n Numero componenti connesse grafo: "+ci.connectedSets().size()); //a me basta stampare la sua dimensione
		
		
	}
	
	//(8) delle componenti connesse voglio il set di nodi più grosso tra le varie componenti
	public Set<Airport> getBiggestSCC() {
		
		ConnectivityInspector<Airport, DefaultWeightedEdge> ci = new ConnectivityInspector(grafo);
		
		//itero su ogni set e mi salvo il più grosso
		Set<Airport> bestSet = null;
		int bestSize = 0;
		
		for (Set<Airport> s : ci.connectedSets()) {
			if (s.size() > bestSize) {
				bestSet = new HashSet<Airport>(s); //faccio deepcopy
				bestSize = s.size();
			}	
		}
		
		return bestSet;
	}

	//(9) prendo il cammino minimo: sequenza di rotte da prendere per spostarmi tra due aereoporti
	public List<Airport> getShortestPath(int id1, int id2) {
		
		//dati gli id mi rifaccio all'aereoporto
		Airport source = airportIdMap.get(id1);
		Airport destination = airportIdMap.get(id2);
		
		System.out.println("\n\nAereoporto di partenza: "+source);
		System.out.println("Aereoporto di destinazione: "+destination);
		
		if (source == null || destination == null) {
			throw new RuntimeException("Gli areoporti selezionati non sono presenti in memoria");
		}
		
		//non ho pesi negativi, uso Dijstra
		                      //vertici, archi  e gli passo riferimento al grafo
		ShortestPathAlgorithm<Airport,DefaultWeightedEdge> spa = new DijkstraShortestPath<Airport, DefaultWeightedEdge>(grafo);
		
		//calcola peso tra i due aereoporti getPathWeight
		double weight = spa.getPathWeight(source, destination);
		System.out.println("Peso del viaggio: "+weight);
		
		//calcola distanza tra due vertici getPath
		GraphPath<Airport,DefaultWeightedEdge> gp = spa.getPath(source, destination);
		
		return gp.getVertexList(); //ritorno la lista di vertici cioè le fermate intermedie
	}

}
