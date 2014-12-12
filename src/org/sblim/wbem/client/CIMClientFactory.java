/**
 * CIMClientFactory.java
 *
 * (C) Copyright IBM Corp. 2005, 2009
 *
 * THIS FILE IS PROVIDED UNDER THE TERMS OF THE ECLIPSE PUBLIC LICENSE 
 * ("AGREEMENT"). ANY USE, REPRODUCTION OR DISTRIBUTION OF THIS FILE 
 * CONSTITUTES RECIPIENTS ACCEPTANCE OF THE AGREEMENT.
 *
 * You can obtain a current copy of the Eclipse Public License from
 * http://www.opensource.org/licenses/eclipse-1.0.php
 *
 * @author: Roberto Pineiro, IBM, roberto.pineiro@us.ibm.com  
 * @author: Chung-hao Tan, IBM ,chungtan@us.ibm.com
 * 
 * 
 * Change History
 * Flag       Date        Prog         Description
 *------------------------------------------------------------------------------- 
 *   18045    2005-08-10  pineiro5     Some code clean up in multiple points
 * 1498130    2006-05-31  lupusalex    Selection of xml parser on a per connection basis
 * 1535756    2006-08-07  lupusalex    Make code warning free
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 */

package org.sblim.wbem.client;

/**
 * A factory for CIMOMHandle instances. Used internally by the
 * <code>CIMCkient</code> class.
 */
import org.sblim.wbem.cim.*;
import org.sblim.wbem.util.SessionProperties;

import java.lang.reflect.Constructor;
import java.security.Principal;

public class CIMClientFactory {

	/**
	 * Constructs a <code>CIMOMHandle</code> with connection information to a
	 * target CIM server and a default namespace, using the specified principal,
	 * credential, protocol and session properties.
	 * 
	 * @param pNameSpace
	 *            Namespace identifying target CIM server and default CIM
	 *            namespace name. Must not be null. The host and port attributes
	 *            identify the target CIM server to connect to. Must be valid.
	 *            The namespace attribute identifies the default namespace name
	 *            to be used if object paths in subsequent method invocations do
	 *            not provide a namespace name. Must be valid.
	 * @param pPrincipal
	 *            Principal (e.g. userid) used to log on to the CIM Server. Must
	 *            not be null.
	 * @param pCredential
	 *            Credential (e.g. password) used to log on to the CIM Server.
	 *            Must not be null.
	 * @param pProtocol
	 *            Identifier for the CIM client protocol to be used. Must be
	 *            valid. Valid identifiers are the following constants:
	 *            <ul>
	 *            <li>{@link CIMClient#CIM_XML CIM_XML} - CIM Operations over
	 *            HTTP</li>
	 *            </ul>
	 *            and also the name of any Java class that implements
	 *            <code>CIMOMHandle</code>.
	 * @param pProperties
	 *            The session properties to be used. Might be either the global
	 *            properties or a connection specific properties instance. If
	 *            <code>null</code> the global properties are used.
	 * @return The CIMOMHandle
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_SUPPORTED CIM_ERR_NOT_SUPPORTED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 * @see CIMClient#CIMClient(CIMNameSpace, Principal, Object, String,
	 *      SessionProperties)
	 */
	public static CIMOMHandle getClient(CIMNameSpace pNameSpace, Principal pPrincipal,
			Object pCredential, String pProtocol, SessionProperties pProperties)
			throws CIMException {

		CIMOMHandle client = null;
		try {
			Class c = null;
			if (pProtocol.equals(CIMClient.CIM_XML)) { return new CIMClientXML(pNameSpace,
					pPrincipal, pCredential, pProtocol, (pProperties != null) ? pProperties
							: SessionProperties.getGlobalProperties()); }

			c = Class.forName(pProtocol);
			if (c != null) {
				Constructor constructor = c.getConstructor(new Class[] { CIMNameSpace.class,
						Principal.class, Object.class });
				client = (CIMOMHandle) constructor.newInstance(new Object[] { pNameSpace,
						pPrincipal, pCredential });
			}
		} catch (CIMException e) {
			throw e;
		} catch (Exception e) {
			throw new CIMException(CIMException.CIM_ERR_FAILED,
					"CIMClient not found for the specified protocol(" + pProtocol + ")", e);
		}
		return client;
	}

	/**
	 * Constructs a <code>CIMOMHandle</code> with connection information to a
	 * target CIM server and a default namespace, using the specified principal,
	 * credential and protocol. The global <code>SessionPropeties</code> are
	 * applied.
	 * 
	 * @param pNameSpace
	 *            Namespace identifying target CIM server and default CIM
	 *            namespace name. Must not be null. The host and port attributes
	 *            identify the target CIM server to connect to. Must be valid.
	 *            The namespace attribute identifies the default namespace name
	 *            to be used if object paths in subsequent method invocations do
	 *            not provide a namespace name. Must be valid.
	 * @param pPrincipal
	 *            Principal (e.g. userid) used to log on to the CIM Server. Must
	 *            not be null.
	 * @param pCredential
	 *            Credential (e.g. password) used to log on to the CIM Server.
	 *            Must not be null.
	 * @param pProtocol
	 *            Identifier for the CIM client protocol to be used. Must be
	 *            valid. Valid identifiers are the following constants:
	 *            <ul>
	 *            <li>{@link CIMClient#CIM_XML CIM_XML} - CIM Operations over
	 *            HTTP</li>
	 *            </ul>
	 *            and also the name of any Java class that implements
	 *            <code>CIMOMHandle</code>.
	 * @return The CIMOMHandle
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_SUPPORTED CIM_ERR_NOT_SUPPORTED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 * @deprecated Use getClient(CIMNameSpace, Principal, Object, String,
	 *             SessionProperties) instead
	 * @see #getClient(CIMNameSpace, Principal, Object, String,
	 *      SessionProperties)
	 */
	public static CIMOMHandle getClient(CIMNameSpace pNameSpace, Principal pPrincipal,
			Object pCredential, String pProtocol) throws CIMException {

		return getClient(pNameSpace, pPrincipal, pCredential, pProtocol, null);

	}
}
