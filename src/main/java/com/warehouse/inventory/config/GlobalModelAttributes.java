package com.warehouse.inventory.config;

import com.warehouse.inventory.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAttributes {

    private final AlertService alertService;

    @ModelAttribute("alertCount")
    public long alertCount() {
        try {
            return alertService.countUnresolved();
        } catch (Exception e) {
            return 0;
        }
    }

    @ModelAttribute("currentUser")
    public String currentUser(Principal principal) {
        return principal != null ? principal.getName() : "Admin";
    }
}
