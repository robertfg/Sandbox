package lamda.doNotRepeatYourSelf;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

public class UserComponentAdapter {
	  UserComponent uc = new MockUserComponent();

	  UserResponse callComponent(UserRequest request, Function<UserRequest, UserResponse> func) {
	    try {
	      //Special validation function which validations the request object.

	      UserResponse response = func.apply(request);

	      //Special response handling that checks all response properties and
	      //performs logging and/or other operations.

	      return response;
	    } catch(Exception e) {
	      UserResponse response = new UserResponse();
	      //special code for applying error information into the response object.
	      return response;
	    }
	  }

	  List<User> fetchAllUsers() throws IOException {
	    return callComponent(new UserRequest(), uc::fetchAllUsers).users;
	  }

	  User fetchUser(Integer id) throws IOException {
	    return callComponent(new UserRequest(new User(id, "", "", 0)), uc::fetchUser).users.get(0);
	  }

	  boolean deleteUser(User user) throws IOException {
	    return callComponent(new UserRequest(user), uc::deleteUser).success;
	  }

	  boolean updateUser(User user) throws IOException {
	    return callComponent(new UserRequest(user), uc::updateUser).success;
	  }

	  boolean insertUser(User user) throws IOException {
	    return callComponent(new UserRequest(user), uc::insertUser).success;
	  }
	}