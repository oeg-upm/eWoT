package oeg.interoperability.client.service;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ch.qos.logback.core.net.SyslogOutputStream;
import helio.components.engine.EngineImp;
import helio.framework.mapping.Mapping;
import helio.framework.objects.RDF;
import helio.mappings.translators.WotTranslator;
import oeg.interoperability.client.ClientApplication;
import oeg.interoperability.client.ClientQueries;
import oeg.interoperability.client.jena.QueryVisitor;


@Service
public class SemanticInteroperabilityService {

	// -- Attributes
	@Autowired
	public GraphDBService graphDBService;
	private static final RDF EMPTY_TED = new RDF();
	private static final Property TED_PREDICATE_HAS_COMPONENTS;
	private Logger log = Logger.getLogger(SemanticInteroperabilityService.class.getName());
	static{
		EMPTY_TED.parseRDF("<http://vicinity.eu/data/ted> a <http://iot.linkeddata.es/def/core#ThingEcosystemDescription>;\n   <http://iot.linkeddata.es/def/core#describes> <http://bnodes/N9e711c303f3e40f7872d87ccb66cc225> .\n \n <http://bnodes/N9e711c303f3e40f7872d87ccb66cc225>  a <http://iot.linkeddata.es/def/core#Ecosystem>.");
		TED_PREDICATE_HAS_COMPONENTS = ResourceFactory.createProperty("http://iot.linkeddata.es/def/core#hasComponent");

	}

	// -- Methods
	
	public String solveQueryRemotely(String query, String format) {
		return graphDBService.executeQueryRemotely(query, format);
	}
	
	
	public RDF fetchInteroperableData(String queryString, QueryVisitor visitor, String queryName) {
		RDF ted = new RDF();
		ted.addRDF(EMPTY_TED);
    		if(!queryString.isEmpty()) {
    			try {
    				
    				// 1.1.B Find relevant descriptions for the query
    				long startTime = System.nanoTime();
    				ted = findRelevantDescriptions(queryString, visitor);	
    				long endTime = System.nanoTime();
    				long timeElapsed = (endTime - startTime) / 1000000;		
    				System.out.println("Mid-Result;"+queryName+";discovery(ms)=" + timeElapsed);
    				// 1.2.B Virtualize RDF from descriptions (those that have it)
    				startTime = System.nanoTime();
    				RDF virtualRDF = virtualizeRDF(ted);
    				ted.addRDF(virtualRDF);
    				endTime = System.nanoTime();
    				timeElapsed = (endTime - startTime) / 1000000;		
    				System.out.println("Mid-Result;"+queryName+";access(ms)=" + timeElapsed);
			} catch (Exception e) {
				log.severe(e.toString());
			}
    		}
    		
    		return ted;
	}
	


    private RDF findRelevantDescriptions(String query, QueryVisitor visitor) {
    		RDF result = new RDF();
    		// Clean the query from FILTER statements
    		query = cleanQuery(query);
    		if(!query.isEmpty()) {
    			try {
	    			// Build TED
    				result = buildParallelTED(GraphDBService.getThingsRegistered(), visitor);
			} catch (Exception e) {
				log.severe(e.toString());
			}
    		}
    		
    		return result;
    }

   
	private String cleanQuery(String query) {
		String newQuery = query;
		if(query.contains("FILTER")) {
			newQuery = query.replaceAll(".*FILTER.*", "" );
		}
		
		return newQuery;
	}



	
	private RDF buildParallelTED(List<String> things, QueryVisitor visitor) {
		RDF tedFiltered = new RDF();
		String noAccessTripletsN3 = visitor.getN3NoAccessTripplets();
		ExecutorService executorService = Executors.newFixedThreadPool(ClientApplication.DISCOVERY_THREADS);
		List<Callable<RDF>> taskList = new ArrayList<>();
		int maxIndex = things.size();
		for(int index=0; index < maxIndex; index++) {
			String thing = things.get(index);
			 Callable<RDF> task = () -> {
				RDF result = new RDF();
				RDF thingRDF = graphDBService.retrieveThingRDF(thing);
				Boolean isRelevant = isRelevantObject(thingRDF, noAccessTripletsN3);
				if(!thingRDF.getRDF().isEmpty() && isRelevant) {
					RDF extraRDF = graphDBService.retrieveResourcesOutsideRDF(thingRDF);
					result.addRDF(extraRDF);
					// add thing to ted
					result.getRDF().add(ResourceFactory.createResource("http://bnodes/N9e711c303f3e40f7872d87ccb66cc225"), TED_PREDICATE_HAS_COMPONENTS, ResourceFactory.createResource(thing));
					result.addRDF(thingRDF);
				}
				return result;
			};
			taskList.add(task);
		}
		try {
			List<Future<RDF>> futures = executorService.invokeAll(taskList);
			futures.forEach(futureModel -> {
				try {
					tedFiltered.addRDF(futureModel.get());
				} catch (InterruptedException | ExecutionException e) {
					log.severe(e.toString());
				}
			});
		}catch (Exception e) {
			log.severe(e.toString());
		}

		return tedFiltered;
	}
	
	
	
	private Boolean isRelevantObject(RDF thing,  String noAccessTripletsN3) {
		String queryASKInstantiated = ClientQueries.instanciateAskQuery(noAccessTripletsN3);		
		return graphDBService.executeQueryAskLocally(thing, queryASKInstantiated);
	}


	
	/**
	 * This method virtualizes the {@link RDF} from a {@link RDF} document with WoT-Mappings within
	 * @param ted a {@link RDF} document containing  WoT-Mappings within
	 * @return a {@link RDF} with containing the virtual both {@link RDF}, the original and the virtual
	 */
	private RDF virtualizeRDF(RDF ted) {
		RDF virtualRDF = new RDF();
		try {
			WotTranslator translator = new WotTranslator();
			Mapping mapping = translator.translate(ted.toString());
			EngineImp engine = new EngineImp(mapping);
			virtualRDF = engine.publishRDF();
			engine.close();
		}catch(Exception e) {
			log.severe(e.getMessage());
			e.printStackTrace();
		}
		return virtualRDF;
	}
	
}
