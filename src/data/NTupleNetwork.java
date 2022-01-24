package data;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class NTupleNetwork implements Serializable {

    public final Point[][] tuples;
    public final double[][] weights;
    public final String description;
    private final String[][] shapes;

    public NTupleNetwork(String[][] shapes, String description) {
        this.shapes = shapes;
        this.description = description;
        int nTuples = shapes.length;
        tuples = new Point[nTuples][];
        for (int i = 0 ; i < nTuples ; i++) {
            tuples[i] = getPoints(shapes[i]);
        }
        weights = new double[nTuples][];
        for (int i = 0 ; i < tuples.length ; i++) {
            weights[i] = new double[(int) Math.pow(16, tuples[i].length)];
        }
    }

    private Point[] getPoints(String[] shape) {
        List<Point> points = new ArrayList<>();
        for (int y = 0 ; y < 4 ; y++) {
            for (int x = 0 ; x < 4 ; x++) {
                if (shape[y].charAt(x) == '*') {
                    points.add(new Point(x, y));
                }
            }
        }
        Point[] pointArray = new Point[points.size()];
        for (int i = 0 ; i < pointArray.length ; i++) {
            pointArray[i] = points.get(i);
        }
        return pointArray;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(description);
        sb.append('\n');
        sb.append("\t{\n\t\t");
        for(String[] shape : shapes) {
            sb.append("\t{\n\t\t\t\"");
            for (int y = 0 ; y < 3 ; y++) {
                sb.append(shape[y]);
                sb.append("\",\n\t\t\t\"");
            }
            sb.append(shape[3]);
            sb.append("\",\n\t\t");
            sb.append("},\n");
        }
        sb.append("\t}\n");
        return sb.toString();
    }
}
