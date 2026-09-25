package cm.kfokam48.api;

import cm.kfokam48.service.RelectureService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/exercices")
@CrossOrigin(origins = "http://localhost:3000")
public class NoteController {
    private final RelectureService relectureService;

    public NoteController(RelectureService relectureService) {
        this.relectureService = relectureService;
    }

    @GetMapping("/{id}/note")
    public RelectureDtos.NoteResponse consulter(@PathVariable Long id) {
        return relectureService.consulterNote(id);
    }
}