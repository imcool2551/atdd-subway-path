package nextstep.subway.domain;

import lombok.Getter;

import javax.persistence.*;
import java.util.List;

@Getter
@Entity
public class Line {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String color;

    @Embedded
    private Sections sections;

    protected Line() {
    }

    public Line(String name, String color) {
        this.name = name;
        this.color = color;
        this.sections = new Sections();
    }

    public void addSection(Long upStationId, Long downStationId, int distance) {
        sections.add(new Section(upStationId, downStationId, distance));
    }

    public boolean isLastStation(Long stationId) {
        return sections.isLastStation(stationId);
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateColor(String color) {
        this.color = color;
    }

    public List<Section> getSections() {
        return sections.getSections();
    }
}
