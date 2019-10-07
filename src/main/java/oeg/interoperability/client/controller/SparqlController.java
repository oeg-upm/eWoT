package oeg.interoperability.client.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import helio.components.engine.sparql.SparqlEndpoint;
import helio.framework.objects.RDF;
import helio.framework.objects.SparqlResultsFormat;
import oeg.interoperability.client.jena.JenaUtils;
import oeg.interoperability.client.jena.QueryVisitor;
import oeg.interoperability.client.service.SemanticInteroperabilityService;

@Controller
public class SparqlController {


	@Autowired
	private SemanticInteroperabilityService semanticInteroperabilityService;	
	private static Map<String,SparqlResultsFormat> sparqlResponseFormats;
	private Logger log = Logger.getLogger(SparqlController.class.getName());
	
	
	// -- GET method for GUI

	@RequestMapping(method = RequestMethod.GET, produces = { "text/html", "application/xhtml+xml", "application/xml" })
	public String sparqlGUI(@RequestHeader Map<String, String> headers, HttpServletResponse response, Model model) {
		return "sparql.html";
	}
	
	// -- GET & POST methods for solving queries
	
	@RequestMapping(method = RequestMethod.GET, produces = {"application/sparql-results+xml", "text/rdf+n3", "text/rdf+ttl", "text/rdf+turtle", "text/turtle", "text/n3", "application/turtle", "application/x-turtle", "application/x-nice-turtle", "text/rdf+nt", "text/plain", "text/ntriples", "application/x-trig", "application/rdf+xml", "application/soap+xml", "application/soap+xml;11",  "application/vnd.ms-excel", "text/csv", "text/tab-separated-values", "application/javascript", "application/json", "application/sparql-results+json", "application/odata+json", "application/microdata+json", "text/cxml", "text/cxml+qrcode", "application/atom+xml"})
	@ResponseBody
	public String sparqlEndpointGET(@RequestHeader Map<String, String> headers, @RequestParam (required = true) String query, HttpServletResponse response) {
		return solveQuery(query, headers);
	}
	
	@RequestMapping(value ="/sparql", method = RequestMethod.POST, produces = {"application/sparql-results+xml", "text/rdf+n3", "text/rdf+ttl", "text/rdf+turtle", "text/turtle", "text/n3", "application/turtle", "application/x-turtle", "application/x-nice-turtle", "text/rdf+nt", "text/plain", "text/ntriples", "application/x-trig", "application/rdf+xml", "application/soap+xml", "application/soap+xml;11", "application/vnd.ms-excel", "text/csv", "text/tab-separated-values", "application/javascript", "application/json", "application/sparql-results+json", "application/odata+json", "application/microdata+json", "text/cxml", "text/cxml+qrcode", "application/atom+xml"}) 
	@ResponseBody
	public String sparqlEndpointPOST(@RequestHeader Map<String, String> headers, @RequestBody(required = true) String query, HttpServletResponse response) {
		return solveQuery(query, headers);
    }
    
