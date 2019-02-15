package com.maxdemarzi;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.maxdemarzi.schema.RelationshipTypes;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.BranchState;

import java.util.*;

import static com.maxdemarzi.Heuristics.getCost;
import static com.maxdemarzi.Procedures.latitudes;
import static com.maxdemarzi.Procedures.longitudes;
import static com.maxdemarzi.schema.Properties.*;

public class ValidPathExpander implements PathExpander {
    private final double maxCost;
    private final double latitude;
    private final double longitude;
    private final HashSet<Long> valid = new HashSet<>();
    private final HashSet<Long> invalid = new HashSet<>();

    public ValidPathExpander(double maxCost, double latitude, double longitude) {
        this.maxCost = maxCost;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public Iterable<Relationship> expand(Path path, BranchState branchState) {
        List<Relationship> rels = new ArrayList<>();
        // Make sure we don't get on a track that is out of service:
        for (Relationship r : path.endNode().getRelationships(RelationshipTypes.LINKS)) {
            if (!(r.getProperty(NETWORK)).equals(OUT_OF_SERVICE)) {

                // Make sure we don't stray too far away.
                Node from = r.getOtherNode(path.endNode());
                if(valid.contains(from.getId())){
                    rels.add(r);
                    continue;
                }

                if(invalid.contains(from.getId())){
                    continue;
                }

                if (getCost(latitudes.get(from), longitudes.get(from), latitude, longitude) <= maxCost) {
                    rels.add(r);
                    valid.add(from.getId());
                } else {
                    invalid.add(from.getId());
                }
            }
        }
        return rels;
    }

    @Override
    public PathExpander reverse() {
        return null;
    }
}
