package nextstep.subway.path.application;

import lombok.RequiredArgsConstructor;
import nextstep.subway.line.application.SectionService;
import nextstep.subway.path.application.dto.PathResponse;
import nextstep.subway.path.domain.Edge;
import nextstep.subway.path.domain.Graph;
import nextstep.subway.path.domain.Path;
import nextstep.subway.station.applicaion.StationService;
import nextstep.subway.station.applicaion.dto.response.StationResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PathService {

    private final StationService stationService;
    private final SectionService sectionService;

    public PathResponse findPath(Long source, Long target) {
        List<StationResponse> stations = stationService.findAllStations();

        List<Long> stationIds = stations.stream()
                .map(StationResponse::getId)
                .collect(Collectors.toList());

        List<Edge> sections = sectionService.findAllSections()
                .stream()
                .map(it -> new Edge(it.getUpStationId(), it.getDownStationId(), it.getDistance()))
                .collect(Collectors.toList());

        Path shortestPath = new Graph(stationIds, sections).findShortestPath(source, target);
        List<Long> pathStationIds = shortestPath.getVertexes();

        List<StationResponse> pathStationsInOrder = stations.stream()
                .filter(s -> pathStationIds.contains(s.getId()))
                .sorted(comparing(s -> pathStationIds.indexOf(s.getId())))
                .collect(Collectors.toList());

        return new PathResponse(pathStationsInOrder, shortestPath.getDistance());
    }
}
