package org.hsai.server.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.hsai.server.abstraction.service.UserService;
import org.hsai.server.repository.UserRepo;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
public record UserController(
        UserService userService
) {

    @GetMapping("/getUser/{id}")
    public Mono<UserService.UserDto> findById(@PathVariable Long id){
        return userService.getById(id);
    }

    @PostMapping("/addUser")
    public Mono<UserRepo.User> addUser(@RequestBody UserService.AddUserDto addUserDto, @PathVariable Long id) { return userService.addUser(addUserDto, id);}

    @PostMapping("/signUp")
    public Mono<UserRepo.User> signUp(@RequestBody UserService.AddUserDto addUserDto, @PathVariable Long id) { return userService.addUser(addUserDto, id);}

    @PostMapping("/signIn")
    public Mono<UserService.UserDto> signIn(@RequestBody UserService.SignInDto signInDto, HttpServletResponse response) {
        return userService.signIn(signInDto)
                .doOnNext(user -> {
                    var cookie = new Cookie("userId", String.valueOf(user.id()));
                    cookie.setMaxAge(3600);
                    cookie.setPath("/");
                    response.addCookie(cookie );
                });
    }
}
