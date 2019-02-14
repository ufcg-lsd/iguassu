package org.fogbowcloud.app.utils.authenticator;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.model.LDAPUser;
import org.fogbowcloud.app.model.User;
import org.fogbowcloud.app.utils.constants.IguassuPropertiesConstants;

public class LDAPAuthenticator implements IguassuAuthenticator {

	public static final String AUTH_NAME = "ldapauth";
	private static final String ENCRYPT_TYPE = ":TYPE:";
	private static final String ENCRYPT_PASS = ":PASS:";
	private static final String PASSWORD_ENCRYPTED = "{" + ENCRYPT_TYPE + "}" + ENCRYPT_PASS;

	private String ldapBase;
	private String encryptType;

	private static final Logger LOGGER = Logger.getLogger(LDAPAuthenticator.class);
	private String ldapUrl;

	public LDAPAuthenticator(Properties properties) {
		this.ldapUrl = properties.getProperty(IguassuPropertiesConstants.LDAP_AUTHENTICATION_URL);
		this.ldapBase = properties.getProperty(IguassuPropertiesConstants.LDAP_AUTHENTICATION_BASE);
		this.encryptType = properties.getProperty(IguassuPropertiesConstants.ENCRYPTION_TYPE);
	}

	@Override
	public User authenticateUser(Credential credential){
		String username = credential.getUsername();
//		String password = credential.getPassword();
		String password = credential.getToken();
		LOGGER.debug("Authenticating LDAP user: " + username);
		User user = null;
		try {
			user = new LDAPUser(ldapAuthenticate(username, password), username);
		} catch (Exception e) {
			LOGGER.error("Error while authenticate " + username +" - Error: ", e);
		}
		return user;
	}

	private String ldapAuthenticate(String uid, String password) throws Exception {
		Hashtable<String, String> env = new Hashtable<>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, ldapUrl);
		env.put(Context.SECURITY_AUTHENTICATION, "simple");

		DirContext dirContext = null;
		String userName = null;
		try {
			password = encryptPassword(password);

			dirContext = new InitialDirContext(env);

			//Search the directory to get User Name and Domain from UID
			String filter = "(&(objectClass=inetOrgPerson)(uid={0}))";
			SearchControls ctls = new SearchControls();
			ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			ctls.setReturningAttributes(new String[0]);
			ctls.setReturningObjFlag(true);
			NamingEnumeration<SearchResult> enm = dirContext.search(ldapBase, filter, new String[] { uid }, ctls);

			String dn = null;

			if (enm.hasMore()) {
				SearchResult result = enm.next();
				dn = result.getNameInNamespace();
				userName = extractUserName(result);
			}

			if (dn == null || enm.hasMore()) {
				// uid not found or not unique
				throw new NamingException("Authentication failed");
			}
			
			// Bind with found DN and given password
			dirContext.addToEnvironment(Context.SECURITY_PRINCIPAL, dn);
			dirContext.addToEnvironment(Context.SECURITY_CREDENTIALS, password);
			// Perform a lookup in order to force a bind operation with JNDI
			dirContext.lookup(dn);

			enm.close();

			return userName;
		} finally {
			if (dirContext != null) {
				dirContext.close();
			}
		}
	}

	private String encryptPassword(String password) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		if (encryptType == null || encryptType.isEmpty()) {
			return password;
		}

		MessageDigest algorithm = MessageDigest.getInstance(encryptType);
		byte messageDigest[] = algorithm.digest(password.getBytes("UTF-8"));

		StringBuilder hexString = new StringBuilder();
		for (byte b : messageDigest) {
			hexString.append(String.format("%02X", 0xFF & b));
		}

		return PASSWORD_ENCRYPTED.replaceAll(ENCRYPT_TYPE, encryptType).replaceAll(ENCRYPT_PASS, hexString.toString());
	}
	
	private String extractUserName(SearchResult result) {
		String nameGroup[] = result.getName().split(",");
		if(nameGroup.length > 0){
			String cnName[] = nameGroup[0].split("=");
			if(cnName.length > 1){
				return cnName[1];
			}
		}
		return null;
	}

	@Override
	public String getAuthenticatorName() {
		return AUTH_NAME;
	}

	@Override
	public User addUser(String username, String password) {
		throw new RuntimeException("Add a user is not allowed in the LDAP authenticator plugin.");
	}

	@Override
	public User getUserByUsername(String username) {
		throw new RuntimeException("Method not implemented.");
	}
	
}
