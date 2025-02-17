package io.metersphere.plan.service.remote.api;

import io.metersphere.base.domain.ApiScenarioWithBLOBs;
import io.metersphere.base.domain.Schedule;
import io.metersphere.dto.MsExecResponseDTO;
import io.metersphere.plan.reuest.api.RunScenarioRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Service
public class PlanApiAutomationService extends ApiTestService {

    private static final String BASE_UEL = "/api/automation";


    public List<MsExecResponseDTO> run(RunScenarioRequest request) {
        return microService.postForDataArray(serviceName, BASE_UEL + "/plan/run", request, MsExecResponseDTO.class);
    }

    public void updateSchedule(Schedule request) {
        microService.postForData(serviceName, BASE_UEL + "/schedule/update", request);
    }

    public ApiScenarioWithBLOBs get(@PathVariable String id) {
        return microService.getForData(serviceName, BASE_UEL + "/get/" + id, ApiScenarioWithBLOBs.class);
    }
}
