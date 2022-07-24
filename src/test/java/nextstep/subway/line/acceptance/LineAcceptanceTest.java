package nextstep.subway.line.acceptance;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.AcceptanceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

import static nextstep.subway.steps.LineSectionSteps.*;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("지하철 노선 관리 기능")
class LineAcceptanceTest extends AcceptanceTest {
    /**
     * When 지하철 노선을 생성하면
     * Then 지하철 노선 목록 조회 시 생성한 노선을 찾을 수 있다
     */
    @DisplayName("지하철 노선 생성")
    @Test
    void createLine() {
        // when
        var response = 지하철_노선_생성_요청("2호선", "green");
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());

        // then
        노선목록에_노선들이_존재한다("2호선");
    }

    /**
     * Given 2개의 지하철 노선을 생성하고
     * When 지하철 노선 목록을 조회하면
     * Then 지하철 노선 목록 조회 시 2개의 노선을 조회할 수 있다.
     */
    @DisplayName("지하철 노선 목록 조회")
    @Test
    void getLines() {
        // given
        지하철_노선_생성_요청("2호선", "green");
        지하철_노선_생성_요청("3호선", "orange");

        // when + then
        노선목록에_노선들이_존재한다("2호선", "3호선");
    }

    /**
     * Given 지하철 노선을 생성하고
     * When 생성한 지하철 노선을 조회하면
     * Then 생성한 지하철 노선의 정보를 응답받을 수 있다.
     */
    @DisplayName("지하철 노선 조회")
    @Test
    void getLine() {
        // given
        Long _2호선 = 지하철_노선_생성_요청("2호선", "green").jsonPath().getLong("id");

        // when + then
        노선_정보가_일치한다(_2호선, "2호선", "green");
    }

    /**
     * Given 지하철 노선을 생성하고
     * When 생성한 지하철 노선을 수정하면
     * Then 해당 지하철 노선 정보는 수정된다
     */
    @DisplayName("지하철 노선 수정")
    @Test
    void updateLine() {
        // given
        long _2호선 = 지하철_노선_생성_요청("2호선", "green").jsonPath().getLong("id");

        // when
        지하철_노선_수정_요청(_2호선, createLineUpdateParams("신2호선", "lightgreen"));

        // then
        노선_정보가_일치한다(_2호선, "신2호선", "lightgreen");
    }

    /**
     * Given 지하철 노선을 생성하고
     * When 생성한 지하철 노선을 삭제하면
     * Then 해당 지하철 노선 정보는 삭제된다
     */
    @DisplayName("지하철 노선 삭제")
    @Test
    void deleteLine() {
        // given
        long _2호선 = 지하철_노선_생성_요청("2호선", "green").jsonPath().getLong("id");

        // when
        지하철_노선_제거_요청(_2호선);

        // then
        노선이_존재하지_않는다(_2호선);
    }

    private Map<String, String> createLineUpdateParams(String name, String color) {
        Map<String, String> params = new HashMap<>();
        params.put("name", name);
        params.put("color", color);
        return params;
    }

    private void 노선_정보가_일치한다(Long lineId, String name, String color) {
        var response = 지하철_노선_조회_요청(lineId);

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.jsonPath().getString("name")).isEqualTo(name);
        assertThat(response.jsonPath().getString("color")).isEqualTo(color);
    }

    private void 노선목록에_노선들이_존재한다(String... names) {
        ExtractableResponse<Response> response = 지하철_노선_목록_조회_요청();

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.jsonPath().getList("name", String.class)).contains(names);
    }

    private void 노선이_존재하지_않는다(Long lineId) {
        var response = 지하철_노선_조회_요청(lineId);

        assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }
}
