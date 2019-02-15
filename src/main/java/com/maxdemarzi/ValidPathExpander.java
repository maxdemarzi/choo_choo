package com.maxdemarzi;

import com.maxdemarzi.schema.RelationshipTypes;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.BranchState;

import java.util.ArrayList;
import java.util.List;

import static com.maxdemarzi.schema.Properties.NETWORK;
import static com.maxdemarzi.schema.Properties.OUT_OF_SERVICE;

public class ValidPathExpander implements PathExpander {

    @Override
    public Iterable<Relationship> expand(Path path, BranchState branchState) {
        List<Relationship> rels = new ArrayList<>();
        for (Relationship r : path.endNode().getRelationships(RelationshipTypes.LINKS)) {
            if (!(r.getProperty(NETWORK)).equals(OUT_OF_SERVICE)) {
                rels.add(r);
            }
        }
        return rels;
    }

    @Override
    public PathExpander reverse() {
        return null;
    }
}