	private String solveQuery(String query, Map<String,String> headers) {
		String queryAnswer = "";
		String queryName = "query";
		if(query.contains("@split")) {
			queryName = query.split("@split")[0];
			query = query.split("@split")[1].trim();
		}
		// ---
		long startTime = System.nanoTime();
		log.info("query received");
		SparqlResultsFormat queryFormat = extractResponseAnswerFormat(headers);
		// 1. Analyze the query
		QueryVisitor visitor = JenaUtils.computeQueryVisitor(query);
		if(!visitor.isRequiresAccess()) {
			// 1.1.A Execute query in TD repository if does not require virtualization
			String format = "application/json";
			if(queryFormat.toString().toLowerCase().contains("csv"))
				format = "text/csv";
			queryAnswer = semanticInteroperabilityService.solveQueryRemotely(query, format);
		}else {
			RDF queriableData = semanticInteroperabilityService.fetchInteroperableData(query,visitor,queryName);
			SparqlEndpoint enpointSPARQL = new SparqlEndpoint();
			queryAnswer = enpointSPARQL.solveQuery(queriableData, query, queryFormat);
		}
		long endTime = System.nanoTime();
		long timeElapsed = (endTime - startTime) / 1000000;		
		log.info("Query answered in milliseconds : " + timeElapsed);
		//System.out.println("Mid-Result;"+queryName+";total(ms)="+timeElapsed);
		
		return queryAnswer;
	}
	
    
	/**
	 * This method extracts from the request headers the right {@link SparqlResultsFormat} to format the query results
	 * @param headers A set of headers
	 * @return A {@link SparqlResultsFormat} object
	 */
	private SparqlResultsFormat extractResponseAnswerFormat(Map<String, String> headers) {
		String format = "application/json";
		String stringHeader = headers.toString();
		if(headers!=null && !headers.isEmpty()) {
			/*if(headers.containsKey("accept")) {
				String stringHeader = headers.toString();
				String sub1 = stringHeader.substring(stringHeader.indexOf("accept"));
				String sub2 = sub1.substring(0, sub1.indexOf(" ")).replace("accept=", "");
				format = sub2;
			}*/
			if(stringHeader.contains("Accept")) {
				String sub1 = stringHeader.substring(stringHeader.indexOf("Accept"));
				String sub2 = sub1.substring(0, sub1.indexOf(',')).replaceAll("Accept:\\s*", "");
				format = sub2;
			}
		}
		SparqlResultsFormat specifiedFormat = sparqlResponseFormats.get(format);
		if(specifiedFormat == null)
			specifiedFormat = SparqlResultsFormat.JSON;
		return specifiedFormat;
	}
	
	
	static{
		sparqlResponseFormats = new HashMap<>();
		sparqlResponseFormats.put("application/sparql-results+xml", SparqlResultsFormat.XML );
		sparqlResponseFormats.put("text/rdf+n3", SparqlResultsFormat.RDF_N3 );
		sparqlResponseFormats.put("text/rdf+ttl", SparqlResultsFormat.RDF_TTL );
		sparqlResponseFormats.put("text/rdf+turtle", SparqlResultsFormat.RDF_TURTLE );
		sparqlResponseFormats.put("text/turtle", SparqlResultsFormat.RDF_TURTLE );
		sparqlResponseFormats.put("text/n3", SparqlResultsFormat.RDF_N3 );
		sparqlResponseFormats.put("application/turtle", SparqlResultsFormat.RDF_TURTLE );
		sparqlResponseFormats.put("application/x-turtle", SparqlResultsFormat.RDF_TURTLE );
		sparqlResponseFormats.put("application/x-nice-turtle", SparqlResultsFormat.RDF_TURTLE );
		sparqlResponseFormats.put("text/rdf+nt", SparqlResultsFormat.RDF_NT );
		sparqlResponseFormats.put("text/plain", SparqlResultsFormat.TEXT );
		sparqlResponseFormats.put("text/ntriples", SparqlResultsFormat.N_TRIPLES );
		sparqlResponseFormats.put("application/x-trig", SparqlResultsFormat.TRIG );
		sparqlResponseFormats.put("application/rdf+xml", SparqlResultsFormat.RDF_XML );
		sparqlResponseFormats.put("text/html", SparqlResultsFormat.HTML );
		sparqlResponseFormats.put("text/md+html", SparqlResultsFormat.HTML ); // TODO: 
		sparqlResponseFormats.put("text/microdata+html", SparqlResultsFormat.HTML ); // TODO: 
		sparqlResponseFormats.put("text/x-html+ul", SparqlResultsFormat.HTML ); // TODO: 
		sparqlResponseFormats.put("text/x-html+tr", SparqlResultsFormat.HTML ); // TODO: 
		sparqlResponseFormats.put("text/csv", SparqlResultsFormat.CSV );
		sparqlResponseFormats.put("text/tab-separated-values", SparqlResultsFormat.TSV );
		sparqlResponseFormats.put("application/json", SparqlResultsFormat.JSON );
		sparqlResponseFormats.put("application/sparql-results+json", SparqlResultsFormat.JSON );
		sparqlResponseFormats.put("application/xhtml+xml", SparqlResultsFormat.HTML ); // TODO: 
	}
	
	
}
