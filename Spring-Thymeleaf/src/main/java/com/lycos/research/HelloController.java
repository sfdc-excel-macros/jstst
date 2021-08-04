package com.lycos.research;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.thymeleaf.spring5.view.ThymeleafView;

@Controller
public class HelloController {

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("message", "happy birthday");
        return "welcome"; //FP, static home page
    }

    // http://127.0.0.1:8090/fragment?section=$%7BT(java.lang.Runtime).getRuntime().exec(%22touch%20lippo.txt%22)%7D
    @GetMapping("/fragment")
    public String fragment(@RequestParam String section) {
        return "welcome :: " + section;
    }

    @GetMapping("/path")
    public String path(@RequestParam String lang) {
        return "user/" + lang + "/welcome";
    }

    @GetMapping("/model")
    public Object model(@RequestParam String section) {
        ModelAndView mav = new ModelAndView("welcome :: " + section);
        return mav;
    }

    @GetMapping("/model/other") //other ModelAndView sinks
    public ModelAndView modelOther(@RequestParam String section, Model m) {
        String viewName = "welcome :: " + section;
        new ThymeleafView(viewName);
        new ModelAndView(viewName, HttpStatus.OK);
        new ModelAndView(viewName, m.asMap());
        new ModelAndView(viewName, m.asMap(), HttpStatus.OK);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName(viewName);
        return modelAndView;
    }


    @GetMapping("/model/asparam") // when ModelView is injected and not tainted
    public ModelAndView modelParam(ModelAndView mav) {
        return mav; // FP since the view object is not tainted
    }


    @GetMapping("/redirect")
    public String redirect(@RequestParam String url) {
        return "redirect:" + url;
    }

    @GetMapping("/ajaxredirect")
    public String ajaxredirect(@RequestParam String name) {
        return "ajaxredirect:" + name;
    }

    //safe cases
    @GetMapping("/_safe/model")
    public Object safeModel(@RequestParam String section) {
        return new ModelAndView("welcome"); //FP, as the view name is not tainted
    }

    @GetMapping("/_safe/fragment")
    @ResponseBody
    public String safeFragment(@RequestParam String section) {
        return "welcome :: " + section;
    }

    @GetMapping("/_safe/redirect")
    public String safeRedirect(@RequestParam String url) {
        return "redirect:./login?xxx=" + url; //FP, as we can control the hostname in redirect
    }

    @GetMapping("/_safe/sanitize")
    public Object safeSanitize(@RequestParam String section) {
        return sanitize("welcome :: " + section); //FP, as the view name is not tainted
    }

    private String sanitize(String section) {
        return ""; //no propagation here
    }


    // http://127.0.0.1:8090/model/pathdefault/::$%7BT(java.lang.Runtime).getRuntime().exec(%22touch%20lippo.txt%22)%7D.x
    @GetMapping("/model/pathdefault/{id}")
    public Object modelDefaultPathVariable(@PathVariable("id") String id) {
        // When returning an Object/Map, Spring uses DefaultRequestToViewNameTranslator that parses the URL string
        // and checks if it can be parsed as EL expression. In this case it can be exploited when GetMapping contains
        // a PathVariable or a Path containing a *
        return new Object();
    }

    // http://127.0.0.1:8090/model/default::$%7BT(java.lang.Runtime).getRuntime().exec(%22touch%20lippo.txt%22)%7D.x
    @GetMapping("/model/default*")
    public Object modelDefaultStar() {
        // When returning an Object/Map, Spring uses DefaultRequestToViewNameTranslator that parses the URL string
        // and checks if it can be parsed as EL expression. In this case it can be exploited when GetMapping contains
        // a PathVariable or a Path containing a *
        return new Object();
    }

    // http://127.0.0.1:8090/model/pathdefaultsafe/::$%7BT(java.lang.Runtime).getRuntime().exec(%22touch%20lippo.txt%22)%7D.x
    @GetMapping("/model/pathdefaultsafe/{id}")
    @ResponseBody
    public Object modelDefaultPathVariableSafe(@PathVariable("id") String id) {   // FP - Method annotated with @ResponseBody
        // If the method is annotated with @ResponseBody, the flaw cannot be exploited since a different ViewResolver will be used
        return new Object();
    }

    // http://127.0.0.1:8090/model/default::$%7BT(java.lang.Runtime).getRuntime().exec(%22touch%20lippo.txt%22)%7D.x
    @GetMapping("/model/defaultsafe*")
    @ResponseBody
    public Object modelDefaultStarSafe() {  // FP - Method annotated with @ResponseBody
        // If the method is annotated with @ResponseBody, the flaw cannot be exploited since a different ViewResolver will be used
        return new Object();
    }

    @GetMapping("/doc/{document}")
    public void getDocument(@PathVariable String document) {
        String me = "2";
        //
    }

}


@RestController
class SafeController {

    @GetMapping("/rest/_safe/fragment")
    public String safeFragment(@RequestParam String section) {
        return "welcome :: " + section; //FP, as @ResponseBody is inherited by @RestController
    }

    @GetMapping("/model/pathdefaultsafe2/{id}")
    public Object modelDefaultPathVariableSafe2(@PathVariable("id") String id) {
        // If the containing class is annotated with @ResponseBody, the flaw cannot be exploited since a different ViewResolver will be used
        return new Object(); // FP - as @ResponseBody is inherited by @RestController
    }

    @GetMapping("/model/defaultsafe2*")
    public Object modelDefaultStarSafe2() {
        // If the containing class is annotated with @ResponseBody, the flaw cannot be exploited since a different ViewResolver will be used
        return new Object(); // FP - as @ResponseBody is inherited by @RestController
    }

}
