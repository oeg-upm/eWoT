package oeg.interoperability.client.jena;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphExtract;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.TripleBoundary;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;

public class JenaUtils {
	
	// -- Constructor
	
	/**
	 * Private constructor prevents Java to add an implicit public one for this utility class which is not meant to be instantiated
	 */
	private JenaUtils() {
		// empty
	}
	
	// -- Methods
	
     
    /**
     * This method explores a graph (given as a {@link Model}) starting from a given {@link Node} until it reaches the leafs of the tree 
     * @param resourceNode A {@link Node} in the graph where the search starts
     * @param ted A {@link Model} containing the graph to explore
     * @return a String variable containing the explored graph as RDF in "TURTLE" format
     */
    public static Model extractFullSubTree(Node resourceNode, Model ted) {
    		GraphExtract extractor = new GraphExtract(TripleBoundary.stopNowhere);
        Graph extracted = extractor.extract(resourceNode, ted.getGraph());
        return ModelFactory.createModelForGraph(extracted);
    }
    
    /**
     * This method instantiates a {@link QueryVisitor} object that analyzes the BGP of a given query
     * @param queryString a SPARQL query
     * @return a {@link QueryVisitor} containing several parameters obtained after analyzing the query
     */
    public static QueryVisitor computeQueryVisitor(String queryString) {
		QueryVisitor visitor = new QueryVisitor();
		Query query = QueryFactory.create(queryString);
		Op op = Algebra.compile(query);
		visitor.myOpVisitorWalker(op);
		return visitor;
	}
   
    
    public static String transformTripleIntoSparqlStatement(Triple triple) {
		StringBuilder strTriple = new StringBuilder();
		
		String subject = JenaUtils.nodeToSparqlElement(triple.getSubject());
		String property =  JenaUtils.nodeToSparqlElement(triple.getPredicate());
		String object =  JenaUtils.nodeToSparqlElement(triple.getObject());
		
		strTriple.append(subject).append(" ");
		strTriple.append(property).append(" ");
		strTriple.append(object).append(" ");
		
		return strTriple.toString();
	}

    
	public static String nodeToSparqlElement(Node node) {
		String result = "";
		if(node.isURI()) {
			result = "<"+node.toString().trim()+">";
		}else {
			result = node.toString().trim();
		}
		return result.replace("\n\t\r", "");
	}

}
