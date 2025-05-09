package com.WebBiblioteca.Controller;

import com.WebBiblioteca.Model.User;
import com.WebBiblioteca.Service.UserService;
import com.WebBiblioteca.Utilidades.ExceptionUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;


@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }
    @GetMapping("/")
    public HashMap<Long,User> getAllUsers() {
        return userService.getAllUsers();
    }
    @PostMapping("/add")
    public ResponseEntity<?> addUser(@Valid @RequestBody User user) {
        try {
            User newuser = userService.addUser(user);
            return ResponseEntity.ok(newuser);
        }catch (ExceptionUser ex){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateUser(@RequestBody User user,@Valid @PathVariable Long id){
        User updatedUser = userService.updateUser(user,id);
        if(updatedUser != null){
            return ResponseEntity.ok(updatedUser);
        }else{
            return ResponseEntity.notFound().build();
        }
    }
    @PatchMapping("/update/{id}")
    public ResponseEntity<?> updatePartialUser( @RequestBody User partialUser,@PathVariable Long id) {
        User updatedUser = userService.updateUser(partialUser,id);
        if (updatedUser != null) {
            return ResponseEntity.ok(updatedUser);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        boolean result = userService.deleteUser(id);
        if(result){
            return ResponseEntity.ok("User deleted successfully");
        }else{
            return ResponseEntity.notFound().build();
        }
    }
    @PostMapping("/login/{email}/{password}")
    public ResponseEntity<?> login(@PathVariable String email,@PathVariable String password){
        User user = userService.loginUser(email,password);
        if(user != null){
            return ResponseEntity.ok(user);
        }else{
            return new ResponseEntity<>("Invalid email or password", HttpStatus.UNAUTHORIZED);
        }
    }
}
