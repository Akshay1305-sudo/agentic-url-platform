package com.example.orchestrator;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class WorkflowControllerTest {
    private final WorkflowController controller = new WorkflowController();

    @Test void workflowStopsAtHumanApprovalGateAndCanBeApproved() {
        ResponseEntity<?> response = controller.start(new WorkflowController.StartRequest("greenfield", "Add URL expiry support"));
        assertEquals(202, response.getStatusCode().value());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        String id = (String) body.get("id");
        assertEquals("WAITING_APPROVAL", body.get("status").toString());
        ResponseEntity<?> approval = controller.approve(id, new WorkflowController.ApprovalRequest(true, "Reviewed"));
        assertEquals(200, approval.getStatusCode().value());
        assertEquals("SUCCEEDED", ((Map<?, ?>) approval.getBody()).get("status").toString());
    }

    @Test void rejectsUnknownScenario() {
        ResponseEntity<?> response = controller.start(new WorkflowController.StartRequest("unknown", "A requirement"));
        assertEquals(400, response.getStatusCode().value());
    }

    @Test void safeStopIsAudited() {
        Map<?, ?> started = (Map<?, ?>) controller.start(new WorkflowController.StartRequest("ambiguous", "Improve analytics")).getBody();
        ResponseEntity<?> stopped = controller.safeStop((String) started.get("id"));
        assertEquals("SAFE_STOP", ((Map<?, ?>) stopped.getBody()).get("status").toString());
    }
}
