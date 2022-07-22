package nextstep.subway.line.application;

import nextstep.subway.station.applicaion.StationService;
import nextstep.subway.line.application.dto.request.SectionRequest;
import nextstep.subway.line.domain.Line;
import nextstep.subway.line.domain.LineRepository;
import nextstep.subway.station.domain.Station;
import nextstep.subway.line.domain.exception.CannotDeleteSectionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SectionServiceMockTest {
    @Mock
    private LineRepository lineRepository;
    @Mock
    private StationService stationService;

    @InjectMocks
    private SectionService sectionService;

    @Test
    void 구간_추가() {
        // given
        Long lineId = 1L;
        Long upStationId = 1L;
        Long downStationId = 2L;

        Line line = new Line("신분당선", "red");

        when(stationService.findById(upStationId)).thenReturn(new Station("강남"));
        when(stationService.findById(downStationId)).thenReturn(new Station("양재"));
        when(lineRepository.findById(lineId)).thenReturn(Optional.of(line));

        // when
        sectionService.addSection(lineId, new SectionRequest(upStationId, downStationId, 6));

        // then
        assertThat(line.getSections()).hasSize(1);
    }

    @Test
    void 구간_삭제() {
        // given
        Long lineId = 1L;
        Long upStationId = 1L;
        Long downStationId = 2L;

        Line line = new Line("신분당선", "red");
        line.addSection(upStationId, downStationId, 6);

        when(lineRepository.findById(lineId)).thenReturn(Optional.of(line));

        // when
        sectionService.deleteSection(lineId, downStationId);

        // then
        assertThat(line.getSections()).isEmpty();
    }

    @Test
    void 구간_삭제_종점이_아니면_예외() {
        // given
        Long lineId = 1L;
        Long upStationId = 1L;
        Long downStationId = 2L;

        Line line = new Line("신분당선", "red");
        line.addSection(upStationId, downStationId, 6);

        when(lineRepository.findById(lineId)).thenReturn(Optional.of(line));

        // when + then
        assertThatThrownBy(() -> sectionService.deleteSection(lineId, upStationId))
                .isInstanceOf(CannotDeleteSectionException.class);
    }
}
