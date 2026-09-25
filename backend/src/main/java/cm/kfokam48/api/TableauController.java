package cm.kfokam48.api;

import cm.kfokam48.service.TableauService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tableau")
@CrossOrigin(origins = "http://localhost:3000")
public class TableauController {
    private final TableauService tableauService;

    public TableauController(TableauService tableauService) {
        this.tableauService = tableauService;
    }

    @GetMapping
    public List<TableauDtos.LigneTableau> consulter(@RequestParam Long promotionId) {
        return tableauService.consulter(promotionId);
    }
}