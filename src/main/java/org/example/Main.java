package org.example;

import it.unimi.dsi.logging.ProgressLogger;
import it.unimi.dsi.webgraph.ArrayListMutableGraph;
import it.unimi.dsi.webgraph.BVGraph;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.LazyIntIterator;
import it.unimi.dsi.webgraph.algo.GeometricCentralities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class Main {
    static String BASE_PATH;

    public static void main(String[] args) throws IOException, InterruptedException {
        String graphPath = args[1];
        var split = graphPath.split("/");
        var graphName = split[split.length - 1];
        Path currentRelativePath = Paths.get("");
        BASE_PATH = currentRelativePath.toAbsolutePath().toString() + "/" + graphName + "_javaresults";
        System.out.println("Graph path: " + graphPath);
        System.out.println("Graph name: " + graphName);
        System.out.println("Base path: " + BASE_PATH);
        ImmutableGraph g = BVGraph.load(graphPath, new ProgressLogger());

        if (Arrays.stream(args).anyMatch(Predicate.isEqual("-d"))) {
            g = decompress_graph(g);
        }
        
        var created = new File(BASE_PATH).mkdir();
        if (!created) {
            System.out.println("The directory already exists");
        }
        if (Arrays.stream(args).anyMatch(Predicate.isEqual("-g"))) {
            geometric_main(args, g);
        } else if (Arrays.stream(args).anyMatch(Predicate.isEqual("-b"))) {
            betweenness_main(args, g);
        }
    }


    private static void betweenness_main(String[] args, ImmutableGraph g) throws IOException, InterruptedException {
        BetweennessCentrality centrality = new org.example.BetweennessCentrality(g, 0, null);
        var startTime = System.currentTimeMillis();
        centrality.compute();
        var elapsed = System.currentTimeMillis() - startTime;
        System.out.println(elapsed);

        
        //write_doubles_to_file("betweenness", Arrays.stream(centrality.betweenness).boxed());
           
        /*
        var g = new it.unimi.dsi.webgraph.ArrayListMutableGraph();
        g.addNodes(8);

        g.addArc(5, 6);
        g.addArc(5, 0);
        g.addArc(6, 0);
        g.addArc(7, 0);
        g.addArc(3, 0);
        g.addArc(3, 7);
        g.addArc(2, 6);
        g.addArc(4, 2);
        g.addArc(4, 5);
        g.addArc(1, 7);
        g.addArc(1, 3);

        g.addArc(6, 5);
        g.addArc(0, 5);
        g.addArc(0, 6);
        g.addArc(0, 7);
        g.addArc(0, 3);
        g.addArc(7, 3);
        g.addArc(6, 2);
        g.addArc(2, 4);
        g.addArc(5, 4);
        g.addArc(7, 1);
        g.addArc(3, 1);
        
        var b = new org.example.BetweennessCentrality(g.immutableView(), 1);
        try {
            b.compute();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        var i = 0;
        for (var item : b.betweenness) {
            System.out.println(i + " " + item);
            i++;
        }
       */
    }

    private static ImmutableGraph decompress_graph(ImmutableGraph g) {
        var arrListGraph = new ArrayListMutableGraph();
        var numNodes = g.numNodes();
        arrListGraph.addNodes(numNodes);
        for (int node = 0; node < numNodes; node++) {
            final LazyIntIterator successors = g.successors(node);
            for (int s; (s = successors.nextInt()) != -1; ) {
                arrListGraph.addArc(node, s);
            }
        }
        return arrListGraph.immutableView();
    }

    private static void geometric_main(String[] args, ImmutableGraph g) throws IOException, InterruptedException {
        GeometricCentralities centralities = new GeometricCentralities(decompress_graph(g), 0, null);
        var startTime = System.currentTimeMillis();
        centralities.compute();
        var elapsed = System.currentTimeMillis() - startTime;
        System.out.println(elapsed);
        /*
        write_doubles_to_file("closeness", Arrays.stream(centralities.closeness).boxed());
        write_doubles_to_file("lin", Arrays.stream(centralities.lin).boxed());
        write_doubles_to_file("exponential", Arrays.stream(centralities.exponential).boxed());
        write_doubles_to_file("harmonic", Arrays.stream(centralities.harmonic).boxed());
        write_nums_to_file("reachable", Arrays.stream(centralities.reachable).boxed());
        */
		/*
		var g = new it.unimi.dsi.webgraph.ArrayListMutableGraph();
		g.addNodes(5);

		g.addArc(0, 1);
		g.addArc(0, 2);
		g.addArc(1, 3);
		g.addArc(2, 3);
		g.addArc(3, 4);

		var b = new it.unimi.dsi.webgraph.algo.BetweennessCentrality(g.immutableView(), 1);
		try {
			b.compute();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		var i = 0;
		for(var item: b.betweenness) {
			System.out.println(i + " " + item);
			i++;
		}
		*/
    }


    private static void write_doubles_to_file(String fileName, Stream<Double> numsIter) throws IOException {
        var sb = new StringBuilder();
        numsIter.forEach((item) -> {
            sb.append(String.format("%.20f", new BigDecimal(item)));
            sb.append("\n");
        });
        write_to_file(fileName, sb.toString());
    }


    private static <T> void write_nums_to_file(String fileName, Stream<T> numsIter) throws IOException {
        var sb = new StringBuilder();
        numsIter.forEach((item) -> {
            sb.append(item);
            sb.append("\n");
        });
        write_to_file(fileName, sb.toString());
    }

    private static void write_to_file(String fileName, String text) throws IOException {
        String filePath = BASE_PATH + "/" + fileName;
        var f = new File(filePath);
        f.createNewFile();
        var fo = new FileOutputStream(f);
        fo.write(text.getBytes());
        fo.close();
    }
}