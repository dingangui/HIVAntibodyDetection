package HAD.mapper;

import HAD.entity.User;

public interface UserRepository {
  User findByUsername(String username);

  User login(User user);

  void save(User user);
}
