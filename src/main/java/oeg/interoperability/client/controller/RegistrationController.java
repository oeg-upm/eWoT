package oeg.interoperability.client.controller;

import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import helio.framework.objects.RDF;
import oeg.interoperability.client.service.GraphDBService;


@Controller
@RequestMapping("/register")
public class RegistrationController extends AbstractController {

	
	private Logger log = Logger.getLogger(RegistrationController.class.getName());
	private static final String QUERY_INSERT_TEMPLATE = "INSERT DATA { GRAPH <###> { \n @@@ \n}}";
	private static final String WOT_THING_TYPE = "http://iot.linkeddata.es/def/wot#Thing";
	@Autowired
	private GraphDBService graphDBService;
	
	// -- POST method
	@RequestMapping(method = RequestMethod.POST) 
	@ResponseBody
	public String sparqlEndpointPOST(@RequestHeader Map<String, String> headers, @RequestBody(required = true) String thingDescription, HttpServletResponse response) {
		prepareResponse(response);
		String code = "{\"code\":\"200\", \"text\":\"Thing registered in the ecosystem\"}";
		try {
			RDF thingDescriptionRDF = new RDF();
			try {
				thingDescriptionRDF.parseRDF(thingDescription);
				String graphName = thingDescriptionRDF.getRDF().listSubjectsWithProperty(org.apache.jena.vocabulary.RDF.type, ResourceFactory.createResource(WOT_THING_TYPE)).next().getURI();
				String query =  QUERY_INSERT_TEMPLATE.replace("@@@",thingDescriptionRDF.toString("TURTLE")).replace("###", graphName);
				graphDBService.executeUpdateQueryRemotely(query, "TURTLE");
				response.setStatus( HttpServletResponse.SC_OK ); // by default response code is BAD
				
			}catch(Exception e) {
				log.severe("Provided thing descripion contains syntax errors");
			}
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return code;
	}
	
	
	
	
}
