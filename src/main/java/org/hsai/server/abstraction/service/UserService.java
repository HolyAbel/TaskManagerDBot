package org.hsai.server.abstraction.service;

import org.hsai.server.repository.UserRepo;
import reactor.core.publisher.Mono;

public interface UserService {
    Mono<UserDto> getById(Long id);
    Mono<UserRepo.User> addUser(AddUserDto addUserDto, Long id);

    void addUser(Long id, String login);
    Mono<UserDto> signIn(UserService.SignInDto signInDto);
    record UserDto(
            Long id,
            String login,
            Long chatId
    ){
        public static UserDto fromDbEntity(UserRepo.User user){
            return new UserDto(
                    user.id(),
                    user.login(),
                    user.chatId()
            );
        }
    }

    record AddUserDto(
            String login,
            Long chatId
    ){
        public static UserRepo.User toDbEntity(UserService.AddUserDto addUserDto){
            return new UserRepo.User(
                    null,
                    addUserDto.login(),
                    addUserDto.chatId()
            );
        }
    }



    record SignInDto(
            String login,
            String password
    ){}
}
