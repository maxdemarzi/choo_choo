package com.maxdemarzi;

import org.neo4j.graphdb.Node;

import static com.maxdemarzi.schema.Properties.LATITUDE;
import static com.maxdemarzi.schema.Properties.LONGITUDE;

public class Heuristics {

    public static Double getCost(double lat1, double lon1, double lat2, double lon2) {
        final int earthRadius = 6371;
        final double kmToNM = 0.539957;
        final double latDistance = Math.toRadians(lat2 - lat1);
        final double lonDistance = Math.toRadians(lon2 - lon1);
        final double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c * kmToNM;
    }

    public static Double getCost(Node node1, Node node2) {
        final double lat1 = (double)node1.getProperty(LATITUDE);
        final double lon1 = (double)node1.getProperty(LONGITUDE);
        final double lat2 = (double)node2.getProperty(LATITUDE);
        final double lon2 = (double)node2.getProperty(LONGITUDE);
        return getCost(lat1, lon1, lat2, lon2);
    }

    public static Double getCost(Node node1, double lat2, double lon2) {
        final double lat1 = (double)node1.getProperty(LATITUDE);
        final double lon1 = (double)node1.getProperty(LONGITUDE);
        return getCost(lat1, lon1, lat2, lon2);
    }
}
