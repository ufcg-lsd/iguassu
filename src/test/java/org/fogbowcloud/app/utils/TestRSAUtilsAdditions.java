package org.fogbowcloud.app.utils;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import org.junit.Assert;
import org.junit.Test;

public class TestRSAUtilsAdditions {

	@Test
	public void TestEncryptDecrypt() throws IOException, GeneralSecurityException {
		String testString = "testString";
		
		KeyPair pair = RSAUtils.generateKeyPair();
		
		String encrypted = RSAUtils.encrypt(testString, pair.getPublic());
		
		String encrypted2 = RSAUtils.encrypt(testString, pair.getPublic());
		
		
		String decrypt = RSAUtils.decrypt(encrypted, pair.getPrivate());
		
		String decrypt2 = RSAUtils.decrypt(encrypted2, pair.getPrivate());
		
		String decrypt3 = RSAUtils.decrypt(encrypted, pair.getPrivate());
		
		Assert.assertEquals(decrypt, decrypt3);
		
		Assert.assertEquals(decrypt, decrypt2);
		
		Assert.assertEquals(testString, decrypt);
		
	}
	
	@Test
	public void TestSavePrivateKey() throws GeneralSecurityException, IOException {
		
		KeyPair pair = RSAUtils.generateKeyPair();
		
		String privateKeyValue = RSAUtils.savePrivateKey(pair.getPrivate());
		
		
		RSAPrivateKey pk = RSAUtils.getPrivateKeyFromString(privateKeyValue);
		
		
		Assert.assertEquals(pair.getPrivate(), pk);
	}
	
	@Test
	public void TestSavePublicKey() throws GeneralSecurityException, IOException {
		
		KeyPair pair = RSAUtils.generateKeyPair();
		
		String publicKeyValue = RSAUtils.savePublicKey(pair.getPublic());
		
		
		RSAPublicKey pk = RSAUtils.getPublicKeyFromString(publicKeyValue);
		
		
		Assert.assertEquals(pair.getPublic(), pk);
	}
	
}
