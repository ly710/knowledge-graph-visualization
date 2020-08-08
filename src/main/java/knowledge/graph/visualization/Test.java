package knowledge.graph.visualization;

import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.RDFDataMgr;

import java.io.InputStream;

public class Test {
    public static void main(String[] args) {
        Model model = ModelFactory.createDefaultModel();
        InputStream in = RDFDataMgr.open("input/vc-db-1.rdf");

        model.read(in, null);
        for(Resource resource : model.listSubjects().toList()) {
            System.out.println(resource.toString());
        }

        System.out.println();

        for(Statement s : model.listStatements().toList()) {
            System.out.println(s.toString());
        }
    }
}
