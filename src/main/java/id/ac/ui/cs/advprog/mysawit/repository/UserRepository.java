package id.ac.ui.cs.advprog.mysawit.repository;


import id.ac.ui.cs.advprog.mysawit.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    boolean existsByUsername(String username);
}