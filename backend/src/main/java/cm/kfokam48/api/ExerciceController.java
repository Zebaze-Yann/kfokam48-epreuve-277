package cm.kfokam48.api;

import cm.kfokam48.service.ExerciceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/exercices")
@CrossOrigin(origins = "http://localhost:3000")
public class ExerciceController {
    private final ExerciceService exerciceService;

    public ExerciceController(ExerciceService exerciceService) {
        this.exerciceService = exerciceService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExerciceDtos.DepotResponse deposer(
            @Valid @RequestBody ExerciceDtos.DepotRequest request) {
        return exerciceService.deposer(request);
    }
}