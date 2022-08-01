package nextstep.subway.path.domain;

import nextstep.subway.line.domain.Section;
import nextstep.subway.path.domain.exception.CannotFindPathException;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.WeightedMultigraph;

import java.util.List;

public class Graph {
    private final WeightedMultigraph<Long, DefaultWeightedEdge> graph
            = new WeightedMultigraph<>(DefaultWeightedEdge.class);

    public Graph(List<Section> sections) {
        sections.forEach(it -> {
            graph.addVertex(it.getUpStationId());
            graph.addVertex(it.getDownStationId());
            graph.setEdgeWeight(graph.addEdge(it.getUpStationId(), it.getDownStationId()), it.getDistance());
        });
    }

    public Path findShortestPath(Long source, Long target) {
        validateSourceAndTarget(source, target);

        GraphPath<Long, DefaultWeightedEdge> shortestPath
                = new DijkstraShortestPath<>(graph).getPath(source, target);

        validatePathExistence(shortestPath);

        List<Long> vertexes = shortestPath.getVertexList();
        int distance = (int) shortestPath.getWeight();
        return new Path(vertexes, distance);
    }

    private void validateSourceAndTarget(Long source, Long target) {
        if (source.equals(target)) {
            throw new CannotFindPathException("시작점과 종점이 같은 경로를 조회할 수 없습니다.");
        }

        if (!graph.containsVertex(source) || !graph.containsVertex(target)) {
            throw new CannotFindPathException("시작점이나 종점이 존재하지 않습니다.");
        }
    }

    private void validatePathExistence(GraphPath<Long, DefaultWeightedEdge> shortestPath) {
        if (shortestPath == null) {
            throw new CannotFindPathException("시작점과 종점이 연결되어 있지 않습니다.");
        }
    }

}
