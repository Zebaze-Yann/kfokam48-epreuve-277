package cm.kfokam48.api;

import cm.kfokam48.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class ApiController {
    private final CourseService service;

    public ApiController(CourseService service) {
        this.service = service;
    }

    @PostMapping("/sessions")
    public ApiDtos.SessionResponse open(@Valid @RequestBody ApiDtos.SessionRequest request) {
        return service.open(request);
    }

    @PostMapping("/presences")
    public ApiDtos.PresenceResponse presence(@Valid @RequestBody ApiDtos.PresenceRequest request) {
        return service.presence(request);
    }

    @PostMapping("/exercices")
    public ApiDtos.ExerciseResponse exercise(@Valid @RequestBody ApiDtos.ExerciseRequest request) {
        return service.exercise(request);
    }

    @PostMapping("/relectures/{id}")
    public ApiDtos.ReviewResponse review(@PathVariable long id, @Valid @RequestBody ApiDtos.ReviewRequest request) {
        return service.review(id, request, false);
    }

    @GetMapping("/relectures/a-faire")
    public List<ApiDtos.AssignedReview> assigned(@RequestParam long etudiantId) {
        return service.assigned(etudiantId);
    }

    @GetMapping("/exercices/{id}/note")
    public ApiDtos.ReviewResponse note(@PathVariable long id) {
        return service.note(id);
    }

    @GetMapping("/tableau")
    public List<ApiDtos.BoardRow> board(@RequestParam long promotionId) {
        return service.board(promotionId);
    }
}