package lam.exportfileorder;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/view")
public class ViewController {
    @GetMapping("/download")
    public String callApi(){
        return "downloadFile";
    }
}
