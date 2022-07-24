package nextstep.subway.line.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.persistence.*;

@Getter
@EqualsAndHashCode
@Entity
public class Section {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "line_id")
    private Line line;

    private Long upStationId;

    private Long downStationId;

    private int distance;

    protected Section() {
    }

    public Section(Line line, Long upStationId, Long downStationId, int distance) {
        this.line = line;
        this.upStationId = upStationId;
        this.downStationId = downStationId;
        this.distance = distance;
    }

    public Section subtract(Section anotherSection) {
        int subtractedDistance = this.distance - anotherSection.distance;
        assert subtractedDistance > 0;

        if (startsTogether(anotherSection)) {
            return new Section(line, anotherSection.getDownStationId(), downStationId, subtractedDistance);
        }

        if (endsTogether(anotherSection)) {
            return new Section(line, upStationId, anotherSection.getUpStationId(), subtractedDistance);
        }

        throw new IllegalArgumentException("상행역이나 하행역이 겹치는 구간끼리만 뺄 수 있습니다.");
    }

    public boolean startsOrEndsTogether(Section anotherSection) {
        return startsTogether(anotherSection) || endsTogether(anotherSection);
    }

    private boolean startsTogether(Section anotherSection) {
        return upStationId.equals(anotherSection.upStationId);
    }

    private boolean endsTogether(Section anotherSection) {
        return downStationId.equals(anotherSection.downStationId);
    }

    public boolean isShorterThanOrEqualWith(Section anotherSection) {
        return distance <= anotherSection.distance;
    }

    public boolean matchUpStation(Long stationId) {
        return upStationId.equals(stationId);
    }

    public boolean matchDownStation(Long stationId) {
        return downStationId.equals(stationId);
    }

    public boolean isBefore(Section anotherSection) {
        return downStationId.equals(anotherSection.upStationId);
    }

    public boolean isAfter(Section anotherSection) {
        return upStationId.equals(anotherSection.downStationId);
    }
}