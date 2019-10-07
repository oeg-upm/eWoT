package oeg.interoperability.client.service;

import java.net.URLEncoder;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import helio.framework.objects.RDF;
import oeg.interoperability.client.ClientApplication;
import oeg.interoperability.client.ClientQueries;

@Service
public class GraphDBService {

	private Logger log = Logger.getLogger(GraphDBService.class.getName());
    private static final String QUERY_HEAD = "?name=&infer=true&sameAs=true&query=";
	private static List<String> thingsRegistered;
	static {
		thingsRegistered = new CopyOnWriteArrayList<>();
	}
	
	@SuppressWarnings("deprecation")
	public String executeQueryRemotely(String query, String format) {
		String result = "";
		try {
			Unirest.setTimeouts(0, 0);
			String queryRequest = ClientApplication.repositoryEndpoint+QUERY_HEAD+URLEncoder.encode(query)+"&execute=";
			String queryAnswer = Unirest.get(queryRequest).header("Accept",format).asString().getBody();
			result = queryAnswer;
			log.info("query correctly ansewered remotelly");
		} catch (Exception e) {
			log.severe(e.toString());
		}
		
		return result;
	}
	
	
	@Scheduled(fixedDelay = 60000)
	public void updateThingsURLs() {
		@SuppressWarnings("deprecation")
		String thingRequest = ClientApplication.repositoryEndpoint+QUERY_HEAD+URLEncoder.encode(ClientQueries.SELECT_QUERY_FIND_WOT_THINGS)+"&execute=";
		String thingsURLs;
		try {
			Unirest.setTimeouts(0, 0);
			thingsURLs = Unirest.get(thingRequest).asString().getBody();
			String[] urls = thingsURLs.split("\n");
			thingsRegistered.clear();
			for(int index=1; index < urls.length; index++) {
				String thingURL = urls[index].trim();
				thingsRegistered.add(thingURL);
			}
			log.info("Things registered: "+thingsRegistered.size());
		} catch (UnirestException e) {
			log.severe(e.toString());
			log.severe("No endpoint for the description repository was provided, or repository endpoint is down!");
		}
	}
	
	
	public RDF retrieveThingRDF(String thingIri) {
		RDF tedFiltered = new RDF();

		try {
			String thingRequest = ClientApplication.repositoryEndpoint
					+ QUERY_HEAD + URLEncoder
							.encode("DESCRIBE * FROM <" + thingIri + ">  {\n" + "    	?s ?p ?o .\n" + "}", "UTF-8")
					+ "&execute=";

			String thingRDF = Unirest.get(thingRequest).asString().getBody();
			tedFiltered.parseRDF(thingRDF);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return tedFiltered;
	}
	
	public Boolean executeQueryAskLocally(RDF thing, String queryASKInstantiated) {
		Boolean isRelevant =false; 
		try {
				Query query = QueryFactory.create(queryASKInstantiated) ;
				QueryExecution qexec = QueryExecutionFactory.create(query, thing.getRDF());
				isRelevant = qexec.execAsk(); 
			    qexec.close();
		  } catch (Exception e) {
			  e.printStackTrace();
		  }
		return isRelevant;
	}
	
	
	public RDF retrieveResourcesOutsideRDF(RDF thingRDF) {
		RDF result = new RDF();
		List<RDFNode> objects = thingRDF.getRDF().listObjects().toList();
		for(int index=0; index < objects.size(); index++) {
			RDFNode objectNode = objects.get(index);
			if(objectNode.isResource()) {
				String object = objectNode.toString();
				String queryASKInstantiated = ClientQueries.instanciateAskQueryWithSubject(object);
				if(!executeQueryAskLocally(thingRDF, queryASKInstantiated)) {
					RDF extraData = retrieveThingRDF(object);
					result.addRDF(extraData);
				}
			}
		}
		
		return result;		
	}
	
	
	public static List<String> getThingsRegistered() {
		return thingsRegistered;
	}
	
}
