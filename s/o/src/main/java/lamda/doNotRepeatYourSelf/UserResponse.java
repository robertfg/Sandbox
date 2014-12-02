package lamda.doNotRepeatYourSelf;

import java.util.List;

public class UserResponse {

	List<User> users;
	boolean success = true;

	public UserResponse() {

	}

	public UserResponse(List<User> users) {
		this.users = users;
	}
}