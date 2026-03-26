package com.wardwatch.protection;

public interface PasswordProtected {
	boolean wardWatch$isProtected();

	String wardWatch$getPassword();

	void wardWatch$setPassword(String password);

	void wardWatch$clearPassword();
}
