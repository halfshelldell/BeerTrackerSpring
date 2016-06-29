package com.theironyard.controllers;

import com.theironyard.PasswordStorage;
import com.theironyard.entities.Beer;
import com.theironyard.entities.User;
import com.theironyard.services.BeerRepository;
import com.theironyard.services.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpSession;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

/**
 * Created by zach on 11/10/15.
 */

@Controller
public class BeerTrackerController {
    @Autowired
    BeerRepository beers;

    @Autowired
    UserRepository users;

    @PostConstruct
    public void init() throws InvalidKeySpecException, NoSuchAlgorithmException, PasswordStorage.CannotPerformOperationException {
        User user = users.findByName("Zach");
        if (user == null) {
            user = new User();
            user.name = "Zach";
            user.password = PasswordStorage.createHash("hunter2");
            users.save(user);
        }
    }

    @RequestMapping(path = "/", method = RequestMethod.GET)
    public String home(HttpSession session, Model model, String type, Integer calories, String search) {
        String username = (String) session.getAttribute("username");
        User user = users.findByName(username);

        if (username == null) {
            return "login";
        }

        if (search != null) {
            model.addAttribute("beers", beers.searchByName(search));
        } else if (type != null && calories != null) {
            model.addAttribute("beers", beers.findByTypeAndCaloriesIsLessThanEqual(type, calories));
        } else if (type != null) {
            model.addAttribute("beers", beers.findByTypeOrderByNameAsc(type));
        } else {
            model.addAttribute("beers", beers.findAll());
        }
        return "home";
    }

    @RequestMapping(path = "/add-beer", method = RequestMethod.POST)
    public String addBeer(String name, String type, int calories, HttpSession session) throws Exception {
        String username = (String) session.getAttribute("username");
        User user = users.findByName(username);
        Beer beer = new Beer(name, type, calories, user);
        beers.save(beer);
        return "redirect:/";
    }

    /*@RequestMapping(path = "/edit-beer", method = RequestMethod.POST)
    public String editBeer(int id, String name, String type, HttpSession session) throws Exception {
        if (session.getAttribute("username") == null) {
            throw new Exception("Not logged in.");
        }
        Beer beer = beers.findOne(id);
        beer.name = name;
        beer.type = type;
        beers.save(beer);
        return "redirect:/";
    }*/

    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public String login(String username, String password, HttpSession session) throws Exception {
        User user = users.findByName(username);
        if (user == null) {
            user = new User(username, PasswordStorage.createHash(password));
            users.save(user);
        }
        else if (!PasswordStorage.verifyPassword(password, user.password)) {
            throw new Exception("Wrong password");
        }

        session.setAttribute("username", username);
        return "redirect:/";
    }

    @RequestMapping(path = "/logout", method = RequestMethod.POST)
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
