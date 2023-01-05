package ru.kata.spring.boot_security.demo.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.kata.spring.boot_security.demo.models.Role;
import ru.kata.spring.boot_security.demo.models.User;
import ru.kata.spring.boot_security.demo.repositories.UserRepository;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.stream.Collectors;
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private UserRepository userRepository;

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    // обертка над методом из интерфейса UserRepository
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // переопределяем метод из UserDetailsService
    // по имени пользователя вернет самого пользователя
    @Override
    @Transactional // чтобы коллекция ролей подгрузилась нужно загнать все в одну транзакцию иначе при
    // залогинивании вылезает failed to lazily initialize a collection of role:
    // ru.tinyakov.security_training.entities.User.roles, could not initialize proxy - no Session
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = findByUsername(username); //  если юзер в базе есть - мы его получим, если нет, то получим null
        if(user == null) {
            throw new UsernameNotFoundException(String.format("User '%s' not found", username)); // юзер с таким именем не найден
        }
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(),
                mapRolesToAuthorities(user.getRoles()));
    }


    // получаем коллекцию авторитис(прав доступа) из коллекции ролей и передаем в loadUserByUsername
    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(Collection<Role> roles){
        return roles.stream().map(r -> new SimpleGrantedAuthority(r.getName())).collect(Collectors.toList());
    }
}
