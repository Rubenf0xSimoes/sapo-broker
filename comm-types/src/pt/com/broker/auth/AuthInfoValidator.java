package pt.com.broker.auth;

public interface AuthInfoValidator
{
	AuthValidationResult validate(AuthInfo clientAuthInfo) throws Exception;

	void init();
}
