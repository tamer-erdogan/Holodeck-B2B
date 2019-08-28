/*
 * Copyright (C) 2017 The Holodeck B2B Team.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.holodeckb2b.interfaces.security.trust;

import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.List;

import org.holodeckb2b.interfaces.pmode.ISigningConfiguration;
import org.holodeckb2b.interfaces.security.SecurityProcessingException;

/**
 * Defines the interface of the Holodeck B2B <i>Certificate Manager</i> component which is responsible for providing the 
 * private keys and X509v3 certificates to the components processing the messages and validating trust in certificates.
 * <p>The interface distinguishes between the key pairs used by Holodeck B2B for signing and decryption and certificates 
 * of trading partners used for encryption and identification.  
 * The key pairs consist of the private key and at least the certificate containing the corresponding public key. 
 * Sometimes however when signing a message the signature needs to include a certificate chain up to a common trusted 
 * root. Therefore key pairs can contain the such a chain. Trading partner certificates relate directly to the trading 
 * partner and contain its public key which should be used for encryption.<br>
 * All key pairs and trading partner certificates must have a unique "alias" that can be used in the security 
 * configuration section of the P-Mode. How key pairs and partner certificates are registered with the Certificate 
 * Manager is not defined by this interface and left up to the implementation. 
 * <p>How the validation of trust in a certificate (path) is established is implementation dependent and therefore the 
 * only defined methods are to validate the trust. Two versions of the validation method are defined, one for generic 
 * validation of trust according to the Certificate Manager's trust policy and one that uses additional configuration
 * that applies to the signature for which the certificate (path) was used. The implementation of the latter is optional
 * and can be checked using the {@link #supportsConfigBasedValidation()} method.
 * 
 * @author Sander Fieten (sander at holodeck-b2b.org)
 * @since HB2B_NEXT_VERSION A <i>Certificate Manager</i> already existed in version 4.x as part of the <i>Security 
 * 		Provider</i>. Its functionality however is more generic and therefore it has been decoupled and split off into 
 * 		a stand alone component. As trust validation and management have been left to the implementation the related 
 * 		methods from the 4.x version have been removed.
 */
public interface ICertificateManager {
	
    /**
     * Gets the name of this Certificate Manager.
     * <p>The name is only informational and mainly used for logging. However it is recommended to clearly identify the 
     * certificate manager.
     *
     * @return The name of this Certificate Manager.
     */
    String getName();

    /**
     * Initializes the Certificate Manager. This method is called once at startup of the Holodeck B2B instance. Since
     * the message processing depends on the correct functioning of the Certificate Manager this method MUST ensure that
     * that all required configuration and data is available. Required configuration parameters must be implemented by 
     * the Certificate Manager.
     *
     * @param hb2bHome  Path to the Holodeck B2B home directory.
     * @throws SecurityProcessingException When the certificate manager can not be initialized correctly.
     */
    void init(final String hb2bHome) throws SecurityProcessingException;

    /**
     * Gets the key pair registered under the given alias.
     *
     * @param alias     The alias of the key pair to retrieve
     * @param password  The password needed to access the key pair.
     * @return      	The key pair if it was found, or<br>
     * 					<code>null</code> if no key pair is registered under the given alias
     * @throws SecurityProcessingException When there is a problem in retrieving the key pair.
     */
    KeyStore.PrivateKeyEntry getKeyPair(final String alias, final String password) throws SecurityProcessingException;
   
    /**
     * Gets the trading partner's certificate registered under the given alias.
     *
     * @param alias The alias of the certificate to retrieve
     * @return      Trading partner's certificate if it was found, or<br>
     * 				<code>null</code> if no certificate is registered under the given alias
     * @throws SecurityProcessingException When there is a problem in retrieving the certificate.
     */
    X509Certificate getCertificate(final String alias) throws SecurityProcessingException;
    
    /**
     * Searches for the given certificate in the set of trading partner certificates registered with Certificate Manager 
     * and returns the alias if found.
     *
     * @param cert  The certificate to search for
     * @return      The alias under which the certificate is registered if it was found, or<br><code>null</code> if the
     *              certificate is not registered
     * @throws SecurityProcessingException When there is a problem in searching for the certificate.
     * @since HB2B_NEXT_VERSION This was the alternative <code>getCertificate</code> method from version 4.x   
     */
    String findCertificate(final X509Certificate cert) throws SecurityProcessingException;

    /**
     * Checks if the given certificate path is trusted for the validation of signatures. The Certificate Manager may
     * extend the given path with already registered trusted certificates to perform the actual trust validation. 
     * 
     * @param certs	List of certificates that form the path to validate trust in. Must be in forward order.  
     * @return		An instance of {@link IValidationResult} describing the validation result
     * @throws SecurityProcessingException 	When the trust cannot be validated due to some error. NOTE: This exception
     * 										MUST only be used to indicate errors that prevent checking the trust. When
     * 										thrown it indicates only that the trust could not be checked, i.e. the trust
     * 										in the certificate is undetermined. 
     * @since HB2B_NEXT_VERSION
     */
    IValidationResult validateTrust(final List<X509Certificate> certs) throws SecurityProcessingException;
    
    /**
     * Checks if the given certificate path is trusted for the validation of a signature given the specific signing
     * configuration. The Certificate Manager may extend the given path with already registered trusted certificates to 
     * perform the actual trust validation.
     * <p>NOTE: This is an optional function of the <i>Certificate Manager</i> which by default returns the generic 
     * trust in the given certificate. The {@link #supportsConfigBasedValidation()} method can be used to determine
     * whether the message based check is supported.    
     * 
     * @param certs		List of certificates that form the path to validate trust in. Must be in forward order.
     * @param sigCfg	Configuration that applies to signature for which the certificate (path) to validate applies
     * @return			An instance of {@link IValidationResult} describing the validation result
     * @throws SecurityProcessingException 	When the trust cannot be validated due to some error. NOTE: This exception
     * 										MUST only be used to indicate errors that prevent checking the trust. When
     * 										thrown it indicates only that the trust could not be checked, i.e. the trust
     * 										in the certificate is undetermined. 
     * @since HB2B_NEXT_VERSION
     */
    default IValidationResult validateTrust(final List<X509Certificate> certs, final ISigningConfiguration sigCfg) 
    																				throws SecurityProcessingException {
    	return validateTrust(certs);
    }
    
    /**
     * Indicates whether the <i>Certificate Manager</i> implementation supports configuration based trust validation.  
     * 
     * @return	<code>true</code> if supported, <code>false</code> if not. 
     * 			Since this is an optional feature <code>false</code> is default.
     * @since HB2B_NEXT_VERSION
     */
    default boolean supportsConfigBasedValidation() {
    	return false;
    }    
}
