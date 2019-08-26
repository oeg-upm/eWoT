package oeg.interoperability.client.jena;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVisitorBase;
import org.apache.jena.sparql.algebra.OpWalker;
import org.apache.jena.sparql.algebra.op.OpBGP;

public class QueryVisitor extends OpVisitorBase {
	
	private static final List<String> virtualizationPatterns = new ArrayList<String>(14);
	static {
		virtualizationPatterns.add("http://iot.linkeddata.es/def/core#Value");
		virtualizationPatterns.add("core:Value");
		virtualizationPatterns.add("http://iot.linkeddata.es/def/core#hasValue");
		virtualizationPatterns.add("core:hasValue");
		virtualizationPatterns.add("http://iot.linkeddata.es/def/core#literalValue");
		virtualizationPatterns.add("core:literalValue");
		virtualizationPatterns.add("http://iot.linkeddata.es/def/core#hasMaxValue");
		virtualizationPatterns.add("core:hasMaxValue");
		virtualizationPatterns.add("http://iot.linkeddata.es/def/core#hasMinValue");
		virtualizationPatterns.add("core:hasMinValue");
		virtualizationPatterns.add("http://iot.linkeddata.es/def/core#timestamp");
		virtualizationPatterns.add("core:timestamp");
		virtualizationPatterns.add("http://iot.linkeddata.es/def/core#expressedInFormat");
		virtualizationPatterns.add("core:expressedInFormat");
	}
	private List<Triple> triplets0Variables;
	private List<Triple> triplets1Variables;
	private List<Triple> triplets2Variables;
	private List<Triple> triplets3Variables;
	private List<Triple> noAccessTriplets;
	private StringBuilder noAccessTripletsN3;
	private boolean requiresAccess;
	
	public QueryVisitor() {
		triplets0Variables = new ArrayList<>();
		triplets1Variables = new ArrayList<>();
		triplets2Variables = new ArrayList<>();
		triplets3Variables = new ArrayList<>();
		noAccessTriplets = new ArrayList<>();
		requiresAccess = false;
		noAccessTripletsN3 = new StringBuilder();
	}
	
    public void myOpVisitorWalker(Op op){
        OpWalker.walk(op, this);
    }

    @Override
    public void visit(final OpBGP opBGP) {
        final List<Triple> triples = opBGP.getPattern().getList();
        int triplesSize = triples.size();
        for (int index=0; index < triplesSize; index++) {
        		Triple triple = triples.get(index);
        		checkDistributedAccessRequired(triple);
        		processTriple(triple);
            
        }
    }
    
	private void checkDistributedAccessRequired(Triple triple) {
		Boolean needsAccess = false;
		for(int counter=0; counter < virtualizationPatterns.size(); counter++) {
			needsAccess = triple.toString().contains(virtualizationPatterns.get(counter));
			if(needsAccess) {
				requiresAccess = true;
				break;
			}
		}
		if(!needsAccess) {
			noAccessTriplets.add(triple);
			noAccessTripletsN3.append(JenaUtils.transformTripleIntoSparqlStatement(triple)).append(" .\n");
			
		}
    }
	
	   private void processTriple(Triple triple) {
   		int variables = 0;
		Boolean subjectVariable = triple.getSubject().isVariable();
		Boolean predicateVariable = triple.getPredicate().isVariable();
		Boolean objectVariable = triple.getObject().isVariable();
		
		if(subjectVariable)
			variables += 1;
		if(predicateVariable)
			variables += 1;
		if(objectVariable)
			variables += 1;
		
		storeTriple(triple, variables);
	}
	

	private void storeTriple(Triple triple, int variables) {
		if(variables == 0) {
			triplets0Variables.add(triple);
		}else if(variables == 1) {
			triplets1Variables.add(triple);
		}else if(variables == 2) {
			triplets2Variables.add(triple);
		}else {
			triplets3Variables.add(triple);
		}
	}

	public boolean isRequiresAccess() {
		return requiresAccess;
	}

	public List<Triple> getTriplets0Variables() {
		return triplets0Variables;
	}

	public List<Triple> getTriplets1Variables() {
		return triplets1Variables;
	}

	public List<Triple> getTriplets2Variables() {
		return triplets2Variables;
	}

	public List<Triple> getTriplets3Variables() {
		return triplets3Variables;
	}

	public List<Triple> getNoAccessTriplets() {
		return noAccessTriplets;
	}
    
	public String getN3NoAccessTripplets() {
		return noAccessTripletsN3.toString();
	}
	
    
}