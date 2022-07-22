package nextstep.subway.line.application;

import nextstep.subway.line.application.dto.request.SectionRequest;
import nextstep.subway.line.domain.Line;
import nextstep.subway.line.domain.LineRepository;
import nextstep.subway.line.domain.Section;
import nextstep.subway.line.domain.exception.CannotDeleteSectionException;
import nextstep.subway.station.domain.Station;
import nextstep.subway.station.domain.StationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class SectionServiceTest {
    @Autowired
    private StationRepository stationRepository;
    @Autowired
    private LineRepository lineRepository;

    @Autowired
    private SectionService sectionService;

    @Test
    void 구간_추가() {
        // given
        Station upStation = stationRepository.save(new Station("양재역"));
        Station downStation = stationRepository.save(new Station("교대역"));
        Line line = lineRepository.save(new Line("신분당선", "red"));

        SectionRequest sectionRequest = new SectionRequest(upStation.getId(), downStation.getId(), 6);

        // when
        sectionService.addSection(line.getId(), sectionRequest);

        // then
        List<Section> sections = line.getSections();
        assertThat(sections).hasSize(1);

        Section addedSection = sections.get(0);
        assertThat(addedSection.getUpStationId()).isEqualTo(upStation.getId());
        assertThat(addedSection.getDownStationId()).isEqualTo(downStation.getId());
    }

    @Test
    void 구간_삭제() {
        // given
        Station upStation = stationRepository.save(new Station("양재역"));
        Station downStation = stationRepository.save(new Station("교대역"));
        Line line = lineRepository.save(new Line("신분당선", "red"));

        line.addSection(upStation.getId(), downStation.getId(), 6);

        // when
        sectionService.deleteSection(line.getId(), downStation.getId());

        // then
        assertThat(line.getSections()).isEmpty();
    }

    @Test
    void 구간_삭제_종점이_아니면_예외() {
        // given
        Station upStation = stationRepository.save(new Station("양재역"));
        Station downStation = stationRepository.save(new Station("교대역"));
        Line line = lineRepository.save(new Line("신분당선", "red"));

        line.addSection(upStation.getId(), downStation.getId(), 6);

        // when + then
        assertThatThrownBy(() -> sectionService.deleteSection(line.getId(), upStation.getId()))
                .isInstanceOf(CannotDeleteSectionException.class);
    }
}
