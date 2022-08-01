package nextstep.subway.path.acceptance;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.AcceptanceTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static nextstep.subway.steps.LineSectionSteps.*;
import static nextstep.subway.steps.PathSteps.경로_조회_요청;
import static nextstep.subway.steps.StationSteps.지하철역_삭제_요청;
import static nextstep.subway.steps.StationSteps.지하철역_생성_요청;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("지하철 경로 조회 기능")
class PathAcceptanceTest extends AcceptanceTest {

    private Long 교대역;
    private Long 강남역;
    private Long 양재역;
    private Long 남부터미널역;
    private Long 이호선;
    private Long 신분당선;
    private Long 삼호선;

    /** GIVEN
     *
     *  참고: 점선 하나가 거리 1을 의미합니다.
     *
     * 교대역    ---    <2호선> ---   강남역
     *   |                           |
     * <3호선>                     <신분당선>
     *   |                           |
     * 남부터미널역  --- <3호선> ----   양재
     */
    @BeforeEach
    public void setUp() {
        super.setUp();

        교대역 = 지하철역_생성_요청("교대역").jsonPath().getLong("id");
        강남역 = 지하철역_생성_요청("강남역").jsonPath().getLong("id");
        양재역 = 지하철역_생성_요청("양재역").jsonPath().getLong("id");
        남부터미널역 = 지하철역_생성_요청("남부터미널역").jsonPath().getLong("id");

        이호선 = 지하철_노선_생성_요청("2호선", "green").jsonPath().getLong("id");
        신분당선 = 지하철_노선_생성_요청("신분당선", "red").jsonPath().getLong("id");
        삼호선 = 지하철_노선_생성_요청("3호선", "orange").jsonPath().getLong("id");

        지하철_노선에_지하철_구간_생성_요청(이호선, createSectionCreateParams(교대역, 강남역, 6));
        지하철_노선에_지하철_구간_생성_요청(삼호선, createSectionCreateParams(남부터미널역, 양재역, 7));
        지하철_노선에_지하철_구간_생성_요청(삼호선, createSectionCreateParams(교대역, 남부터미널역, 2));
        지하철_노선에_지하철_구간_생성_요청(신분당선, createSectionCreateParams(강남역, 양재역, 2));
    }

    @DisplayName("출발역과 도착역이 이어져있을 때 경로를 조회하면 경로와 최단거리를 응답받는다.")
    @Test
    void 경로_조회() {
        assertAll(
                () -> 경로_조회_정보가_일치한다(경로_조회_요청(교대역, 양재역), 8, 교대역, 강남역, 양재역),
                () -> 경로_조회_정보가_일치한다(경로_조회_요청(교대역, 강남역), 6, 교대역, 강남역),
                () -> 경로_조회_정보가_일치한다(경로_조회_요청(남부터미널역, 강남역), 8, 남부터미널역, 교대역, 강남역),
                () -> 경로_조회_정보가_일치한다(경로_조회_요청(교대역, 남부터미널역), 2, 교대역, 남부터미널역)
        );
    }

    @DisplayName("출발역과 도착역이 같다면 경로 조회를 할 수 없다.")
    @Test
    void 경로_조회_예외1() {
        경로를_조회할_수_없다(교대역, 교대역);
    }

    @DisplayName("출발역과 도착역이 이어져있지 않으면 경로 조회를 할 수 없다.")
    @Test
    void 경로_조회_예외2() {
        // when
        Long 새로운역 = 지하철역_생성_요청("새로운역").jsonPath().getLong("id");

        // then
        경로를_조회할_수_없다(새로운역, 교대역);
    }

    @DisplayName("존재하지 않는 역에대해 경로 조회를 할 수 없다.")
    @Test
    void 경로_조회_예외3() {
        // when
        Long 제거역 = 지하철역을_생성했다가_제거한다("제거역");

        // then
        경로를_조회할_수_없다(제거역, 강남역);
    }

    private void 경로를_조회할_수_없다(Long source, Long target) {
        var response = 경로_조회_요청(source, target);

        assertThat(response.statusCode()).isIn(HttpStatus.BAD_REQUEST.value(), HttpStatus.NOT_FOUND.value());
    }

    private void 경로_조회_정보가_일치한다(ExtractableResponse<Response> response, int distance, Long... pathStationIds) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());

        assertThat(response.jsonPath().getInt("distance")).isEqualTo(distance);
        assertThat(response.jsonPath().getList("stations.id", Long.class))
                .containsExactly(pathStationIds);
    }

    private Long 지하철역을_생성했다가_제거한다(String name) {
        var createResponse = 지하철역_생성_요청(name);
        assertThat(createResponse.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        Long 제거된역 = createResponse.jsonPath().getLong("id");

        var deleteResponse = 지하철역_삭제_요청(제거된역);
        assertThat(deleteResponse.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());

        return 제거된역;
    }
}
