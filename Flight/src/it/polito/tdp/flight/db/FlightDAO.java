package it.polito.tdp.flight.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.polito.tdp.flight.model.Airline;
import it.polito.tdp.flight.model.AirlineIdMap;
import it.polito.tdp.flight.model.Airport;
import it.polito.tdp.flight.model.AirportIdMap;
import it.polito.tdp.flight.model.Route;
import it.polito.tdp.flight.model.RouteIdMap;

public class FlightDAO {

	//(5)metodi generici che restituiscono liste con le rotte, aereoporti e linee:
	//sono implementati col pattern ORM ovvero sfruttano le IDMAP
	 
	public List<Airline> getAllAirlines(AirlineIdMap airlineIdMap) {
		String sql = "SELECT * FROM airline";
		List<Airline> list = new ArrayList<>();
		try {
			Connection conn = ConnectDB.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet res = st.executeQuery();

			while (res.next()) {
				Airline airline = new Airline(res.getInt("Airline_ID"), res.getString("Name"), res.getString("Alias"),
						res.getString("IATA"), res.getString("ICAO"), res.getString("Callsign"),
						res.getString("Country"), res.getString("Active"));
				list.add(airlineIdMap.get(airline));//alla lista inserisco oggetto già presente o appena aggiunta nell'idmap
			}
			conn.close();
			return list;
			
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	//qui creo i collegamenti incrociati
	public List<Route> getAllRoutes(AirlineIdMap airlineIdMap, AirportIdMap airportIdMap, RouteIdMap routeIdMap) {
		String sql = "SELECT * FROM route";
		List<Route> list = new ArrayList<>();
		try {
			Connection conn = ConnectDB.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet res = st.executeQuery();
			
			int counter = 0;//contatore che diventa l'id della rotta
			while (res.next()) {
				/*(5.1) voglo che nell'oggetto rout compaiono gli oggetti effettivi AIRLINE E AIRPORT*/
				Airport sourceAirport = airportIdMap.get(res.getInt("Source_airport_ID"));
				Airport destinationAirport = airportIdMap.get(res.getInt("Destination_airport_ID"));
				Airline airline = airlineIdMap.get(res.getInt("Airline_ID"));
						
				//creo correttamente dopo le conversioni l'oggetto rotta
				Route route = new Route(counter, airline, sourceAirport, destinationAirport,
						res.getString("Codeshare"), res.getInt("Stops"),
						res.getString("Equipment")); 
				list.add(routeIdMap.get(route));
				counter++;//incremento valore id
				
				//(5.5) da rotta ho creato i riferimenti ai 3 oggett, ora in questi devo aggiungere riferimento a rotta 
				sourceAirport.getRoutes().add(routeIdMap.get(route)); //prendo tutte le rotte dell aereoporto sorgente e gli passo la rotta rout passando dall'IDMAP
				destinationAirport.getRoutes().add(routeIdMap.get(route));
				airline.getRoutes().add(routeIdMap.get(route));
			}	
			conn.close();
			return list;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	public List<Airport> getAllAirports(AirportIdMap airportIdMap) {
		String sql = "SELECT * FROM airport";
		List<Airport> list = new ArrayList<>();
		try {
			Connection conn = ConnectDB.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet res = st.executeQuery();

			while (res.next()) {
				Airport airport = new Airport(res.getInt("Airport_ID"), res.getString("name"), res.getString("city"),
						res.getString("country"), res.getString("IATA_FAA"), res.getString("ICAO"),
						res.getDouble("Latitude"), res.getDouble("Longitude"), res.getFloat("timezone"),
						res.getString("dst"), res.getString("tz")); 
				list.add(airportIdMap.get(airport)); //alla lista inserisco oggetto già presente o appena aggiunta nell'idmap
			}
			conn.close();
			return list;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

//	MAIN PER COTROLLARE 
//	public static void main(String args[]) {
//		FlightDAO dao = new FlightDAO();
//
//		List<Airline> airlines = dao.getAllAirlines();
//		System.out.println(airlines);
//
//		List<Airport> airports = dao.getAllAirports();
//		System.out.println(airports);
//
//		List<Route> routes = dao.getAllRoutes();
//		System.out.println(routes);
//	}

}
