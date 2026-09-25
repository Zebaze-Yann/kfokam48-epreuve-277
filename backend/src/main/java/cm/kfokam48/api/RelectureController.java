package cm.kfokam48.api;

import cm.kfokam48.service.RelectureService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/relectures")
@CrossOrigin(origins = "http://localhost:3000")
public class RelectureController {
    private final RelectureService relectureService;

    public RelectureController(RelectureService relectureService) {
        this.relectureService = relectureService;
    }

    @PostMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public RelectureDtos.NoteResponse rendre(
            @PathVariable Long id,
            @Valid @RequestBody RelectureDtos.NoteRequest request) {
        return relectureService.rendre(id, request);
    }
}