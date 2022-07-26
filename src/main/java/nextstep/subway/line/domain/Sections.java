package nextstep.subway.line.domain;

import nextstep.subway.line.domain.exception.CannotAddSectionException;
import nextstep.subway.line.domain.exception.CannotDeleteSectionException;

import javax.persistence.Embeddable;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static javax.persistence.CascadeType.ALL;

@Embeddable
public class Sections {

    @OneToMany(mappedBy = "line", cascade = ALL, orphanRemoval = true)
    private List<Section> sections = new ArrayList<>();

    public void add(Section addedSection) {
        if (sections.isEmpty()) {
            sections.add(addedSection);
            return;
        }

        validateAddedSection(addedSection);

        if (isNewFirstOrLastSection(addedSection)) {
            sections.add(addedSection);
            return;
        }

        Section connectedSection = findConnectedSection(addedSection);
        Section subtractedSection = connectedSection.subtract(addedSection);

        sections.remove(connectedSection);
        sections.add(addedSection);
        sections.add(subtractedSection);
    }

    private void validateAddedSection(Section addedSection) {
        List<Long> stationIds = getOrderedStationIds();

        boolean containsUpStation = stationIds.contains(addedSection.getUpStationId());
        boolean containsDownStation = stationIds.contains(addedSection.getDownStationId());

        if (containsUpStation && containsDownStation) {
            throw new CannotAddSectionException("상행역과 하행역이 이미 노선에 모두 등록되어 있으면 구간을 추가할 수 없습니다.");
        }

        if (!containsUpStation && !containsDownStation) {
            throw new CannotAddSectionException("상행역과 하행역 둘 중 하나도 포함되어있지 않으면 구간을 추가할 수 없습니다.");
        }
    }

    private boolean isNewFirstOrLastSection(Section addedSection) {
        return firstSection().isAfter(addedSection) || lastSection().isBefore(addedSection);
    }

    private Section findConnectedSection(Section section) {
        return sections.stream()
                .filter(s -> s.startsOrEndsTogether(section))
                .findAny()
                .orElseThrow();
    }

    public void removeSection(Long stationId) {
        if (sections.size() < 2) {
            throw new CannotDeleteSectionException("구간이 둘 이상이어야 역을 제거할 수 있습니다.");
        }

        if (!isExistingStation(stationId)) {
            throw new CannotDeleteSectionException("등록되어 있지 않은 역을 제거할 수 없습니다.");
        }

        if (isFirstStation(stationId)) {
            sections.remove(firstSection());
            return;
        }

        if (isLastStation(stationId)) {
            sections.remove(lastSection());
            return;
        }

        List<Long> orderedStationIds = getOrderedStationIds();
        int stationIdIdx = orderedStationIds.indexOf(stationId);
        Long firstRemovedSectionIndex = orderedStationIds.get(stationIdIdx - 1);

        Section firstRemovedSection = findByUpStationId(firstRemovedSectionIndex);
        Section secondRemovedSection = findByUpStationId(stationId);
        Section combinedSection = firstRemovedSection.combine(secondRemovedSection);

        sections.remove(firstRemovedSection);
        sections.remove(secondRemovedSection);
        sections.add(combinedSection);
    }

    private boolean isExistingStation(Long stationId) {
        return getOrderedStationIds().contains(stationId);
    }

    private boolean isLastStation(Long stationId) {
        List<Long> orderedStationIds = getOrderedStationIds();
        Long lastStationId = orderedStationIds.get(orderedStationIds.size() - 1);
        return lastStationId.equals(stationId);
    }

    private boolean isFirstStation(Long stationId) {
        List<Long> orderedStationIds = getOrderedStationIds();
        Long firstStationId = orderedStationIds.get(0);
        return firstStationId.equals(stationId);
    }

    public List<Long> getOrderedStationIds() {
        if (sections.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> stationIds = new ArrayList<>();
        Section iter = firstSection();

        while (!iter.equals(lastSection())) {
            stationIds.add(iter.getUpStationId());
            iter = findByUpStationId(iter.getDownStationId());
        }

        stationIds.add(iter.getUpStationId());
        stationIds.add(iter.getDownStationId());
        return stationIds;
    }

    private Section findByUpStationId(Long stationId) {
        return sections.stream()
                .filter(s -> s.matchUpStation(stationId))
                .findAny()
                .orElseThrow();
    }

    private Section firstSection() {
        return sections.stream()
                .filter(this::isFirstSection)
                .findAny()
                .orElseThrow();
    }

    private Section lastSection() {
        return sections.stream()
                .filter(this::isLastSection)
                .findAny()
                .orElseThrow();
    }

    private boolean isFirstSection(Section section) {
        return sections.stream()
                .map(Section::getDownStationId)
                .noneMatch(id -> id.equals(section.getUpStationId()));
    }

    private boolean isLastSection(Section section) {
        return sections.stream()
                .map(Section::getUpStationId)
                .noneMatch(id -> id.equals(section.getDownStationId()));
    }

    public boolean isEmpty() {
        return sections.isEmpty();
    }

    List<Section> getSections() {
        return sections;
    }
}
