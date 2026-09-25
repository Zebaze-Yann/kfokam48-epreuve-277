package cm.kfokam48.api;

import cm.kfokam48.service.PresenceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/presences")
@CrossOrigin(origins = "http://localhost:3000")
public class PresenceController {
    private final PresenceService presenceService;

    public PresenceController(PresenceService presenceService) {
        this.presenceService = presenceService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PresenceDtos.PresenceResponse marquer(
            @Valid @RequestBody PresenceDtos.MarquerPresenceRequest request) {
        return presenceService.marquer(request);
    }
}