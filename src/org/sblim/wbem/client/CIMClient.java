/**
 * CIMClient.java
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
 * ------------------------------------------------------------------------------- 
 * 1498130    2006-05-31  lupusalex    Selection of xml parser on a per connection basis
 * 1523854    2006-07-17  lupusalex    deep always false with ecn
 * 1535756    2006-08-07  lupusalex    Make code warning free
 * 1646434    2007-01-28  lupusalex    CIMClient close() invalidates all it's enumerations
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 * 
 * Temporary place during docu project for keeping ideas and proposals:
 * 
 *   General docu To-Do list:
 *     TODO Docu: Move preconditions with a method scope from the args to the method description
 *     TODO Docu: Verify whether all "must not be null"s on any parameters are documented.
 *     TODO Docu: Verify which methods require which CIM server capabilities and document that
 * 
 *   Proposed changes (being reviewed):
 *     1. Constants ITSANM_CIMXML, ITSANM_SOAP, ITSANM_LOCAL never used, remove them.
 *     2. WQL1-4 constants never used, remove them.
 *     3. Deprecate enumerateInstances(CIMObjectPath) and getInstance(CIMObjectPath)
 *        because they force localOnly to have the DMTF deprecated value of true.
 *     4. Deprecate the array version of invokeMethod() also at the Java level, it was
 *        already documented as deprecated.
 *     5. Deprecate enumNameSpace() and define a better one, since it is ill defined:
 * 			(1) caller has to know the Interop namespace name
 * 			(2) only instance paths are returned
 * 			(3) the "deep" argument is not used
 *     6. Allow the name of the Interop namespace to be stored inside of the CIMClient object.
 * 		  Would be obtained by a client app from the SLP client, and passed to the CIMClient ctor.
 *        -> Add a new ctor that does this.
 *     7. Add a method for the new ExecuteQuery CIM operation that returns a table.
 *     8. createQualifierType(), setQualifierType() and enumQualifierTypes() take an object path as input,
 *        where the namespace part is used as the target namespace, and its class part gets ignored
 * 		  but is yet required to be non-null. Remove this check, so that the path can be a proper
 * 		  namespace path.
 *
 *   Further ideas (to be fleshed out):
 *     1. Should we introduce separate types for class/instance/namespace/qualifiertype
 *        object paths ? Note that qualifierType is a misuse of the CIM object path, where
 *        the class attribute is used to hold the qualifier name.
 * 
 */

package org.sblim.wbem.client;

import java.security.Principal;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.sblim.wbem.cim.CIMArgument;
import org.sblim.wbem.cim.CIMClass;
import org.sblim.wbem.cim.CIMException;
import org.sblim.wbem.cim.CIMInstance;
import org.sblim.wbem.cim.CIMNameSpace;
import org.sblim.wbem.cim.CIMObjectPath;
import org.sblim.wbem.cim.CIMQualifierType;
import org.sblim.wbem.cim.CIMValue;
import org.sblim.wbem.client.indications.CIMListener;
import org.sblim.wbem.util.Benchmark;
import org.sblim.wbem.util.SessionProperties;

/**
 * A CIM client providing the main interface for a CIM client application to
 * interact with a CIM server.
 * 
 * <p>
 * Each <code>CIMClient</code> instance is connected to a single target CIM
 * server and can be used to interact with all CIM namespaces within that
 * server.
 * </p>
 */
public class CIMClient {

	/**
	 * TODO Proposal: Constants ITSANM_CIMXML, ITSANM_SOAP, ITSANM_LOCAL never
	 * used, remove them.
	 */
	public static final String ITSANM_CIMXML = "ITSANM_CIMXML";

	public static final String ITSANM_SOAP = "ITSANM_SOAP";

	public static final String ITSANM_LOCAL = "ITSANM_LOCAL";

	/**
	 * Protocol identifier for CIM Operations over HTTP.
	 */
	public static final String CIM_XML = "CIM_XML";

	/**
	 * QueryLanguage identifier for WBEM Query Language (WQL).
	 */
	public static final String WQL = "WQL";

	/**
	 * QueryLanguage identifier for CIM Query Language (CQL).
	 */
	public static final String CQL = "CQL";

	/**
	 * WBEM Query Language level 1. TODO Proposal: WQL1 constant is never used,
	 * remove it.
	 */
	public static final String WQL1 = "WBEMSQL1";

	/**
	 * WBEM Query Language level 2. TODO Proposal: WQL2 constant is never used,
	 * remove it.
	 */
	public static final String WQL2 = "WBEMSQL2";

	/**
	 * WBEM Query Language level 3. TODO Proposal: WQL3 constant is never used,
	 * remove it.
	 */
	public static final String WQL3 = "WBEMSQL3";

	/**
	 * WBEM Query Language level 4. TODO Proposal: WQL4 constant is never used,
	 * remove it.
	 */
	public static final String WQL4 = "WBEMSQL4";

	static {
		try {
			SessionProperties.getGlobalProperties().getLogger();
		} catch (RuntimeException e) {
			Logger logger = SessionProperties.getGlobalProperties().getLogger();
			if (logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, "Some error while loading properties", e);
			}
		}
	}

	private CIMOMHandle iHandle;

	/**
	 * Initializes this <code>CIMClient</code> with connection information to
	 * a target CIM server and a default namespace, using the specified
	 * principal and credential, and using the default protocol
	 * <code>CIM_XML</code> (CIM Operations over HTTP). The global session
	 * properties are applied.
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
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_SUPPORTED CIM_ERR_NOT_SUPPORTED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 * @see #CIMClient(CIMNameSpace, Principal, Object, String,
	 *      SessionProperties)
	 */
	public CIMClient(CIMNameSpace pNameSpace, Principal pPrincipal, Object pCredential)
			throws CIMException {

		this(pNameSpace, pPrincipal, pCredential, CIM_XML);
	}

	/**
	 * Initializes this <code>CIMClient</code> with connection information to
	 * a target CIM server and a default namespace, using the specified
	 * principal, credential and protocol. The global session properties are
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
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_SUPPORTED CIM_ERR_NOT_SUPPORTED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 * 
	 * @see #CIMClient(CIMNameSpace, Principal, Object, String,
	 *      SessionProperties)
	 */
	public CIMClient(CIMNameSpace pNameSpace, Principal pPrincipal, Object pCredential,
			String pProtocol) throws CIMException {

		this(pNameSpace, pPrincipal, pCredential, pProtocol, null);
	}

	/**
	 * Initializes this <code>CIMClient</code> with connection information to
	 * a target CIM server and a default namespace, using the specified
	 * principal, credential and protocol.
	 * 
	 * TODO Proposal: Allow the name of the Interop namespace to be passed (add
	 * a new ctor that does this).
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
	 *            The session properties to apply. Might be either the global
	 *            session properties or a connection specific property instance.
	 *            If <code>null</code> the global properties are used.
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_SUPPORTED CIM_ERR_NOT_SUPPORTED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 * @see CIMOMHandle
	 * @see SessionProperties
	 * @see SessionProperties#getGlobalProperties()
	 */
	public CIMClient(CIMNameSpace pNameSpace, Principal pPrincipal, Object pCredential,
			String pProtocol, SessionProperties pProperties) throws CIMException {

		if (pNameSpace == null) { throw new CIMException(CIMException.CIM_ERR_INVALID_PARAMETER,
				"Null namespace"); }
		iHandle = CIMClientFactory.getClient(pNameSpace, pPrincipal, pCredential, pProtocol,
				pProperties);
	}

	/**
	 * Initializes this <code>CIMClient</code> from an existing
	 * <code>CIMOMHandle</code>.
	 * 
	 * <p>
	 * A <code>CIMOMHandle</code> can be obtained from a
	 * <code>CIMClientFactory</code>. This allows to extend the CIM client
	 * (for instance with an additional protocol) without changing the interface
	 * to the CIM client application.
	 * </p>
	 * 
	 * @param pCimomHandle
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             </dl>
	 * @see CIMOMHandle
	 * @see CIMClientFactory
	 */
	public CIMClient(CIMOMHandle pCimomHandle) throws CIMException {
		if (pCimomHandle == null) throw new CIMException(CIMException.CIM_ERR_INVALID_PARAMETER,
				"null cimomHandler parameter");
		iHandle = pCimomHandle;
	}

	private void preCheck() throws CIMException {
		if (iHandle == null) throw new CIMException(CIMException.CIM_ERR_FAILED,
				"CIMClient is not bounded to a CIMOM");
	}

	/**
	 * Enumerates the CIM objects on the target CIM server that are at the other
	 * end of all associations connecting to a source CIM object at this end of
	 * these associations, and returns CIM object paths to these objects.
	 * 
	 * <p>
	 * If the source CIM object is an instance, then the associated instances
	 * are enumerated. If the source CIM object is a class, then the associated
	 * classes are enumerated.
	 * </p>
	 * 
	 * @param pObjectPath
	 *            CIM object path to the source CIM object (instance or class)
	 *            whose associated CIM objects are to be returned. Must be an
	 *            intra-server or intra-namespace object path within the target
	 *            CIM server. In the latter case, the default namespace name is
	 *            added to this parameter before being used. The source CIM
	 *            object must exist.
	 * @param pAssociationClass
	 *            Allows to restrict the set of returned objects. If null, the
	 *            set of objects will not be restricted. If not null, must be a
	 *            valid CIM association class name, and the set of objects will
	 *            be restricted by removing any objects that are not associated
	 *            to the source CIM object via the specified class or one of its
	 *            subclasses. When combined with other object restriction
	 *            filters, all restrictions are accumulated.
	 * @param pResultClass
	 *            Allows to restrict the set of returned objects. If null, the
	 *            set of objects will not be restricted. If not null, must be a
	 *            valid CIM class name, and the set of objects will be
	 *            restricted by removing any objects that do not satisfy the
	 *            following conditions: (1) If the source CIM object is an
	 *            instance, the enumerated objects are CIM instances of the
	 *            specified class or one of its subclasses. (2) If the source
	 *            CIM object is a class, the enumerated objects are the
	 *            specified class or one of its subclasses. When combined with
	 *            other object restriction filters, all restrictions are
	 *            accumulated.
	 * @param pRole
	 *            Allows to restrict the set of returned objects. If null, the
	 *            set of objects will not be restricted. If not null, must be a
	 *            valid CIM property name, and the set of objects will be
	 *            restricted by removing any objects that are not associated to
	 *            the source object via an association in which the source
	 *            object plays the specified pRole (i.e. the name of the
	 *            reference in the association class that refers to the source
	 *            object must match the value of this parameter). When combined
	 *            with other object restriction filters, all restrictions are
	 *            accumulated.
	 * @param pResultRole
	 *            Allows to restrict the set of returned objects. If null, the
	 *            set of objects will not be restricted. If not null, must be a
	 *            valid CIM property name, and the set of objects will be
	 *            restricted by removing any objects that are not associated to
	 *            the source object via an association in which the returned
	 *            object plays the specified pRole (i.e. the name of the
	 *            reference in the association class that refers to the returned
	 *            object must match the value of this parameter). When combined
	 *            with other object restriction filters, all restrictions are
	 *            accumulated.
	 * @return A <code>CIMEnumeration</code> object containing zero or more
	 *         <code>CIMObjectPath</code> objects referencing the enumerated
	 *         classes or instances using global CIM class or instance paths.
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_SUPPORTED CIM_ERR_NOT_SUPPORTED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_NAMESPACE CIM_ERR_INVALID_NAMESPACE}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 */
	public synchronized Enumeration associatorNames(CIMObjectPath pObjectPath,
			String pAssociationClass, String pResultClass, String pRole, String pResultRole)
			throws CIMException {
		Benchmark.startTimer();
		preCheck();
		Enumeration enumeration = iHandle.associatorNames(pObjectPath, pAssociationClass,
				pResultClass, pRole, pResultRole);
		Benchmark.stopTimer();
		return enumeration;
	}

	/**
	 * Enumerates the CIM objects on the target CIM server that are at the other
	 * end of all associations connecting to a source CIM object at this end of
	 * these associations, and returns CIM object paths to these objects.
	 * 
	 * <p>
	 * This method produces the same results as
	 * <code>associatorNames(pObjectPath, null, null, null, null)</code>
	 * </p>
	 * 
	 * @param pObjectPath
	 *            CIM object path to the source CIM object (instance or class)
	 *            whose associated CIM objects are to be returned. Must be an
	 *            intra-server or intra-namespace object path within the target
	 *            CIM server. In the latter case, the default namespace name is
	 *            added to this parameter before being used. The source CIM
	 *            object must exist.
	 * @return A <code>CIMEnumeration</code> object containing zero or more
	 *         <code>CIMObjectPath</code> objects referencing the enumerated
	 *         classes or instances using global CIM class or instance paths.
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_SUPPORTED CIM_ERR_NOT_SUPPORTED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_NAMESPACE CIM_ERR_INVALID_NAMESPACE}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 * 
	 * @see #associatorNames(CIMObjectPath, String, String, String, String)
	 */
	public synchronized Enumeration associatorNames(CIMObjectPath pObjectPath) throws CIMException {
		return associatorNames(pObjectPath, null, null, null, null);
	}

	/**
	 * Enumerates the CIM objects on the target CIM server that are at the other
	 * end of all associations connecting to a source CIM object at this end of
	 * these associations, and returns copies of these CIM objects.
	 * 
	 * <p>
	 * If the source CIM object is an instance, then the associated instances
	 * are enumerated. If the source CIM object is a class, then the associated
	 * classes are enumerated.
	 * </p>
	 * 
	 * @param pObjectPath
	 *            CIM object path to the source CIM object (instance or class)
	 *            whose associated CIM objects are to be returned. Must be an
	 *            intra-server or intra-namespace object path within the target
	 *            CIM server. In the latter case, the default namespace name is
	 *            added to this parameter before being used. The source CIM
	 *            object must exist.
	 * @param pAssociationClass
	 *            Allows to restrict the set of returned objects. If null, the
	 *            set of objects will not be restricted. If not null, must be a
	 *            valid CIM association class name, and the set of objects will
	 *            be restricted by removing any objects that are not associated
	 *            to the source CIM object via the specified class or one of its
	 *            subclasses. When combined with other object restriction
	 *            filters, all restrictions are accumulated.
	 * @param pResultClass
	 *            Allows to restrict the set of returned objects. If null, the
	 *            set of objects will not be restricted. If not null, must be a
	 *            valid CIM class name, and the set of objects will be
	 *            restricted by removing any objects that do not satisfy the
	 *            following conditions: (1) If the source CIM object is an
	 *            instance, the enumerated objects are CIM instances of the
	 *            specified class or one of its subclasses. (2) If the source
	 *            CIM object is a class, enumerated objects are the specified
	 *            class or one of its subclasses. When combined with other
	 *            object restriction filters, all restrictions are accumulated.
	 * @param pRole
	 *            Allows to restrict the set of returned objects. If null, the
	 *            set of objects will not be restricted. If not null, must be a
	 *            valid CIM property name, and the set of objects will be
	 *            restricted by removing any objects that are not associated to
	 *            the source object via an association in which the source
	 *            object plays the specified pRole (i.e. the name of the
	 *            reference in the association class that refers to the source
	 *            object must match the value of this parameter). When combined
	 *            with other object restriction filters, all restrictions are
	 *            accumulated.
	 * @param pResultRole
	 *            Allows to restrict the set of returned objects. If null, the
	 *            set of objects will not be restricted. If not null, must be a
	 *            valid CIM property name, and the set of objects will be
	 *            restricted by removing any objects that are not associated to
	 *            the source object via an association in which the returned
	 *            object plays the specified pRole (i.e. the name of the
	 *            reference in the association class that refers to the returned
	 *            object must match the value of this parameter). When combined
	 *            with other object restriction filters, all restrictions are
	 *            accumulated.
	 * @param pIncludeQualifiers
	 *            If false, no qualifiers will be included. If true, all
	 *            qualifiers for each object will be included in the objects
	 *            returned. <br>
	 *            <span style="color: rgb(255,0,0);">Deprecation Note: The CIM
	 *            Operations over HTTP specification [DSP0200] V1.2 has
	 *            deprecated this parameter. The preferred way to retrieve
	 *            qualifier information is to use the class operations (for
	 *            example, <code>getClass</code>) instead, and to set this
	 *            parameter to <code>false</code>.</span>
	 * @param pIncludeClassOrigin
	 *            If true, class origin information will be present in the
	 *            returned objects. Class origin information indicates the class
	 *            in which a CIM property, method, or reference was first
	 *            defined in the class hierarchy. It can be retrieved from the
	 *            Java objects representing these CIM elements by means of the
	 *            <code>getOriginClass()</code> method. If false, class origin
	 *            information will not be present in the returned objects.
	 * @param pPropertyList
	 *            An array of CIM property names that allows to restrict the set
	 *            of properties in the returned objects (classes or instances).
	 *            If null, the set of properties will not be restricted by this
	 *            parameter. If not null, the set of properties will be
	 *            restricted such that any properties missing from this list
	 *            will not be present in the returned objects. In this case, the
	 *            members of the array must define zero or more property names
	 *            of the specified class. This may include inherited property
	 *            names or property names explicitly defined in the specified
	 *            class, but must not include property names defined in
	 *            subclasses of the specified class. Duplicate property names
	 *            are tolerated and the duplicates are ignored.
	 * @return A <code>CIMEnumeration</code> object containing zero or more
	 *         <code>CIMInstance</code> or <code>CIMClass</code> objects.
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_SUPPORTED CIM_ERR_NOT_SUPPORTED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_NAMESPACE CIM_ERR_INVALID_NAMESPACE}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 * @see org.sblim.wbem.cim.CIMProperty#getOriginClass()
	 * @see org.sblim.wbem.cim.CIMMethod#getOriginClass()
	 * @see org.sblim.wbem.cim.CIMInstance
	 * @see org.sblim.wbem.cim.CIMClass
	 */
	public synchronized Enumeration associators(CIMObjectPath pObjectPath,
			String pAssociationClass, String pResultClass, String pRole, String pResultRole,
			boolean pIncludeQualifiers, boolean pIncludeClassOrigin, String[] pPropertyList)
			throws CIMException {
		Benchmark.startTimer();
		preCheck();
		Enumeration enumeration = iHandle.associators(pObjectPath, pAssociationClass, pResultClass,
				pRole, pResultRole, pIncludeQualifiers, pIncludeClassOrigin, pPropertyList);
		Benchmark.stopTimer();
		return enumeration;
	}

	/**
	 * Deletes a CIM instance on the target CIM server.
	 * 
	 * <p>
	 * Dependent on the semantics of the CIM class, this may result in also
	 * deleting the real world resource represented by the CIM instance.
	 * </p>
	 * 
	 * @param pInstancePath
	 *            CIM instance path to the CIM instance to be deleted. Must be
	 *            an intra-server or intra-namespace object path within the
	 *            target CIM server. In the latter case, the default namespace
	 *            name is added to this parameter before being used. The CIM
	 *            instance must exist.
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_SUPPORTED CIM_ERR_NOT_SUPPORTED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_NAMESPACE CIM_ERR_INVALID_NAMESPACE}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_CLASS CIM_ERR_INVALID_CLASS}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_FOUND CIM_ERR_NOT_FOUND}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 */
	public synchronized void deleteInstance(CIMObjectPath pInstancePath) throws CIMException {
		Benchmark.startTimer();
		preCheck();
		iHandle.deleteInstance(pInstancePath);
		Benchmark.stopTimer();
	}

	/**
	 * Enumerates the CIM instances on the target CIM server that are of the
	 * specified class or its subclasses and returns copies of these instances.
	 * 
	 * <p>
	 * This method produces the same results as
	 * <code>enumerateInstances(pClassPath, pDeepInheritance, false, false, false, null)</code>
	 * </p>
	 * 
	 * @param pClassPath
	 *            CIM class path to the CIM class whose instances will be
	 *            enumerated. Instances of its subclasses in the same namespace
	 *            will also be enumerated. Must be an intra-server or
	 *            intra-namespace object path within the target CIM server. In
	 *            the latter case, the default namespace name is added to this
	 *            parameter before being used. The CIM class must exist.
	 * @param pDeepInheritance
	 *            Allows to restrict the set of properties present in the
	 *            returned instances. If true, the set of properties will not be
	 *            restricted by this parameter. If false, the set of properties
	 *            will be restricted such that any properties in subclasses of
	 *            the specified class are removed. When combined with other
	 *            property restriction filters, all restrictions are
	 *            accumulated. Note: There is an erroneous interpretation of
	 *            this parameter which consists in believing that the parameter
	 *            removes all instances of subclasses from the enumerated set of
	 *            instances. This is wrong as it does not remove such instances,
	 *            instead it removes their subclass properties.
	 * @return A <code>CIMEnumeration</code> object containing zero or more
	 *         <code>CIMInstance</code> objects.
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_SUPPORTED CIM_ERR_NOT_SUPPORTED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_NAMESPACE CIM_ERR_INVALID_NAMESPACE}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_CLASS CIM_ERR_INVALID_CLASS}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 * @see #enumerateInstances(CIMObjectPath, boolean, boolean, boolean,
	 *      boolean, String[])
	 */
	public synchronized Enumeration enumInstances(CIMObjectPath pClassPath, boolean pDeepInheritance)
			throws CIMException {
		return enumerateInstances(pClassPath, pDeepInheritance, false, false, false, null);
	}

	/**
	 * Enumerates the CIM instances on the target CIM server that are of the
	 * specified class or its subclasses and returns copies of these instances.
	 * 
	 * <p>
	 * This method produces the same results as
	 * <code>enumerateInstances(pClassPath, pDeepInheritance, localOnly, false, false, null)</code>
	 * </p>
	 * 
	 * @param pClassPath
	 *            CIM class path to the CIM class whose instances will be
	 *            enumerated. Instances of its subclasses in the same namespace
	 *            will also be enumerated. Must be an intra-server or
	 *            intra-namespace object path within the target CIM server. In
	 *            the latter case, the default namespace name is added to this
	 *            parameter before being used. The CIM class must exist.
	 * @param pDeepInheritance
	 *            Allows to restrict the set of properties present in the
	 *            returned instances. If true, the set of properties will not be
	 *            restricted by this parameter. If false, the set of properties
	 *            will be restricted such that any properties in subclasses of
	 *            the specified class are removed. When combined with other
	 *            property restriction filters, all restrictions are
	 *            accumulated. Note: There is an erroneous interpretation of
	 *            this parameter which consists in believing that the parameter
	 *            removes all instances of subclasses from the enumerated set of
	 *            instances. This is wrong as it does not remove such instances,
	 *            instead it removes their subclass properties.
	 * @param pLocalOnly
	 *            Allows to restrict the set of properties present in the
	 *            returned instances. If false, the set of properties will not
	 *            be restricted by this parameter. If true, the set of
	 *            properties will be restricted in a way that dependens on the
	 *            CIM server implementation. When combined with other property
	 *            restriction filters, all restrictions are accumulated. <br>
	 *            <span style="color: rgb(255,0,0);">Deprecation Note: The CIM
	 *            Operations over HTTP specification [DSP0200] V1.2 has
	 *            deprecated this parameter. It is recommended that CIM client
	 *            applications set <code>pLocalOnly</code> to
	 *            <code>false</code> and not to rely on
	 *            <code>pLocalOnly</code> filtering. To minimize the impact of
	 *            implementing this recommendation in CIM client applications, a
	 *            CIM server may choose to treat the value of
	 *            <code>pLocalOnly</code> as <code>false</code>, regardless
	 *            how it is actually set in an operation. Details: In DSP0200
	 *            V1.0, the pLocalOnly parameter was specified ambiguously. In
	 *            DSP0200 V1.1, the definition of the pLocalOnly parameter was
	 *            modified with the intent to clarify the ambiguity, so that it
	 *            now restricted the set of properties to those of the specified
	 *            class (removing any properties of its superclasses). This
	 *            change introduced backward compatibility problems for CIM
	 *            client applications when working with CIM servers that
	 *            implemented <code>pLocalOnly</code> support with the (quite
	 *            common) interpretation of the V1.0 definition that restricted
	 *            the set of properties to those defined in the CIM class of
	 *            each instance and its superclasses. In DSP0200 V1.2, a CIM
	 *            server must consistently support a single interpretation of
	 *            the <code>pLocalOnly</code> parameter, but which one is left
	 *            open.</span>
	 * @return A <code>CIMEnumeration</code> object containing zero or more
	 *         <code>CIMInstance</code> objects.
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_SUPPORTED CIM_ERR_NOT_SUPPORTED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_NAMESPACE CIM_ERR_INVALID_NAMESPACE}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_CLASS CIM_ERR_INVALID_CLASS}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 * @see #enumerateInstances(CIMObjectPath, boolean, boolean, boolean,
	 *      boolean, String[])
	 */
	public synchronized Enumeration enumInstances(CIMObjectPath pClassPath,
			boolean pDeepInheritance, boolean pLocalOnly) throws CIMException {
		return enumerateInstances(pClassPath, pDeepInheritance, pLocalOnly, false, false, null);
	}

	/**
	 * Enumerates the CIM instances on the target CIM server that are of the
	 * specified class or its subclasses and returns copies of these instances.
	 * 
	 * <p>
	 * This method produces the same results as
	 * <code>enumerateInstances(pClassPath, pDeepInheritance, localOnly, false, false, null)</code>
	 * </p>
	 * 
	 * @param pClassPath
	 *            CIM class path to the CIM class whose instances will be
	 *            enumerated. Instances of its subclasses in the same namespace
	 *            will also be enumerated. Must be an intra-server or
	 *            intra-namespace object path within the target CIM server. In
	 *            the latter case, the default namespace name is added to this
	 *            parameter before being used. The CIM class must exist.
	 * @param pDeepInheritance
	 *            Allows to restrict the set of properties present in the
	 *            returned instances. If true, the set of properties will not be
	 *            restricted by this parameter. If false, the set of properties
	 *            will be restricted such that any properties in subclasses of
	 *            the specified class are removed. When combined with other
	 *            property restriction filters, all restrictions are
	 *            accumulated. Note: There is an erroneous interpretation of
	 *            this parameter which consists in believing that the parameter
	 *            removes all instances of subclasses from the enumerated set of
	 *            instances. This is wrong as it does not remove such instances,
	 *            instead it removes their subclass properties.
	 * @param pLocalOnly
	 *            Allows to restrict the set of properties present in the
	 *            returned instances. If false, the set of properties will not
	 *            be restricted by this parameter. If true, the set of
	 *            properties will be restricted in a way that dependens on the
	 *            CIM server implementation. When combined with other property
	 *            restriction filters, all restrictions are accumulated. <br>
	 *            <span style="color: rgb(255,0,0);">Deprecation Note: The CIM
	 *            Operations over HTTP specification [DSP0200] V1.2 has
	 *            deprecated this parameter. It is recommended that CIM client
	 *            applications set <code>pLocalOnly</code> to
	 *            <code>false</code> and not to rely on
	 *            <code>pLocalOnly</code> filtering. To minimize the impact of
	 *            implementing this recommendation in CIM client applications, a
	 *            CIM server may choose to treat the value of
	 *            <code>pLocalOnly</code> as <code>false</code>, regardless
	 *            how it is actually set in an operation. Details: In DSP0200
	 *            V1.0, the pLocalOnly parameter was specified ambiguously. In
	 *            DSP0200 V1.1, the definition of the pLocalOnly parameter was
	 *            modified with the intent to clarify the ambiguity, so that it
	 *            now restricted the set of properties to those of the specified
	 *            class (removing any properties of its superclasses). This
	 *            change introduced backward compatibility problems for CIM
	 *            client applications when working with CIM servers that
	 *            implemented <code>pLocalOnly</code> support with the (quite
	 *            common) interpretation of the V1.0 definition that restricted
	 *            the set of properties to those defined in the CIM class of
	 *            each instance and its superclasses. In DSP0200 V1.2, a CIM
	 *            server must consistently support a single interpretation of
	 *            the <code>pLocalOnly</code> parameter, but which one is left
	 *            open.</span>
	 * @return A <code>CIMEnumeration</code> object containing zero or more
	 *         <code>CIMInstance</code> objects.
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_SUPPORTED CIM_ERR_NOT_SUPPORTED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_NAMESPACE CIM_ERR_INVALID_NAMESPACE}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_CLASS CIM_ERR_INVALID_CLASS}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 * @see #enumerateInstances(CIMObjectPath, boolean, boolean, boolean,
	 *      boolean, String[])
	 */
	public synchronized Enumeration enumerateInstances(CIMObjectPath pClassPath,
			boolean pDeepInheritance, boolean pLocalOnly) throws CIMException {
		return enumerateInstances(pClassPath, pDeepInheritance, pLocalOnly, false, false, null);
	}

	/**
	 * Enumerates the CIM instances on the target CIM server that are of the
	 * specified class or its subclasses and returns copies of these instances.
	 * 
	 * @param pClassPath
	 *            CIM class path to the CIM class whose instances will be
	 *            enumerated. Instances of its subclasses in the same namespace
	 *            will also be enumerated. Must be an intra-server or
	 *            intra-namespace object path within the target CIM server. In
	 *            the latter case, the default namespace name is added to this
	 *            parameter before being used. The CIM class must exist.
	 * @param pDeepInheritance
	 *            Allows to restrict the set of properties present in the
	 *            returned instances. If true, the set of properties will not be
	 *            restricted by this parameter. If false, the set of properties
	 *            will be restricted such that any properties in subclasses of
	 *            the specified class are removed. When combined with other
	 *            property restriction filters, all restrictions are
	 *            accumulated. Note: There is an erroneous interpretation of
	 *            this parameter which consists in believing that the parameter
	 *            removes all instances of subclasses from the enumerated set of
	 *            instances. This is wrong as it does not remove such instances,
	 *            instead it removes their subclass properties.
	 * @param pLocalOnly
	 *            Allows to restrict the set of properties present in the
	 *            returned instances. If false, the set of properties will not
	 *            be restricted by this parameter. If true, the set of
	 *            properties will be restricted in a way that dependens on the
	 *            CIM server implementation. When combined with other property
	 *            restriction filters, all restrictions are accumulated. <br>
	 *            <span style="color: rgb(255,0,0);">Deprecation Note: The CIM
	 *            Operations over HTTP specification [DSP0200] V1.2 has
	 *            deprecated this parameter. It is recommended that CIM client
	 *            applications set <code>pLocalOnly</code> to
	 *            <code>false</code> and not to rely on
	 *            <code>pLocalOnly</code> filtering. To minimize the impact of
	 *            implementing this recommendation in CIM client applications, a
	 *            CIM server may choose to treat the value of
	 *            <code>pLocalOnly</code> as <code>false</code>, regardless
	 *            how it is actually set in an operation. Details: In DSP0200
	 *            V1.0, the pLocalOnly parameter was specified ambiguously. In
	 *            DSP0200 V1.1, the definition of the pLocalOnly parameter was
	 *            modified with the intent to clarify the ambiguity, so that it
	 *            now restricted the set of properties to those of the specified
	 *            class (removing any properties of its superclasses). This
	 *            change introduced backward compatibility problems for CIM
	 *            client applications when working with CIM servers that
	 *            implemented <code>pLocalOnly</code> support with the (quite
	 *            common) interpretation of the V1.0 definition that restricted
	 *            the set of properties to those defined in the CIM class of
	 *            each instance and its superclasses. In DSP0200 V1.2, a CIM
	 *            server must consistently support a single interpretation of
	 *            the <code>pLocalOnly</code> parameter, but which one is left
	 *            open.</span>
	 * @param pIncludeQualifiers
	 *            If false, no qualifiers will be included. If true, all
	 *            qualifiers for each object will be included in the objects
	 *            returned. <br>
	 *            <span style="color: rgb(255,0,0);">Deprecation Note: The CIM
	 *            Operations over HTTP specification [DSP0200] V1.2 has
	 *            deprecated this parameter. The preferred way to retrieve
	 *            qualifier information is to use the class operations (for
	 *            example, <code>getClass</code>) instead, and to set this
	 *            parameter to <code>false</code>.</span>
	 * @param pIncludeClassOrigin
	 *            If true, class origin information will be present in the
	 *            returned objects. Class origin information indicates the class
	 *            in which a CIM property, method, or reference was first
	 *            defined in the class hierarchy. It can be retrieved from the
	 *            Java objects representing these CIM elements by means of the
	 *            <code>getOriginClass()</code> method. If false, class origin
	 *            information will not be present in the returned objects.
	 * @param pPropertyList
	 *            An array of CIM property names that allows to restrict the set
	 *            of properties in the returned objects (classes or instances).
	 *            If null, the set of properties will not be restricted by this
	 *            parameter. If not null, the set of properties will be
	 *            restricted such that any properties missing from this list
	 *            will not be present in the returned objects. In this case, the
	 *            members of the array must define zero or more property names
	 *            of the specified class. This may include inherited property
	 *            names or property names explicitly defined in the specified
	 *            class, but must not include property names defined in
	 *            subclasses of the specified class. Duplicate property names
	 *            are tolerated and the duplicates are ignored. When combined
	 *            with other property restriction filters, all restrictions are
	 *            accumulated.
	 * @return A <code>CIMEnumeration</code> object containing zero or more
	 *         <code>CIMInstance</code> objects.
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_SUPPORTED CIM_ERR_NOT_SUPPORTED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_NAMESPACE CIM_ERR_INVALID_NAMESPACE}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_CLASS CIM_ERR_INVALID_CLASS}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 * @see org.sblim.wbem.cim.CIMProperty#getOriginClass()
	 * @see org.sblim.wbem.cim.CIMMethod#getOriginClass()
	 */
	public synchronized Enumeration enumerateInstances(CIMObjectPath pClassPath,
			boolean pDeepInheritance, boolean pLocalOnly, boolean pIncludeQualifiers,
			boolean pIncludeClassOrigin, java.lang.String[] pPropertyList) throws CIMException {
		Benchmark.startTimer();
		preCheck();
		Enumeration enumeration = iHandle.enumerateInstances(pClassPath, pDeepInheritance,
				pLocalOnly, pIncludeQualifiers, pIncludeClassOrigin, pPropertyList);
		Benchmark.stopTimer();
		return enumeration;
	}

	/**
	 * Enumerates the CIM instances on the target CIM server that are of the
	 * specified class or its subclasses and returns copies of these instances.
	 * 
	 * <p>
	 * This method produces the same results as
	 * <code>enumerateInstances(pClassPath, true,
	 * <span style="color: rgb(255,0,0); font-weight: bold;">true</span>, false, false, null)</code>
	 * </p>
	 * <p>
	 * <span style="color: rgb(255,0,0);">Deprecation Note: The CIM Operations
	 * over HTTP specification [DSP0200] V1.2 has deprecated the
	 * <code>pLocalOnly</code> parameter. It is recommended that CIM client
	 * applications set <code>pLocalOnly</code> to <code>false</code>.
	 * Therefore, it is not recommended that CIM client applications use this
	 * flavor of <code>enumerateInstances</code>.
	 * </p>
	 * 
	 * @param pClassPath
	 *            CIM class path to the CIM class whose instances will be
	 *            enumerated. Instances of its subclasses in the same namespace
	 *            will also be enumerated. Must be an intra-server or
	 *            intra-namespace object path within the target CIM server. In
	 *            the latter case, the default namespace name is added to this
	 *            parameter before being used. The CIM class must exist.
	 * @return A <code>CIMEnumeration</code> object containing zero or more
	 *         <code>CIMInstance</code> objects.
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_SUPPORTED CIM_ERR_NOT_SUPPORTED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_NAMESPACE CIM_ERR_INVALID_NAMESPACE}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_CLASS CIM_ERR_INVALID_CLASS}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 * @see #enumerateInstances(CIMObjectPath, boolean, boolean, boolean,
	 *      boolean, String[])
	 */
	public synchronized Enumeration enumerateInstances(
	// TODO Proposal: Deprecate this method because it forces pLocalOnly to have
			// the deprecated value of true.
			CIMObjectPath pClassPath) throws CIMException {
		return enumerateInstances(pClassPath, true, true, false, false, null);
	}

	/**
	 * Enumerates the CIM instances on the target CIM server that are of the
	 * specified class or its subclasses and returns copies of these instances.
	 * 
	 * <p>
	 * This method produces the same results as
	 * <code>enumerateInstances(pClassPath, pDeepInheritance, false, false, false, null)</code>
	 * </p>
	 * 
	 * @param pClassPath
	 *            CIM class path to the CIM class whose instances will be
	 *            enumerated. Instances of its subclasses in the same namespace
	 *            will also be enumerated. Must be an intra-server or
	 *            intra-namespace object path within the target CIM server. In
	 *            the latter case, the default namespace name is added to this
	 *            parameter before being used. The CIM class must exist.
	 * @param pDeepInheritance
	 *            Allows to restrict the set of properties present in the
	 *            returned instances. If true, the set of properties will not be
	 *            restricted by this parameter. If false, the set of properties
	 *            will be restricted such that any properties in subclasses of
	 *            the specified class are removed. When combined with other
	 *            property restriction filters, all restrictions are
	 *            accumulated. Note: There is an erroneous interpretation of
	 *            this parameter which consists in believing that the parameter
	 *            removes all instances of subclasses from the enumerated set of
	 *            instances. This is wrong as it does not remove such instances,
	 *            instead it removes their subclass properties.
	 * @return A <code>CIMEnumeration</code> object containing zero or more
	 *         <code>CIMInstance</code> objects.
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_SUPPORTED CIM_ERR_NOT_SUPPORTED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_NAMESPACE CIM_ERR_INVALID_NAMESPACE}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_CLASS CIM_ERR_INVALID_CLASS}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 * @see #enumerateInstances(CIMObjectPath, boolean, boolean, boolean,
	 *      boolean, String[])
	 */
	public synchronized Enumeration enumerateInstances(CIMObjectPath pClassPath,
			boolean pDeepInheritance) throws CIMException {
		return enumerateInstances(pClassPath, pDeepInheritance, false, false, false, null);
	}

	/**
	 * Enumerates the CIM instances on the target CIM server that are of the
	 * specified class or its subclasses and returns copies of these instances.
	 * 
	 * <p>
	 * This method produces the same results as
	 * <code>enumerateInstances(pClassPath, pDeepInheritance, pLocalOnly, pIncludeQualifiers, false, null)</code>
	 * </p>
	 * 
	 * @param pClassPath
	 *            CIM class path to the CIM class whose instances will be
	 *            enumerated. Instances of its subclasses in the same namespace
	 *            will also be enumerated. Must be an intra-server or
	 *            intra-namespace object path within the target CIM server. In
	 *            the latter case, the default namespace name is added to this
	 *            parameter before being used. The CIM class must exist.
	 * @param pDeepInheritance
	 *            Allows to restrict the set of properties present in the
	 *            returned instances. If true, the set of properties will not be
	 *            restricted by this parameter. If false, the set of properties
	 *            will be restricted such that any properties in subclasses of
	 *            the specified class are removed. When combined with other
	 *            property restriction filters, all restrictions are
	 *            accumulated. Note: There is an erroneous interpretation of
	 *            this parameter which consists in believing that the parameter
	 *            removes all instances of subclasses from the enumerated set of
	 *            instances. This is wrong as it does not remove such instances,
	 *            instead it removes their subclass properties.
	 * @param pLocalOnly
	 *            Allows to restrict the set of properties present in the
	 *            returned instances. If false, the set of properties will not
	 *            be restricted by this parameter. If true, the set of
	 *            properties will be restricted in a way that dependens on the
	 *            CIM server implementation. When combined with other property
	 *            restriction filters, all restrictions are accumulated. <br>
	 *            <span style="color: rgb(255,0,0);">Deprecation Note: The CIM
	 *            Operations over HTTP specification [DSP0200] V1.2 has
	 *            deprecated this parameter. It is recommended that CIM client
	 *            applications set <code>pLocalOnly</code> to
	 *            <code>false</code> and not to rely on
	 *            <code>pLocalOnly</code> filtering. To minimize the impact of
	 *            implementing this recommendation in CIM client applications, a
	 *            CIM server may choose to treat the value of
	 *            <code>pLocalOnly</code> as <code>false</code>, regardless
	 *            how it is actually set in an operation. Details: In DSP0200
	 *            V1.0, the pLocalOnly parameter was specified ambiguously. In
	 *            DSP0200 V1.1, the definition of the pLocalOnly parameter was
	 *            modified with the intent to clarify the ambiguity, so that it
	 *            now restricted the set of properties to those of the specified
	 *            class (removing any properties of its superclasses). This
	 *            change introduced backward compatibility problems for CIM
	 *            client applications when working with CIM servers that
	 *            implemented <code>pLocalOnly</code> support with the (quite
	 *            common) interpretation of the V1.0 definition that restricted
	 *            the set of properties to those defined in the CIM class of
	 *            each instance and its superclasses. In DSP0200 V1.2, a CIM
	 *            server must consistently support a single interpretation of
	 *            the <code>pLocalOnly</code> parameter, but which one is left
	 *            open.</span>
	 * @param pIncludeQualifiers
	 *            If false, no qualifiers will be included. If true, all
	 *            qualifiers for each object will be included in the objects
	 *            returned. <br>
	 *            <span style="color: rgb(255,0,0);">Deprecation Note: The CIM
	 *            Operations over HTTP specification [DSP0200] V1.2 has
	 *            deprecated this parameter. The preferred way to retrieve
	 *            qualifier information is to use the class operations (for
	 *            example, <code>getClass</code>) instead, and to set this
	 *            parameter to <code>false</code>.</span>
	 * @return A <code>CIMEnumeration</code> object containing zero or more
	 *         <code>CIMInstance</code> objects.
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_SUPPORTED CIM_ERR_NOT_SUPPORTED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_NAMESPACE CIM_ERR_INVALID_NAMESPACE}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_CLASS CIM_ERR_INVALID_CLASS}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 * @see #enumerateInstances(CIMObjectPath, boolean, boolean, boolean,
	 *      boolean, String[])
	 */
	public synchronized Enumeration enumerateInstances(CIMObjectPath pClassPath,
			boolean pDeepInheritance, boolean pLocalOnly, boolean pIncludeQualifiers)
			throws CIMException {
		return enumerateInstances(pClassPath, pDeepInheritance, pLocalOnly, pIncludeQualifiers,
				false, null);
	}

	/**
	 * Enumerates the CIM instances on the target CIM server that are of the
	 * specified class or its subclasses and returns copies of these instances.
	 * 
	 * <p>
	 * This method produces the same results as
	 * <code>enumerateInstances(pClassPath, pDeepInheritance, pLocalOnly, pIncludeQualifiers, pIncludeClassOrigin, null)</code>
	 * </p>
	 * 
	 * @param pClassPath
	 *            CIM class path to the CIM class whose instances will be
	 *            enumerated. Instances of its subclasses in the same namespace
	 *            will also be enumerated. Must be an intra-server or
	 *            intra-namespace object path within the target CIM server. In
	 *            the latter case, the default namespace name is added to this
	 *            parameter before being used. The CIM class must exist.
	 * @param pDeepInheritance
	 *            Allows to restrict the set of properties present in the
	 *            returned instances. If true, the set of properties will not be
	 *            restricted by this parameter. If false, the set of properties
	 *            will be restricted such that any properties in subclasses of
	 *            the specified class are removed. When combined with other
	 *            property restriction filters, all restrictions are
	 *            accumulated. Note: There is an erroneous interpretation of
	 *            this parameter which consists in believing that the parameter
	 *            removes all instances of subclasses from the enumerated set of
	 *            instances. This is wrong as it does not remove such instances,
	 *            instead it removes their subclass properties.
	 * @param pLocalOnly
	 *            Allows to restrict the set of properties present in the
	 *            returned instances. If false, the set of properties will not
	 *            be restricted by this parameter. If true, the set of
	 *            properties will be restricted in a way that dependens on the
	 *            CIM server implementation. When combined with other property
	 *            restriction filters, all restrictions are accumulated. <br>
	 *            <span style="color: rgb(255,0,0);">Deprecation Note: The CIM
	 *            Operations over HTTP specification [DSP0200] V1.2 has
	 *            deprecated this parameter. It is recommended that CIM client
	 *            applications set <code>pLocalOnly</code> to
	 *            <code>false</code> and not to rely on
	 *            <code>pLocalOnly</code> filtering. To minimize the impact of
	 *            implementing this recommendation in CIM client applications, a
	 *            CIM server may choose to treat the value of
	 *            <code>pLocalOnly</code> as <code>false</code>, regardless
	 *            how it is actually set in an operation. Details: In DSP0200
	 *            V1.0, the pLocalOnly parameter was specified ambiguously. In
	 *            DSP0200 V1.1, the definition of the pLocalOnly parameter was
	 *            modified with the intent to clarify the ambiguity, so that it
	 *            now restricted the set of properties to those of the specified
	 *            class (removing any properties of its superclasses). This
	 *            change introduced backward compatibility problems for CIM
	 *            client applications when working with CIM servers that
	 *            implemented <code>pLocalOnly</code> support with the (quite
	 *            common) interpretation of the V1.0 definition that restricted
	 *            the set of properties to those defined in the CIM class of
	 *            each instance and its superclasses. In DSP0200 V1.2, a CIM
	 *            server must consistently support a single interpretation of
	 *            the <code>pLocalOnly</code> parameter, but which one is left
	 *            open.</span>
	 * @param pIncludeQualifiers
	 *            If false, no qualifiers will be included. If true, all
	 *            qualifiers for each object will be included in the objects
	 *            returned. <br>
	 *            <span style="color: rgb(255,0,0);">Deprecation Note: The CIM
	 *            Operations over HTTP specification [DSP0200] V1.2 has
	 *            deprecated this parameter. The preferred way to retrieve
	 *            qualifier information is to use the class operations (for
	 *            example, <code>getClass</code>) instead, and to set this
	 *            parameter to <code>false</code>.</span>
	 * @param pIncludeClassOrigin
	 *            If true, class origin information will be present in the
	 *            returned objects. Class origin information indicates the class
	 *            in which a CIM property, method, or reference was first
	 *            defined in the class hierarchy. It can be retrieved from the
	 *            Java objects representing these CIM elements by means of the
	 *            <code>getOriginClass()</code> method. If false, class origin
	 *            information will not be present in the returned objects.
	 * @return A <code>CIMEnumeration</code> object containing zero or more
	 *         <code>CIMInstance</code> objects.
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_SUPPORTED CIM_ERR_NOT_SUPPORTED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_NAMESPACE CIM_ERR_INVALID_NAMESPACE}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_CLASS CIM_ERR_INVALID_CLASS}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 * @see #enumerateInstances(CIMObjectPath, boolean, boolean, boolean,
	 *      boolean, String[])
	 */
	public synchronized Enumeration enumerateInstances(CIMObjectPath pClassPath,
			boolean pDeepInheritance, boolean pLocalOnly, boolean pIncludeQualifiers,
			boolean pIncludeClassOrigin) throws CIMException {
		return enumerateInstances(pClassPath, pDeepInheritance, pLocalOnly, pIncludeQualifiers,
				pIncludeClassOrigin, null);
	}

	/**
	 * Enumerates the CIM instances on the target CIM server that are of the
	 * specified class or its subclasses and returns CIM instance paths to these
	 * instances.
	 * 
	 * @param pClassPath
	 *            CIM class path to the CIM class whose instances will be
	 *            enumerated. Instances of its subclasses in the same namespace
	 *            will also be enumerated. Must be an intra-server or
	 *            intra-namespace object path within the target CIM server. In
	 *            the latter case, the default namespace name is added to this
	 *            parameter before being used. The CIM class must exist.
	 * @return A <code>CIMEnumeration</code> object containing zero or more
	 *         <code>CIMObjectPath</code> objects referencing the enumerated
	 *         instances using intra-namespace CIM instance paths (model paths).
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_SUPPORTED CIM_ERR_NOT_SUPPORTED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_NAMESPACE CIM_ERR_INVALID_NAMESPACE}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_CLASS CIM_ERR_INVALID_CLASS}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 */
	public synchronized Enumeration enumerateInstanceNames(CIMObjectPath pClassPath)
			throws CIMException {
		Benchmark.startTimer();
		preCheck();
		Enumeration enumeration = iHandle.enumerateInstanceNames(pClassPath);
		Benchmark.stopTimer();
		return enumeration;
	}

	/**
	 * Gets a copy of a CIM instance from the target CIM server.
	 * 
	 * <p>
	 * This method produces the same results as
	 * <code>getInstance(pInstancePath, <span style="color: rgb(255,0,0); font-weight: bold;">true</span>,
	 * false, false, null)</code>
	 * </p>
	 * 
	 * <p>
	 * <span style="color: rgb(255,0,0);">Deprecation Note: The CIM Operations
	 * over HTTP specification [DSP0200] V1.2 has deprecated the
	 * <code>pLocalOnly</code> parameter. It is recommended that CIM client
	 * applications set <code>pLocalOnly</code> to <code>false</code>.
	 * Therefore, it is not recommended that CIM client applications use this
	 * flavor of <code>getInstance</code>.
	 * </p>
	 * 
	 * @param pInstancePath
	 *            CIM instance path to the CIM instance to be returned. Must be
	 *            an intra-server or intra-namespace object path within the
	 *            target CIM server. In the latter case, the default namespace
	 *            name is added to this parameter before being used. The CIM
	 *            instance must exist.
	 * @return A <code>CIMInstance</code> object.
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_NAMESPACE CIM_ERR_INVALID_NAMESPACE}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_CLASS CIM_ERR_INVALID_CLASS}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_FOUND CIM_ERR_NOT_FOUND}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 * @see #getInstance(CIMObjectPath, boolean, boolean, boolean, String[])
	 */
	public synchronized CIMInstance getInstance(
	// TODO Proposal: Deprecate this method because it forces pLocalOnly to have
			// the deprecated value of true.
			CIMObjectPath pInstancePath) throws CIMException {
		return getInstance(pInstancePath, true, false, false, null);
	}

	/**
	 * Gets a copy of a CIM instance.
	 * 
	 * <p>
	 * This method produces the same results as
	 * <code>getInstance(pInstancePath, pLocalOnly, false, false, null)</code>
	 * </p>
	 * 
	 * @param pInstancePath
	 *            CIM instance path to the CIM instance to be returned. Must be
	 *            an intra-server or intra-namespace object path within the
	 *            target CIM server. In the latter case, the default namespace
	 *            name is added to this parameter before being used. The CIM
	 *            instance must exist.
	 * @param pLocalOnly
	 *            Allows to restrict the set of properties present in the
	 *            returned instance. If false, the set of properties will not be
	 *            restricted by this parameter. If true, the set of properties
	 *            will be restricted in a way that dependens on the CIM server
	 *            implementation. When combined with other property restriction
	 *            filters, all restrictions are accumulated. <br>
	 *            <span style="color: rgb(255,0,0);">Deprecation Note: The CIM
	 *            Operations over HTTP specification [DSP0200] V1.2 has
	 *            deprecated this parameter. It is recommended that CIM client
	 *            applications set <code>pLocalOnly</code> to
	 *            <code>false</code> and not to rely on
	 *            <code>pLocalOnly</code> filtering. To minimize the impact of
	 *            implementing this recommendation in CIM client applications, a
	 *            CIM server may choose to treat the value of
	 *            <code>pLocalOnly</code> as <code>false</code>, regardless
	 *            how it is actually set in an operation. Details: In DSP0200
	 *            V1.0, the pLocalOnly parameter was specified ambiguously. In
	 *            DSP0200 V1.1, the definition of the pLocalOnly parameter was
	 *            modified with the intent to clarify the ambiguity, so that it
	 *            now restricted the set of properties to those of the specified
	 *            class (removing any properties of its superclasses). This
	 *            change introduced backward compatibility problems for CIM
	 *            client applications when working with CIM servers that
	 *            implemented <code>pLocalOnly</code> support with the (quite
	 *            common) interpretation of the V1.0 definition that restricted
	 *            the set of properties to those defined in the CIM class of
	 *            each instance and its superclasses. In DSP0200 V1.2, a CIM
	 *            server must consistently support a single interpretation of
	 *            the <code>pLocalOnly</code> parameter, but which one is left
	 *            open.</span>
	 * @return A <code>CIMInstance</code> object.
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_NAMESPACE CIM_ERR_INVALID_NAMESPACE}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_CLASS CIM_ERR_INVALID_CLASS}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_FOUND CIM_ERR_NOT_FOUND}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 * @see #getInstance(CIMObjectPath, boolean, boolean, boolean, String[])
	 */
	public synchronized CIMInstance getInstance(CIMObjectPath pInstancePath, boolean pLocalOnly)
			throws CIMException {
		return getInstance(pInstancePath, pLocalOnly, false, false, null);
	}

	/**
	 * Gets a copy of a CIM instance from the target CIM server.
	 * 
	 * <p>
	 * This method produces the same results as
	 * <code>getInstance(pObjectPath, pLocalOnly, pIncludeQualifiers, false, null)</code>
	 * </p>
	 * 
	 * @param pInstancePath
	 *            CIM instance path to the CIM instance to be returned. Must be
	 *            an intra-server or intra-namespace object path within the
	 *            target CIM server. In the latter case, the default namespace
	 *            name is added to this parameter before being used. The CIM
	 *            instance must exist.
	 * @param pLocalOnly
	 *            Allows to restrict the set of properties present in the
	 *            returned instance. If false, the set of properties will not be
	 *            restricted by this parameter. If true, the set of properties
	 *            will be restricted in a way that dependens on the CIM server
	 *            implementation. When combined with other property restriction
	 *            filters, all restrictions are accumulated. <br>
	 *            <span style="color: rgb(255,0,0);">Deprecation Note: The CIM
	 *            Operations over HTTP specification [DSP0200] V1.2 has
	 *            deprecated this parameter. It is recommended that CIM client
	 *            applications set <code>pLocalOnly</code> to
	 *            <code>false</code> and not to rely on
	 *            <code>pLocalOnly</code> filtering. To minimize the impact of
	 *            implementing this recommendation in CIM client applications, a
	 *            CIM server may choose to treat the value of
	 *            <code>pLocalOnly</code> as <code>false</code>, regardless
	 *            how it is actually set in an operation. Details: In DSP0200
	 *            V1.0, the pLocalOnly parameter was specified ambiguously. In
	 *            DSP0200 V1.1, the definition of the pLocalOnly parameter was
	 *            modified with the intent to clarify the ambiguity, so that it
	 *            now restricted the set of properties to those of the specified
	 *            class (removing any properties of its superclasses). This
	 *            change introduced backward compatibility problems for CIM
	 *            client applications when working with CIM servers that
	 *            implemented <code>pLocalOnly</code> support with the (quite
	 *            common) interpretation of the V1.0 definition that restricted
	 *            the set of properties to those defined in the CIM class of
	 *            each instance and its superclasses. In DSP0200 V1.2, a CIM
	 *            server must consistently support a single interpretation of
	 *            the <code>pLocalOnly</code> parameter, but which one is left
	 *            open.</span>
	 * @param pIncludeQualifiers
	 *            If false, no qualifiers will be included. If true, all
	 *            qualifiers of the instance will be included in the instance
	 *            returned. <br>
	 *            <span style="color: rgb(255,0,0);">Deprecation Note: The CIM
	 *            Operations over HTTP specification [DSP0200] V1.2 has
	 *            deprecated this parameter. The preferred way to retrieve
	 *            qualifier information is to use the class operations (for
	 *            example, <code>getClass</code>) instead, and to set this
	 *            parameter to <code>false</code>.</span>
	 * @return A <code>CIMInstance</code> object.
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_NAMESPACE CIM_ERR_INVALID_NAMESPACE}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_CLASS CIM_ERR_INVALID_CLASS}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_FOUND CIM_ERR_NOT_FOUND}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 * @see #getInstance(CIMObjectPath, boolean, boolean, boolean, String[])
	 */
	public synchronized CIMInstance getInstance(CIMObjectPath pInstancePath, boolean pLocalOnly,
			boolean pIncludeQualifiers) throws CIMException {
		return getInstance(pInstancePath, pLocalOnly, pIncludeQualifiers, false, null);
	}

	/**
	 * Gets a copy of a CIM instance from the target CIM server.
	 * 
	 * <p>
	 * This method produces the same results as
	 * <code>getInstance(pInstancePath, pLocalOnly, pIncludeQualifiers, pIncludeClassOrigin, null)</code>
	 * </p>
	 * 
	 * @param pInstancePath
	 *            CIM instance path to the CIM instance to be returned. Must be
	 *            an intra-server or intra-namespace object path within the
	 *            target CIM server. In the latter case, the default namespace
	 *            name is added to this parameter before being used. The CIM
	 *            instance must exist.
	 * @param pLocalOnly
	 *            Allows to restrict the set of properties present in the
	 *            returned instance. If false, the set of properties will not be
	 *            restricted by this parameter. If true, the set of properties
	 *            will be restricted in a way that dependens on the CIM server
	 *            implementation. When combined with other property restriction
	 *            filters, all restrictions are accumulated. <br>
	 *            <span style="color: rgb(255,0,0);">Deprecation Note: The CIM
	 *            Operations over HTTP specification [DSP0200] V1.2 has
	 *            deprecated this parameter. It is recommended that CIM client
	 *            applications set <code>pLocalOnly</code> to
	 *            <code>false</code> and not to rely on
	 *            <code>pLocalOnly</code> filtering. To minimize the impact of
	 *            implementing this recommendation in CIM client applications, a
	 *            CIM server may choose to treat the value of
	 *            <code>pLocalOnly</code> as <code>false</code>, regardless
	 *            how it is actually set in an operation. Details: In DSP0200
	 *            V1.0, the pLocalOnly parameter was specified ambiguously. In
	 *            DSP0200 V1.1, the definition of the pLocalOnly parameter was
	 *            modified with the intent to clarify the ambiguity, so that it
	 *            now restricted the set of properties to those of the specified
	 *            class (removing any properties of its superclasses). This
	 *            change introduced backward compatibility problems for CIM
	 *            client applications when working with CIM servers that
	 *            implemented <code>pLocalOnly</code> support with the (quite
	 *            common) interpretation of the V1.0 definition that restricted
	 *            the set of properties to those defined in the CIM class of
	 *            each instance and its superclasses. In DSP0200 V1.2, a CIM
	 *            server must consistently support a single interpretation of
	 *            the <code>pLocalOnly</code> parameter, but which one is left
	 *            open.</span>
	 * @param pIncludeQualifiers
	 *            If false, no qualifiers will be included. If true, all
	 *            qualifiers of the instance will be included in the instance
	 *            returned. <br>
	 *            <span style="color: rgb(255,0,0);">Deprecation Note: The CIM
	 *            Operations over HTTP specification [DSP0200] V1.2 has
	 *            deprecated this parameter. The preferred way to retrieve
	 *            qualifier information is to use the class operations (for
	 *            example, <code>getClass</code>) instead, and to set this
	 *            parameter to <code>false</code>.</span>
	 * @param pIncludeClassOrigin
	 *            If true, class origin information will be present in the
	 *            returned instance. Class origin information indicates the
	 *            class in which a CIM property, method, or reference was first
	 *            defined in the class hierarchy. It can be retrieved from the
	 *            Java objects representing these CIM elements by means of the
	 *            <code>getOriginClass()</code> method. If false, class origin
	 *            information will not be present in the returned instance.
	 * @return A <code>CIMInstance</code> object.
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_NAMESPACE CIM_ERR_INVALID_NAMESPACE}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_CLASS CIM_ERR_INVALID_CLASS}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_FOUND CIM_ERR_NOT_FOUND}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 * @see #getInstance(CIMObjectPath, boolean, boolean, boolean, String[])
	 */
	public synchronized CIMInstance getInstance(CIMObjectPath pInstancePath, boolean pLocalOnly,
			boolean pIncludeQualifiers, boolean pIncludeClassOrigin) throws CIMException {
		return getInstance(pInstancePath, pLocalOnly, pIncludeQualifiers, pIncludeClassOrigin, null);
	}

	/**
	 * Gets a copy of a CIM instance from the target CIM server.
	 * 
	 * @param pInstancePath
	 *            CIM instance path to the CIM instance to be returned. Must be
	 *            an intra-server or intra-namespace object path within the
	 *            target CIM server. In the latter case, the default namespace
	 *            name is added to this parameter before being used. The CIM
	 *            instance must exist.
	 * @param pLocalOnly
	 *            Allows to restrict the set of properties present in the
	 *            returned instance. If false, the set of properties will not be
	 *            restricted by this parameter. If true, the set of properties
	 *            will be restricted in a way that dependens on the CIM server
	 *            implementation. When combined with other property restriction
	 *            filters, all restrictions are accumulated. <br>
	 *            <span style="color: rgb(255,0,0);">Deprecation Note: The CIM
	 *            Operations over HTTP specification [DSP0200] V1.2 has
	 *            deprecated this parameter. It is recommended that CIM client
	 *            applications set <code>pLocalOnly</code> to
	 *            <code>false</code> and not to rely on
	 *            <code>pLocalOnly</code> filtering. To minimize the impact of
	 *            implementing this recommendation in CIM client applications, a
	 *            CIM server may choose to treat the value of
	 *            <code>pLocalOnly</code> as <code>false</code>, regardless
	 *            how it is actually set in an operation. Details: In DSP0200
	 *            V1.0, the pLocalOnly parameter was specified ambiguously. In
	 *            DSP0200 V1.1, the definition of the pLocalOnly parameter was
	 *            modified with the intent to clarify the ambiguity, so that it
	 *            now restricted the set of properties to those of the specified
	 *            class (removing any properties of its superclasses). This
	 *            change introduced backward compatibility problems for CIM
	 *            client applications when working with CIM servers that
	 *            implemented <code>pLocalOnly</code> support with the (quite
	 *            common) interpretation of the V1.0 definition that restricted
	 *            the set of properties to those defined in the CIM class of
	 *            each instance and its superclasses. In DSP0200 V1.2, a CIM
	 *            server must consistently support a single interpretation of
	 *            the <code>pLocalOnly</code> parameter, but which one is left
	 *            open.</span>
	 * @param pIncludeQualifiers
	 *            If false, no qualifiers will be included. If true, all
	 *            qualifiers of the instance will be included in the instance
	 *            returned. <br>
	 *            <span style="color: rgb(255,0,0);">Deprecation Note: The CIM
	 *            Operations over HTTP specification [DSP0200] V1.2 has
	 *            deprecated this parameter. The preferred way to retrieve
	 *            qualifier information is to use the class operations (for
	 *            example, <code>getClass</code>) instead, and to set this
	 *            parameter to <code>false</code>.</span>
	 * @param pIncludeClassOrigin
	 *            If true, class origin information will be present in the
	 *            returned instance. Class origin information indicates the
	 *            class in which a CIM property, method, or reference was first
	 *            defined in the class hierarchy. It can be retrieved from the
	 *            Java objects representing these CIM elements by means of the
	 *            <code>getOriginClass()</code> method. If false, class origin
	 *            information will not be present in the returned instance.
	 * @param pPropertyList
	 *            An array of CIM property names that allows to restrict the set
	 *            of properties in the returned instance. If null, the set of
	 *            properties will not be restricted by this parameter. If not
	 *            null, the set of properties will be restricted such that any
	 *            properties missing from this list will not be present in the
	 *            returned instance. In this case, the members of the array must
	 *            define zero or more property names of the specified instance.
	 *            Duplicate property names are tolerated and the duplicates are
	 *            ignored. When combined with other property restriction
	 *            filters, all restrictions are accumulated.
	 * @return A <code>CIMInstance</code> object.
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_NAMESPACE CIM_ERR_INVALID_NAMESPACE}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_CLASS CIM_ERR_INVALID_CLASS}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_FOUND CIM_ERR_NOT_FOUND}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 * @see org.sblim.wbem.cim.CIMProperty#getOriginClass()
	 * @see org.sblim.wbem.cim.CIMMethod#getOriginClass()
	 */
	public synchronized CIMInstance getInstance(CIMObjectPath pInstancePath, boolean pLocalOnly,
			boolean pIncludeQualifiers, boolean pIncludeClassOrigin,
			java.lang.String[] pPropertyList) throws CIMException {
		Benchmark.startTimer();
		preCheck();
		CIMInstance inst = iHandle.getInstance(pInstancePath, pLocalOnly, pIncludeQualifiers,
				pIncludeClassOrigin, pPropertyList);
		Benchmark.stopTimer();
		return inst;
	}

	/**
	 * Creates a CIM class on the target CIM server.
	 * 
	 * @param pClassPath
	 *            CIM class path to the CIM class to be created. Must be an
	 *            intra-server or intra-namespace object path within the target
	 *            CIM server. In the latter case, the default namespace name is
	 *            added to this parameter before being used. The CIM class must
	 *            not exist. Must not be null.
	 * @param pNewClass
	 *            CIM class definition of the new class. Must be a valid class
	 *            definition according to the CIM Infrastructure Specification
	 *            [DSP0004]. Any class origin and propagated information in
	 *            <code>newClass</code> will be ignored (it is reconstructed
	 *            by the CIM server). Must not be null.
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_SUPPORTED CIM_ERR_NOT_SUPPORTED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_NAMESPACE CIM_ERR_INVALID_NAMESPACE}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_ALREADY_EXISTS CIM_ERR_ALREADY_EXISTS}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_SUPERCLASS CIM_ERR_INVALID_SUPERCLASS}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 */
	public synchronized void createClass(CIMObjectPath pClassPath, CIMClass pNewClass)
			throws CIMException {
		Benchmark.startTimer();
		preCheck();
		iHandle.createClass(pClassPath, pNewClass);
		Benchmark.stopTimer();
	}

	/**
	 * Creates a CIM instance in the specified namespace on the target CIM
	 * server.
	 * 
	 * <p>
	 * The namespace must exist on the CIM server. The class of the CIM instance
	 * must exist in the namespace.</p
	 * 
	 * <p>
	 * Dependent on the semantics of the CIM class, this may result in also
	 * creating the real world resource represented by the CIM instance.
	 * </p>
	 * 
	 * @param pNamespacePath
	 *            CIM namespace path of the CIM namespace. Must be an
	 *            intra-server or intra-namespace object path within the target
	 *            CIM server. In the latter case, the default namespace name is
	 *            added to this parameter before being used. Must not be null.
	 * @param pNewInstance
	 *            CIM instance definition. Must be a valid instance definition
	 *            for the underlying CIM class according to the CIM
	 *            Infrastructure Specification [DSP0004]. The new instance
	 *            definition specifies property values as follows: (1) Any
	 *            property specified in <code>newInstance</code> must be a
	 *            property of the class (including of its superclasses). If so,
	 *            its value (including null) is used for the CIM instance to be
	 *            created. (2) For any properties of the class not specified in
	 *            <code>newInstance</code>, the default value defined in the
	 *            class will be used. If no default value has been defined in
	 *            the class, null is used. The CIM provider is free to override
	 *            the rules above, for example in order to create key values
	 *            dynamically. In any case, the new CIM instance in the target
	 *            namespace will have exactly the properties of its class (that
	 *            is, including those of any superclasses). Any class origin and
	 *            propagated information in <code>newInstance</code> will be
	 *            ignored (it is reconstructed by the CIM server). Any
	 *            qualifiers included in <code>newInstance</code> may be
	 *            ignored (it is recommended that CIM servers ignore them). <br>
	 *            <span style="color: rgb(255,0,0);">Deprecation Note: The CIM
	 *            Infrastructure Specification [DSP0004] V2.3 has deprecated
	 *            instance level qualifiers. The preferred way to create or
	 *            modify qualifier information is to use the class operations
	 *            (for example, <code>createClass</code>) instead.</span>
	 *            Must not be null.
	 * @return A <code>CIMObjectPath</code> object referencing the newly
	 *         created CIM instance using an intra-namespace instance path
	 *         (model path). The instance path is returned in case one or more
	 *         of the new keys of the instance are allocated dynamically during
	 *         the creation process rather than used from
	 *         <code>newInstance</code>.
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_SUPPORTED CIM_ERR_NOT_SUPPORTED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_NAMESPACE CIM_ERR_INVALID_NAMESPACE}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_CLASS CIM_ERR_INVALID_CLASS}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_ALREADY_EXISTS CIM_ERR_ALREADY_EXISTS}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 */
	public synchronized CIMObjectPath createInstance(CIMObjectPath pNamespacePath,
			CIMInstance pNewInstance) throws CIMException {
		Benchmark.startTimer();
		preCheck();
		CIMObjectPath newInstancePath = iHandle.createInstance(pNamespacePath, pNewInstance);
		Benchmark.startTimer();
		return newInstancePath;
	}

	/**
	 * Creates or replaces a CIM qualifier declaration on the target CIM server.
	 * <p>
	 * TODO Proposal: Deprecate this method because there is no such CIM
	 * operation, and there is SetQualifierType() with the same semantics.
	 * </p>
	 * 
	 * <p>
	 * If the qualifier declaration does not exist, it will be created. If it
	 * exists, it will be replaced.
	 * </p>
	 * 
	 * <p>
	 * The CIM namespace must exist.
	 * </p>
	 * 
	 * @param pNamespacePath
	 *            CIM namespace path. Must be an intra-server or intra-namespace
	 *            object path within the target CIM server. In the latter case,
	 *            the default namespace name is added to this parameter before
	 *            being used. Must not be null. Also, the class attribute must
	 *            not be null. TODO Proposal: It turns out the code ignores the
	 *            class attribute, but requires it to be non-null. Remove this
	 *            check.
	 * @param pQualifierType
	 *            CIM qualifier declaration. Must be a valid qualifier
	 *            declaration according to the CIM Infrastructure Specification
	 *            [DSP0004].
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_SUPPORTED CIM_ERR_NOT_SUPPORTED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_NAMESPACE CIM_ERR_INVALID_NAMESPACE}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 */
	public synchronized void createQualifierType(CIMObjectPath pNamespacePath,
			CIMQualifierType pQualifierType) throws CIMException {
		Benchmark.startTimer();
		preCheck();
		iHandle.createQualifierType(pNamespacePath, pQualifierType);
		Benchmark.stopTimer();
	}

	/**
	 * Deletes a CIM class on the target CIM server.
	 * 
	 * <p>
	 * This includes deleting all instances of the specified class, all of its
	 * subclasses, and all instances of these subclasses in the same namespace.
	 * </p>
	 * 
	 * @param pClassPath
	 *            CIM class path to the CIM class to be deleted. Must be an
	 *            intra-server or intra-namespace object path within the target
	 *            CIM server. In the latter case, the default namespace name is
	 *            added to this parameter before being used. The CIM class must
	 *            exist.
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_SUPPORTED CIM_ERR_NOT_SUPPORTED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_NAMESPACE CIM_ERR_INVALID_NAMESPACE}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_FOUND CIM_ERR_NOT_FOUND}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_CLASS_HAS_CHILDREN CIM_ERR_CLASS_HAS_CHILDREN}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_CLASS_HAS_INSTANCES CIM_ERR_CLASS_HAS_INSTANCES}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 */
	public synchronized void deleteClass(CIMObjectPath pClassPath) throws CIMException {
		Benchmark.startTimer();
		preCheck();
		iHandle.deleteClass(pClassPath);
		Benchmark.stopTimer();
	}

	/**
	 * Deletes a CIM qualifier declaration on the target CIM server.
	 * 
	 * <p>
	 * Note that a CIM server is not obliged to verify whether the qualifier is
	 * still used in the namespace.
	 * </p>
	 * 
	 * <p>
	 * The CIM qualifier declaration must exist.
	 * </p>
	 * 
	 * @param pQualifierPath
	 *            CIM qualifier path (a class path used to specify the qualifier
	 *            name in the class attribute). Must be an intra-server or
	 *            intra-namespace object path within the target CIM server. In
	 *            the latter case, the default namespace name is added to this
	 *            parameter before being used. Must not be null.
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_SUPPORTED CIM_ERR_NOT_SUPPORTED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_NAMESPACE CIM_ERR_INVALID_NAMESPACE}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_FOUND CIM_ERR_NOT_FOUND}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 */
	public synchronized void deleteQualifierType(CIMObjectPath pQualifierPath) throws CIMException {
		Benchmark.startTimer();
		preCheck();
		iHandle.deleteQualifierType(pQualifierPath);
		Benchmark.stopTimer();
	}

	/**
	 * Gets a copy of a CIM class from the target CIM server.
	 * 
	 * <p>
	 * This method produces the same results as
	 * <code>getClass(pClassPath, true, true, false, null)</code>
	 * </p>
	 * 
	 * @param pClassPath
	 *            CIM class path to the CIM class to be returned. Must be an
	 *            intra-server or intra-namespace object path within the target
	 *            CIM server. In the latter case, the default namespace name is
	 *            added to this parameter before being used. The CIM class must
	 *            exist.
	 * @return A <code>CIMClass</code> object.
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_NAMESPACE CIM_ERR_INVALID_NAMESPACE}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_FOUND CIM_ERR_NOT_FOUND}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 * @see #getClass(CIMObjectPath, boolean, boolean, boolean, String[])
	 */
	public synchronized CIMClass getClass(CIMObjectPath pClassPath) throws CIMException {
		return getClass(pClassPath, true, true, false, null);
	}

	/**
	 * Gets a copy of a CIM class from the target CIM server.
	 * 
	 * <p>
	 * This method produces the same results as
	 * <code>getClass(pClassPath, pLocalOnly, true, false, null)</code>
	 * </p>
	 * 
	 * @param pClassPath
	 *            CIM class path to the CIM class to be returned. Must be an
	 *            intra-server or intra-namespace object path within the target
	 *            CIM server. In the latter case, the default namespace name is
	 *            added to this parameter before being used. The CIM class must
	 *            exist.
	 * @param pLocalOnly
	 *            Allows to restrict the set of properties present in the
	 *            returned class. If false, the set of properties will not be
	 *            restricted by this parameter. If true, the set of properties
	 *            will be restricted to those defined or overridden within the
	 *            definition of the specified class. When combined with other
	 *            property restriction filters, all restrictions are
	 *            accumulated.
	 * @return A <code>CIMClass</code> object.
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_NAMESPACE CIM_ERR_INVALID_NAMESPACE}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_FOUND CIM_ERR_NOT_FOUND}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 * @see #getClass(CIMObjectPath, boolean, boolean, boolean, String[])
	 */
	public synchronized CIMClass getClass(CIMObjectPath pClassPath, boolean pLocalOnly)
			throws CIMException {
		return getClass(pClassPath, pLocalOnly, true, false, null);
	}

	/**
	 * Gets a copy of a CIM class from the target CIM server.
	 * 
	 * <p>
	 * This method produces the same results as
	 * <code>getClass(pClassPath, pLocalOnly, pIncludeQualifiers, false, null)</code>
	 * </p>
	 * 
	 * @param pClassPath
	 *            CIM class path to the CIM class to be returned. Must be an
	 *            intra-server or intra-namespace object path within the target
	 *            CIM server. In the latter case, the default namespace name is
	 *            added to this parameter before being used. The CIM class must
	 *            exist.
	 * @param pLocalOnly
	 *            Allows to restrict the set of properties present in the
	 *            returned class. If false, the set of properties will not be
	 *            restricted by this parameter. If true, the set of properties
	 *            will be restricted to those defined or overridden within the
	 *            definition of the specified class. When combined with other
	 *            property restriction filters, all restrictions are
	 *            accumulated.
	 * @param pIncludeQualifiers
	 *            If false, no CIM qualifiers will be included. If true, all CIM
	 *            qualifiers for the specified class (including qualifiers on
	 *            the class and on any returned properties, methods or method
	 *            parameters) will be included in the returned class.
	 * @return A <code>CIMClass</code> object.
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_NAMESPACE CIM_ERR_INVALID_NAMESPACE}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_FOUND CIM_ERR_NOT_FOUND}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 * @see #getClass(CIMObjectPath, boolean, boolean, boolean, String[])
	 */
	public synchronized CIMClass getClass(CIMObjectPath pClassPath, boolean pLocalOnly,
			boolean pIncludeQualifiers) throws CIMException {
		return getClass(pClassPath, pLocalOnly, pIncludeQualifiers, false, null);
	}

	/**
	 * Gets a copy of a CIM class from the target CIM server.
	 * 
	 * <p>
	 * This method produces the same results as
	 * <code>getClass(pClassPath, pLocalOnly, pIncludeQualifiers, pIncludeClassOrigin, null)</code>
	 * </p>
	 * 
	 * @param pClassPath
	 *            CIM class path to the CIM class to be returned. Must be an
	 *            intra-server or intra-namespace object path within the target
	 *            CIM server. In the latter case, the default namespace name is
	 *            added to this parameter before being used. The CIM class must
	 *            exist.
	 * @param pLocalOnly
	 *            Allows to restrict the set of properties present in the
	 *            returned class. If false, the set of properties will not be
	 *            restricted by this parameter. If true, the set of properties
	 *            will be restricted to those defined or overridden within the
	 *            definition of the specified class. When combined with other
	 *            property restriction filters, all restrictions are
	 *            accumulated.
	 * @param pIncludeQualifiers
	 *            If false, no CIM qualifiers will be included. If true, all CIM
	 *            qualifiers for the specified class (including qualifiers on
	 *            the class and on any returned properties, methods or method
	 *            parameters) will be included in the returned class.
	 * @param pIncludeClassOrigin
	 *            If true, class origin information will be present in the
	 *            returned class. Class origin information indicates the class
	 *            in which a CIM property, method, or reference was first
	 *            defined in the class hierarchy. It can be retrieved from the
	 *            Java objects representing these CIM elements by means of the
	 *            <code>getOriginClass()</code> method. If false, class origin
	 *            information will not be present in the returned class.
	 * @return A <code>CIMClass</code> object.
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_NAMESPACE CIM_ERR_INVALID_NAMESPACE}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_FOUND CIM_ERR_NOT_FOUND}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 * @see #getClass(CIMObjectPath, boolean, boolean, boolean, String[])
	 */
	public synchronized CIMClass getClass(CIMObjectPath pClassPath, boolean pLocalOnly,
			boolean pIncludeQualifiers, boolean pIncludeClassOrigin) throws CIMException {
		Benchmark.startTimer();
		preCheck();
		CIMClass clazz = iHandle.getClass(pClassPath, pLocalOnly, pIncludeQualifiers,
				pIncludeClassOrigin, null);
		Benchmark.startTimer();
		return clazz;
	}

	/**
	 * Gets a copy of a CIM class from the target CIM server.
	 * 
	 * @param pClassPath
	 *            CIM class path to the CIM class to be returned. Must be an
	 *            intra-server or intra-namespace object path within the target
	 *            CIM server. In the latter case, the default namespace name is
	 *            added to this parameter before being used. The CIM class must
	 *            exist.
	 * @param pLocalOnly
	 *            Allows to restrict the set of properties present in the
	 *            returned class. If false, the set of properties will not be
	 *            restricted by this parameter. If true, the set of properties
	 *            will be restricted to those defined or overridden within the
	 *            definition of the specified class. When combined with other
	 *            property restriction filters, all restrictions are
	 *            accumulated.
	 * @param pIncludeQualifiers
	 *            If false, no CIM qualifiers will be included. If true, all CIM
	 *            qualifiers for the specified class (including qualifiers on
	 *            the class and on any returned properties, methods or method
	 *            parameters) will be included in the returned class.
	 * @param pIncludeClassOrigin
	 *            If true, class origin information will be present in the
	 *            returned class. Class origin information indicates the class
	 *            in which a CIM property, method, or reference was first
	 *            defined in the class hierarchy. It can be retrieved from the
	 *            Java objects representing these CIM elements by means of the
	 *            <code>getOriginClass()</code> method. If false, class origin
	 *            information will not be present in the returned class.
	 * @param pPropertyList
	 *            An array of CIM property names that allows to restrict the set
	 *            of properties in the returned class. If null, the set of
	 *            properties will not be restricted by this parameter. If not
	 *            null, the set of properties will be restricted such that any
	 *            properties missing from this list will not be present in the
	 *            returned class. In this case, the members of the array must
	 *            define zero or more property names of the specified class.
	 *            Duplicate property names are tolerated and the duplicates are
	 *            ignored. When combined with other property restriction
	 *            filters, all restrictions are accumulated.
	 * @return A <code>CIMClass</code> object.
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_NAMESPACE CIM_ERR_INVALID_NAMESPACE}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_FOUND CIM_ERR_NOT_FOUND}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 * @see org.sblim.wbem.cim.CIMProperty#getOriginClass()
	 * @see org.sblim.wbem.cim.CIMMethod#getOriginClass()
	 */
	public synchronized CIMClass getClass(CIMObjectPath pClassPath, boolean pLocalOnly,
			boolean pIncludeQualifiers, boolean pIncludeClassOrigin,
			java.lang.String[] pPropertyList) throws CIMException {
		Benchmark.startTimer();
		preCheck();
		CIMClass clazz = iHandle.getClass(pClassPath, pLocalOnly, pIncludeQualifiers,
				pIncludeClassOrigin, pPropertyList);
		Benchmark.startTimer();
		return clazz;
	}

	/**
	 * Gets a copy of a property value of a CIM instance on the target CIM
	 * server.
	 * 
	 * @param pInstancePath
	 *            CIM instance path to the CIM instance containing the property.
	 *            Must be an intra-server or intra-namespace object path within
	 *            the target CIM server. In the latter case, the default
	 *            namespace name is added to this parameter before being used.
	 *            The CIM instance must exist.
	 * @param pPropertyName
	 *            Name of the CIM property to be returned. The property must be
	 *            defined on the class of the CIM instance.
	 * @return A <code>CIMValue</code> object.
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_NAMESPACE CIM_ERR_INVALID_NAMESPACE}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_CLASS CIM_ERR_INVALID_CLASS}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_FOUND CIM_ERR_NOT_FOUND}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NO_SUCH_PROPERTY CIM_ERR_NO_SUCH_PROPERTY}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 */
	public synchronized CIMValue getProperty(CIMObjectPath pInstancePath, String pPropertyName)
			throws CIMException {
		Benchmark.startTimer();
		preCheck();
		CIMValue value = iHandle.getProperty(pInstancePath, pPropertyName);
		Benchmark.startTimer();
		return value;
	}

	/**
	 * Invokes the specified CIM method on a CIM object (instance or class) on
	 * the target CIM server.
	 * 
	 * @param pObjectPath
	 *            CIM object path to the CIM object (instance or class) on which
	 *            the method is to be invoked. Must be an intra-server or
	 *            intra-namespace object path within the target CIM server. In
	 *            the latter case, the default namespace name is added to this
	 *            parameter before being used. The CIM object must exist. If the
	 *            CIM method is static, pObjectPath must reference a CIM class.
	 *            If the CIM method is not static, pObjectPath must reference a
	 *            CIM instance.
	 * @param pMethodName
	 *            Name of the CIM method to be invoked. The method must be
	 *            defined on the class.
	 * @param pInputArguments
	 *            Input parameters of the CIM method, as a vector containing a
	 *            <code>CIMArgument</code> member for each parameter. The set
	 *            of parameters must match the CIM method definition for its
	 *            input parameters. Must not be null. If a method has no input
	 *            parameters, an empty vector must be supplied.
	 * @param pOutputArguments
	 *            Output parameters of the CIM method, as a vector containing a
	 *            <code>CIMArgument</code> member for each parameter. The
	 *            caller supplies an empty vector, and the set of parameters
	 *            returned will match the CIM method definition for its output
	 *            parameters. Must not be null. If a method has no output
	 *            parameters, an empty vector will be returned.
	 * @return A <code>CIMValue</code> object representing the return value of
	 *         the CIM method.
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_SUPPORTED CIM_ERR_NOT_SUPPORTED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_NAMESPACE CIM_ERR_INVALID_NAMESPACE}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_FOUND CIM_ERR_NOT_FOUND}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_METHOD_NOT_FOUND CIM_ERR_METHOD_NOT_FOUND}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_METHOD_NOT_AVAILABLE CIM_ERR_METHOD_NOT_AVAILABLE}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 */
	public synchronized CIMValue invokeMethod(CIMObjectPath pObjectPath, String pMethodName,
			Vector pInputArguments, Vector pOutputArguments) throws CIMException {
		Benchmark.startTimer();
		preCheck();

		if (pInputArguments != null) {
			Iterator iter = pInputArguments.iterator();
			int i = 0;
			while (iter.hasNext()) {
				Object o = iter.next();
				if (!(o instanceof CIMArgument)) throw new IllegalArgumentException(
						"Invalid input arguments at position: " + i
								+ " CIMArgument type expected. \n" + o);
				i++;
			}
		}
		CIMValue value = iHandle.invokeMethod(pObjectPath, pMethodName, pInputArguments,
				pOutputArguments);

		Benchmark.stopTimer();
		return value;
	}

	/**
	 * Invokes the specified CIM method on a CIM object (instance or class) on
	 * the target CIM server.
	 * 
	 * @deprecated It is not recommended to use this method, since the number of
	 *             output parameters may be extended by the CIM provider in the
	 *             future. Instead, use
	 *             {@link #invokeMethod(CIMObjectPath, String, Vector, Vector)}.
	 * @param pObjectPath
	 *            CIM object path to the CIM object (instance or class) on which
	 *            the method is to be invoked. Must be an intra-server or
	 *            intra-namespace object path within the target CIM server. In
	 *            the latter case, the default namespace name is added to this
	 *            parameter before being used. The CIM object must exist. If the
	 *            CIM method is static, pObjectPath must reference a CIM class.
	 *            If the CIM method is not static, pObjectPath must reference a
	 *            CIM instance.
	 * @param pMethodName
	 *            Name of the CIM method to be invoked. The method must be
	 *            defined on the class.
	 * @param pInputArguments
	 *            Input parameters of the CIM method, as a vector containing a
	 *            <code>CIMArgument</code> member for each parameter. The set
	 *            of parameters must match the CIM method definition for its
	 *            input parameters. Must not be null. If a method has no input
	 *            parameters, an empty vector must be supplied.
	 * @param pOutputArguments
	 *            Output parameters of the CIM method, as a vector containing a
	 *            <code>CIMArgument</code> member for each parameter. The
	 *            caller supplies an empty vector, and the set of parameters
	 *            returned will match the CIM method definition for its output
	 *            parameters. Must not be null. If a method has no output
	 *            parameters, an empty vector will be returned.
	 * @return A <code>CIMValue</code> object representing the return value of
	 *         the CIM method.
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_SUPPORTED CIM_ERR_NOT_SUPPORTED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_NAMESPACE CIM_ERR_INVALID_NAMESPACE}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_FOUND CIM_ERR_NOT_FOUND}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_METHOD_NOT_FOUND CIM_ERR_METHOD_NOT_FOUND}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_METHOD_NOT_AVAILABLE CIM_ERR_METHOD_NOT_AVAILABLE}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 */
	public synchronized CIMValue invokeMethod(CIMObjectPath pObjectPath, String pMethodName,
			CIMArgument[] pInputArguments, CIMArgument[] pOutputArguments) throws CIMException {

		Vector out = new Vector();
		Vector in = new Vector();
		if (pInputArguments != null) {
			in.addAll(Arrays.asList(pInputArguments));
		}
		CIMValue value = invokeMethod(pObjectPath, pMethodName, in, out);

		if (pOutputArguments != null && pOutputArguments.length > 0) {
			int totalArgs = pOutputArguments.length;
			if (out.size() < totalArgs) totalArgs = out.size();

			for (int i = 0; i < totalArgs; i++) {
				pOutputArguments[i] = (CIMArgument) out.elementAt(i);
			}
		}
		return value;
	}

	/**
	 * Adds the specified CIMListener to start receiving indications from the
	 * CIM server.
	 * 
	 * <p>
	 * The CIMListener must differentiate itself from other CIMListener
	 * instances through the hashCode().
	 * </p>
	 * 
	 * TODO Clarify: CIMListener does not have a hashcode() method.
	 * 
	 * @param pListener
	 *            CIMListener to be added. Must not be null.
	 * @throws IllegalArgumentException
	 *             if the listener is null.
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 */
	public synchronized void addCIMListener(CIMListener pListener) throws CIMException {
		Benchmark.startTimer();
		preCheck();
		iHandle.addCIMListener(pListener);
		Benchmark.stopTimer();
	}

	/**
	 * Removes the specified CIMListener, to stop receiving indications from the
	 * CIM server.
	 * 
	 * @param pListener
	 *            CIMListener to be added. Must not be null.
	 * @throws IllegalArgumentException
	 *             if the listener is null.
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 */
	public synchronized void removeCIMListener(CIMListener pListener) throws CIMException {
		Benchmark.startTimer();
		preCheck();
		iHandle.removeCIMListener(pListener);
		Benchmark.startTimer();
	}

	/**
	 * This methods return an instance of CIM_IndicationDestination, since CIM
	 * over HTTP is used as transport an instance of
	 * CIM_ListenerDestinationCIMXML is returned.
	 * <p>
	 * TODO Docu: Improve documentation.
	 * </p>
	 * 
	 * @param pListener
	 *            CIMListener for which the CIM_IndicationHandler subclass is
	 *            being returned, which is used to deliver indications to the
	 *            specific listener. Unique listeners should return unique
	 *            values in their hashCode methods in order to be differenciate
	 *            themself from other CIMListeners.
	 * @return CIMInstance an instance of the CIM_IndicationDestination
	 * @throws IllegalArgumentException
	 *             if the listener is null.
	 * @throws CIMException
	 *             if unsuccessful, one of the error codes must be returned.
	 *             CIM_ERR_ACCESS_DENIED CIM_ERR_INVALID_PARAMETER
	 *             CIM_ERR_FAILED (some unspecified error) or the CIMListener
	 *             server fails to load
	 * 
	 */
	public synchronized CIMInstance getIndicationListener(CIMListener pListener)
			throws CIMException {
		Benchmark.startTimer();
		preCheck();
		CIMInstance instance = iHandle.getIndicationListener(pListener);
		Benchmark.startTimer();
		return instance;
	}

	/**
	 * This methods return an instance of CIM_IndicationHandler, since CIM over
	 * HTTP is used as transport an instance of CIM_IndicationHandlerCIMXML is
	 * returned.
	 * <p>
	 * TODO Docu: Improve documentation.
	 * </p>
	 * 
	 * @param pListener
	 *            CIMListener for which the CIM)IndicationHandler subclass is
	 *            being returned, which is used to deliver indications to the
	 *            specific listener. Unique listeners should return unique
	 *            values in their hashCode methods in order to be differenciate
	 *            themself from other CIMListeners.
	 * @return CIMInstance an CIM Instance of the indication handler.
	 * @throws IllegalArgumentException
	 *             if the listener is null.
	 * @throws CIMException
	 *             if unsuccessful, one of the error codes must be returned.
	 *             CIM_ERR_ACCESS_DENIED CIM_ERR_INVALID_PARAMETER
	 *             CIM_ERR_FAILED (some unspecified error) or the CIMListener
	 *             server fails to load
	 * 
	 */
	public synchronized CIMInstance getIndicationHandler(CIMListener pListener) throws CIMException {
		Benchmark.startTimer();
		preCheck();
		CIMInstance inst = iHandle.getIndicationHandler(pListener);
		Benchmark.stopTimer();
		return inst;
	}

	/**
	 * Closes the connection to the CIM server.
	 * 
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 */
	public synchronized void close() throws CIMException {
		close(false);
	}

	/**
	 * Closes the connection to the CIM server.
	 * 
	 * @param pKeepEnumerations
	 *            If <code>true</code> all open enumeration will be preserved,
	 *            if <code>false</code> all enumerations are invalidated
	 * 
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 */
	public synchronized void close(boolean pKeepEnumerations) throws CIMException {
		if (iHandle != null) {
			iHandle.close(pKeepEnumerations);
			iHandle = null;
		} else throw new CIMException(CIMException.CIM_ERR_FAILED);
	}

	/**
	 * Creates a CIM namespace on the target CIM server.
	 * 
	 * <p>
	 * The namespace must not exist on the target CIM server.
	 * </p>
	 * 
	 * @param pNameSpace
	 *            CIM namespace to be created. Must not be null. The namespace
	 *            attribute must be a valid namespace name according to the CIM
	 *            Infrastructure Specification [DSP0004]. All other attributes
	 *            are ignored.
	 * @throws IllegalArgumentException
	 *             if a null namespace is passed as an argument
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_SUPPORTED CIM_ERR_NOT_SUPPORTED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_ALREADY_EXISTS CIM_ERR_ALREADY_EXISTS}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 */
	public synchronized void createNameSpace(CIMNameSpace pNameSpace) throws CIMException {
		preCheck();
		iHandle.createNameSpace(pNameSpace);
	}

	/**
	 * Deletes a CIM namespace from the target CIM server.
	 * 
	 * @param pNameSpace
	 *            CIM namespace to be deleted. The namespace attribute must be a
	 *            valid namespace name according to the CIM Infrastructure
	 *            Specification [DSP0004]. All other attributes are ignored.
	 *            Must not be null. The namespace must exist on the target CIM
	 *            server and must not contain any elements.
	 * @throws IllegalArgumentException
	 *             if a null namespace is passed as an argument
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_SUPPORTED CIM_ERR_NOT_SUPPORTED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_ALREADY_EXISTS CIM_ERR_ALREADY_EXISTS}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 */
	public synchronized void deleteNameSpace(CIMNameSpace pNameSpace) throws CIMException {
		preCheck();
		iHandle.deleteNameSpace(pNameSpace);
	}

	/**
	 * Enumerates the CIM qualifier declarations in a namespace on the target
	 * CIM server.
	 * 
	 * <p>
	 * The namespace must exist.
	 * </p>
	 * 
	 * @param pNamespacePath
	 *            CIM namespace path. Must be an intra-server or intra-namespace
	 *            object path within the target CIM server. In the latter case,
	 *            the default namespace name is added to this parameter before
	 *            being used. Must not be null.
	 * @return A <code>CIMEnumeration</code> object containing zero or more
	 *         <code>CIMQualifierType</code> objects.
	 * @throws IllegalArgumentException
	 *             if a null namespace is passed as an argument
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_SUPPORTED CIM_ERR_NOT_SUPPORTED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_NAMESPACE CIM_ERR_INVALID_NAMESPACE}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 */
	public synchronized Enumeration enumQualifierTypes(CIMObjectPath pNamespacePath)
			throws CIMException {
		Benchmark.startTimer();
		preCheck();
		Enumeration enumeration = iHandle.enumQualifierTypes(pNamespacePath);
		Benchmark.stopTimer();
		return enumeration;
	}

	/**
	 * Enumerates the CIM classes on the target CIM server that are subclasses
	 * of the specified class, and returns copies of these classes.
	 * 
	 * <p>
	 * This method produces the same results as
	 * <code>enumerateClasses(pClassPath, false, true, true, false)</code>
	 * </p>
	 * 
	 * @param pClassPath
	 *            CIM class path to the CIM class whose subclasses will be
	 *            enumerated. Must be an intra-server or intra-namespace object
	 *            path within the target CIM server. In the latter case, the
	 *            default namespace name is added to this parameter before being
	 *            used. If the classname attribute is an empty string,
	 *            enumeration starts at the top of the class hierarchy, so that
	 *            the immediate subclasses are the top level classes of the
	 *            hierarchy. If the classname attribute is a valid class name,
	 *            the class must exist in the namespace and enumeration starts
	 *            at that class.
	 * @return A <code>CIMEnumeration</code> object containing zero or more
	 *         <code>CIMClass</code> objects.
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_SUPPORTED CIM_ERR_NOT_SUPPORTED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_NAMESPACE CIM_ERR_INVALID_NAMESPACE}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_CLASS CIM_ERR_INVALID_CLASS}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 * @see #enumerateClasses(CIMObjectPath, boolean, boolean, boolean, boolean)
	 */
	public synchronized Enumeration enumClass(CIMObjectPath pClassPath) throws CIMException {
		return enumerateClasses(pClassPath, false, true, true, false);
	}

	/**
	 * Enumerates the CIM classes on the target CIM server that are subclasses
	 * of the specified class, and returns copies of these classes.
	 * 
	 * <p>
	 * This method produces the same results as
	 * <code>enumerateClasses(pClassPath, pDeepInheritance, true, true, false)</code>
	 * </p>
	 * 
	 * @param pClassPath
	 *            CIM class path to the CIM class whose subclasses will be
	 *            enumerated. Must be an intra-server or intra-namespace object
	 *            path within the target CIM server. In the latter case, the
	 *            default namespace name is added to this parameter before being
	 *            used. If the classname attribute is an empty string,
	 *            enumeration starts at the top of the class hierarchy, so that
	 *            the immediate subclasses are the top level classes of the
	 *            hierarchy. If the classname attribute is a valid class name,
	 *            the class must exist in the namespace and enumeration starts
	 *            at that class.
	 * @param pDeepInheritance
	 *            Controls the set of returned subclasses. If true, all
	 *            subclasses of the specified class will be returned, down to
	 *            the leaves of the class hierarchy. If false, only immediate
	 *            subclasses of the specified class will be returned. Note that
	 *            this definition of <code>pDeepInheritance</code> applies
	 *            only to the methods for class enumeration and is different
	 *            from its usage in the methods for instance enumeration.
	 * @return A <code>CIMEnumeration</code> object containing zero or more
	 *         <code>CIMClass</code> objects.
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_SUPPORTED CIM_ERR_NOT_SUPPORTED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_NAMESPACE CIM_ERR_INVALID_NAMESPACE}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_CLASS CIM_ERR_INVALID_CLASS}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 * @see #enumerateClasses(CIMObjectPath, boolean, boolean, boolean, boolean)
	 */
	public synchronized Enumeration enumClass(CIMObjectPath pClassPath, boolean pDeepInheritance)
			throws CIMException {
		return enumerateClasses(pClassPath, pDeepInheritance, true, true, false);
	}

	/**
	 * Enumerates the CIM classes on the target CIM server that are subclasses
	 * of the specified class, and returns copies of these classes.
	 * 
	 * <p>
	 * This method produces the same results as
	 * <code>enumerateClasses(pClassPath, pDeepInheritance, pLocalOnly, true, false)</code>
	 * </p>
	 * 
	 * @param pClassPath
	 *            CIM class path to the CIM class whose subclasses will be
	 *            enumerated. Must be an intra-server or intra-namespace object
	 *            path within the target CIM server. In the latter case, the
	 *            default namespace name is added to this parameter before being
	 *            used. If the classname attribute is an empty string,
	 *            enumeration starts at the top of the class hierarchy, so that
	 *            the immediate subclasses are the top level classes of the
	 *            hierarchy. If the classname attribute is a valid class name,
	 *            the class must exist in the namespace and enumeration starts
	 *            at that class.
	 * @param pDeepInheritance
	 *            Controls the set of returned subclasses. If true, all
	 *            subclasses of the specified class will be returned, down to
	 *            the leaves of the class hierarchy. If false, only immediate
	 *            subclasses of the specified class will be returned. Note that
	 *            this definition of <code>pDeepInheritance</code> applies
	 *            only to the methods for class enumeration and is different
	 *            from its usage in the methods for instance enumeration.
	 * @param pLocalOnly
	 *            Allows to restrict the set of properties present in the
	 *            returned classes. If false, the set of properties will not be
	 *            restricted by this parameter. If true, the set of properties
	 *            will be restricted to those defined or overridden within the
	 *            definition of the specified class.
	 * @return A <code>CIMEnumeration</code> object containing zero or more
	 *         <code>CIMClass</code> objects.
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_SUPPORTED CIM_ERR_NOT_SUPPORTED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_NAMESPACE CIM_ERR_INVALID_NAMESPACE}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_CLASS CIM_ERR_INVALID_CLASS}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 * @see #enumerateClasses(CIMObjectPath, boolean, boolean, boolean, boolean)
	 */
	public synchronized Enumeration enumClass(CIMObjectPath pClassPath, boolean pDeepInheritance,
			boolean pLocalOnly) throws CIMException {
		return enumerateClasses(pClassPath, pDeepInheritance, pLocalOnly, true, false);
	}

	/**
	 * Enumerates the CIM classes on the target CIM server that are subclasses
	 * of the specified class, and returns copies of these classes.
	 * 
	 * <p>
	 * This method produces the same results as
	 * <code>enumerateClasses(null, false, true, true, false)</code>
	 * </p>
	 * 
	 * @return A <code>CIMEnumeration</code> object containing zero or more
	 *         <code>CIMClass</code> objects.
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_SUPPORTED CIM_ERR_NOT_SUPPORTED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_NAMESPACE CIM_ERR_INVALID_NAMESPACE}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_CLASS CIM_ERR_INVALID_CLASS}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 * @see #enumerateClasses(CIMObjectPath, boolean, boolean, boolean, boolean)
	 */
	public synchronized Enumeration enumerateClasses() throws CIMException {
		return enumerateClasses(null, false, true, true, false);
	}

	/**
	 * Enumerates the CIM classes on the target CIM server that are subclasses
	 * of the specified class, and returns copies of these classes.
	 * 
	 * <p>
	 * This method produces the same results as
	 * <code>enumerateClasses(pClassPath, false, true, true, false)</code>
	 * </p>
	 * 
	 * @param pClassPath
	 *            CIM class path to the CIM class whose subclasses will be
	 *            enumerated. Must be an intra-server or intra-namespace object
	 *            path within the target CIM server. In the latter case, the
	 *            default namespace name is added to this parameter before being
	 *            used. If the classname attribute is an empty string,
	 *            enumeration starts at the top of the class hierarchy, so that
	 *            the immediate subclasses are the top level classes of the
	 *            hierarchy. If the classname attribute is a valid class name,
	 *            the class must exist in the namespace and enumeration starts
	 *            at that class.
	 * @return A <code>CIMEnumeration</code> object containing zero or more
	 *         <code>CIMClass</code> objects.
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_SUPPORTED CIM_ERR_NOT_SUPPORTED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_NAMESPACE CIM_ERR_INVALID_NAMESPACE}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_CLASS CIM_ERR_INVALID_CLASS}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 * @see #enumerateClasses(CIMObjectPath, boolean, boolean, boolean, boolean)
	 */
	public synchronized Enumeration enumerateClasses(CIMObjectPath pClassPath) throws CIMException {
		return enumerateClasses(pClassPath, false, true, true, false);
	}

	/**
	 * Enumerates the CIM classes on the target CIM server that are subclasses
	 * of the specified class, and returns copies of these classes.
	 * 
	 * <p>
	 * This method produces the same results as
	 * <code>enumerateClasses(pClassPath, pDeepInheritance, true, true, false)</code>
	 * </p>
	 * 
	 * @param pClassPath
	 *            CIM class path to the CIM class whose subclasses will be
	 *            enumerated. Must be an intra-server or intra-namespace object
	 *            path within the target CIM server. In the latter case, the
	 *            default namespace name is added to this parameter before being
	 *            used. If the classname attribute is an empty string,
	 *            enumeration starts at the top of the class hierarchy, so that
	 *            the immediate subclasses are the top level classes of the
	 *            hierarchy. If the classname attribute is a valid class name,
	 *            the class must exist in the namespace and enumeration starts
	 *            at that class.
	 * @param pDeepInheritance
	 *            Controls the set of returned subclasses. If true, all
	 *            subclasses of the specified class will be returned, down to
	 *            the leaves of the class hierarchy. If false, only immediate
	 *            subclasses of the specified class will be returned. Note that
	 *            this definition of <code>pDeepInheritance</code> applies
	 *            only to the methods for class enumeration and is different
	 *            from its usage in the methods for instance enumeration.
	 * @return A <code>CIMEnumeration</code> object containing zero or more
	 *         <code>CIMClass</code> objects.
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_SUPPORTED CIM_ERR_NOT_SUPPORTED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_NAMESPACE CIM_ERR_INVALID_NAMESPACE}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_CLASS CIM_ERR_INVALID_CLASS}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 * @see #enumerateClasses(CIMObjectPath, boolean, boolean, boolean, boolean)
	 */
	public synchronized Enumeration enumerateClasses(CIMObjectPath pClassPath,
			boolean pDeepInheritance) throws CIMException {
		return enumerateClasses(pClassPath, pDeepInheritance, true, true, false);
	}

	/**
	 * Enumerates the CIM classes on the target CIM server that are subclasses
	 * of the specified class, and returns copies of these classes.
	 * 
	 * <p>
	 * This method produces the same results as
	 * <code>enumerateClasses(pClassPath, pDeepInheritance, pLocalOnly, true, false)</code>
	 * </p>
	 * 
	 * @param pClassPath
	 *            CIM class path to the CIM class whose subclasses will be
	 *            enumerated. Must be an intra-server or intra-namespace object
	 *            path within the target CIM server. In the latter case, the
	 *            default namespace name is added to this parameter before being
	 *            used. If the classname attribute is an empty string,
	 *            enumeration starts at the top of the class hierarchy, so that
	 *            the immediate subclasses are the top level classes of the
	 *            hierarchy. If the classname attribute is a valid class name,
	 *            the class must exist in the namespace and enumeration starts
	 *            at that class.
	 * @param pDeepInheritance
	 *            Controls the set of returned subclasses. If true, all
	 *            subclasses of the specified class will be returned, down to
	 *            the leaves of the class hierarchy. If false, only immediate
	 *            subclasses of the specified class will be returned. Note that
	 *            this definition of <code>pDeepInheritance</code> applies
	 *            only to the methods for class enumeration and is different
	 *            from its usage in the methods for instance enumeration.
	 * @param pLocalOnly
	 *            Allows to restrict the set of properties present in the
	 *            returned classes. If false, the set of properties will not be
	 *            restricted by this parameter. If true, the set of properties
	 *            will be restricted to those defined or overridden within the
	 *            definition of the specified class.
	 * @return A <code>CIMEnumeration</code> object containing zero or more
	 *         <code>CIMClass</code> objects.
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_SUPPORTED CIM_ERR_NOT_SUPPORTED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_NAMESPACE CIM_ERR_INVALID_NAMESPACE}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_CLASS CIM_ERR_INVALID_CLASS}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 * @see #enumerateClasses(CIMObjectPath, boolean, boolean, boolean, boolean)
	 */
	public synchronized Enumeration enumerateClasses(CIMObjectPath pClassPath,
			boolean pDeepInheritance, boolean pLocalOnly) throws CIMException {
		return enumerateClasses(pClassPath, pDeepInheritance, pLocalOnly, true, false);
	}

	/**
	 * Enumerates the CIM classes on the target CIM server that are subclasses
	 * of the specified class, and returns copies of these classes.
	 * 
	 * <p>
	 * This method produces the same results as
	 * <code>enumerateClasses(pClassPath, pDeepInheritance, pLocalOnly, pIncludeQualifiers, false)</code>
	 * </p>
	 * 
	 * @param pClassPath
	 *            CIM class path to the CIM class whose subclasses will be
	 *            enumerated. Must be an intra-server or intra-namespace object
	 *            path within the target CIM server. In the latter case, the
	 *            default namespace name is added to this parameter before being
	 *            used. If the classname attribute is an empty string,
	 *            enumeration starts at the top of the class hierarchy, so that
	 *            the immediate subclasses are the top level classes of the
	 *            hierarchy. If the classname attribute is a valid class name,
	 *            the class must exist in the namespace and enumeration starts
	 *            at that class.
	 * @param pDeepInheritance
	 *            Controls the set of returned subclasses. If true, all
	 *            subclasses of the specified class will be returned, down to
	 *            the leaves of the class hierarchy. If false, only immediate
	 *            subclasses of the specified class will be returned. Note that
	 *            this definition of <code>pDeepInheritance</code> applies
	 *            only to the methods for class enumeration and is different
	 *            from its usage in the methods for instance enumeration.
	 * @param pLocalOnly
	 *            Allows to restrict the set of properties present in the
	 *            returned classes. If false, the set of properties will not be
	 *            restricted by this parameter. If true, the set of properties
	 *            will be restricted to those defined or overridden within the
	 *            definition of the specified class.
	 * @param pIncludeQualifiers
	 *            If false, no CIM qualifiers will be included. If true, all CIM
	 *            qualifiers for the specified class (including qualifiers on
	 *            the class and on any returned properties, methods or method
	 *            parameters) will be included in the returned classes.
	 * @return A <code>CIMEnumeration</code> object containing zero or more
	 *         <code>CIMClass</code> objects.
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_SUPPORTED CIM_ERR_NOT_SUPPORTED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_NAMESPACE CIM_ERR_INVALID_NAMESPACE}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_CLASS CIM_ERR_INVALID_CLASS}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 * @see #enumerateClasses(CIMObjectPath, boolean, boolean, boolean, boolean)
	 */
	public synchronized Enumeration enumerateClasses(CIMObjectPath pClassPath,
			boolean pDeepInheritance, boolean pLocalOnly, boolean pIncludeQualifiers)
			throws CIMException {
		return enumerateClasses(pClassPath, pDeepInheritance, pLocalOnly, pIncludeQualifiers, false);
	}

	/**
	 * Enumerates the CIM classes on the target CIM server that are subclasses
	 * of the specified class, and returns copies of these classes.
	 * 
	 * @param pClassPath
	 *            CIM class path to the CIM class whose subclasses will be
	 *            enumerated. Must be an intra-server or intra-namespace object
	 *            path within the target CIM server. In the latter case, the
	 *            default namespace name is added to this parameter before being
	 *            used. If the classname attribute is an empty string,
	 *            enumeration starts at the top of the class hierarchy, so that
	 *            the immediate subclasses are the top level classes of the
	 *            hierarchy. If the classname attribute is a valid class name,
	 *            the class must exist in the namespace and enumeration starts
	 *            at that class.
	 * @param pDeepInheritance
	 *            Controls the set of returned subclasses. If true, all
	 *            subclasses of the specified class will be returned, down to
	 *            the leaves of the class hierarchy. If false, only immediate
	 *            subclasses of the specified class will be returned. Note that
	 *            this definition of <code>pDeepInheritance</code> applies
	 *            only to the methods for class enumeration and is different
	 *            from its usage in the methods for instance enumeration.
	 * @param pLocalOnly
	 *            Allows to restrict the set of properties present in the
	 *            returned classes. If false, the set of properties will not be
	 *            restricted by this parameter. If true, the set of properties
	 *            will be restricted to those defined or overridden within the
	 *            definition of the specified class.
	 * @param pIncludeQualifiers
	 *            If false, no CIM qualifiers will be included. If true, all CIM
	 *            qualifiers for the specified class (including qualifiers on
	 *            the class and on any returned properties, methods or method
	 *            parameters) will be included in the returned classes.
	 * @param pIncludeClassOrigin
	 *            If true, class origin information will be present in the
	 *            returned classes. Class origin information indicates the class
	 *            in which a CIM property, method, or reference was first
	 *            defined in the class hierarchy. It can be retrieved from the
	 *            Java objects representing these CIM elements by means of the
	 *            <code>getOriginClass()</code> method. If false, class origin
	 *            information will not be present in the returned classes.
	 * @return A <code>CIMEnumeration</code> object containing zero or more
	 *         <code>CIMClass</code> objects.
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_SUPPORTED CIM_ERR_NOT_SUPPORTED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_NAMESPACE CIM_ERR_INVALID_NAMESPACE}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_CLASS CIM_ERR_INVALID_CLASS}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 * @see org.sblim.wbem.cim.CIMProperty#getOriginClass()
	 * @see org.sblim.wbem.cim.CIMMethod#getOriginClass()
	 */
	public synchronized Enumeration enumerateClasses(CIMObjectPath pClassPath,
			boolean pDeepInheritance, boolean pLocalOnly, boolean pIncludeQualifiers,
			boolean pIncludeClassOrigin) throws CIMException {
		Benchmark.startTimer();
		preCheck();
		if (pClassPath == null) pClassPath = new CIMObjectPath();
		Enumeration enumeration = iHandle.enumerateClasses(pClassPath, pDeepInheritance,
				pLocalOnly, pIncludeQualifiers, pIncludeClassOrigin);
		Benchmark.stopTimer();
		return enumeration;
	}

	/**
	 * Enumerates the CIM classes on the target CIM server that are subclasses
	 * of the specified class, and returns CIM class paths to these classes.
	 * 
	 * <p>
	 * This method produces the same results as
	 * <code>enumerateClassNames(null, false)</code>
	 * </p>
	 * 
	 * @return A <code>CIMEnumeration</code> object containing zero or more
	 *         <code>CIMObjectPath</code> objects referencing the enumerated
	 *         classes using intra-namespace CIM class paths (class names).
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_SUPPORTED CIM_ERR_NOT_SUPPORTED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_NAMESPACE CIM_ERR_INVALID_NAMESPACE}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_CLASS CIM_ERR_INVALID_CLASS}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 * @see #enumerateClassNames(CIMObjectPath, boolean)
	 */
	public synchronized Enumeration enumerateClassNames() throws CIMException {
		return enumerateClassNames(null, false);
	}

	/**
	 * Enumerates the CIM classes on the target CIM server that are subclasses
	 * of the specified class, and returns CIM class paths to these classes.
	 * 
	 * <p>
	 * This method produces the same results as
	 * <code>enumerateClassNames(pClassPath, false)</code>
	 * </p>
	 * 
	 * @param pClassPath
	 *            CIM class path to the CIM class whose subclasses will be
	 *            enumerated. Must be an intra-server or intra-namespace object
	 *            path within the target CIM server. In the latter case, the
	 *            default namespace name is added to this parameter before being
	 *            used. If the classname attribute is an empty string,
	 *            enumeration starts at the top of the class hierarchy, so that
	 *            the immediate subclasses are the top level classes of the
	 *            hierarchy. If the classname attribute is a valid class name,
	 *            the class must exist in the namespace and enumeration starts
	 *            at that class.
	 * @return A <code>CIMEnumeration</code> object containing zero or more
	 *         <code>CIMObjectPath</code> objects referencing the enumerated
	 *         classes using intra-namespace CIM class paths (class names).
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_SUPPORTED CIM_ERR_NOT_SUPPORTED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_NAMESPACE CIM_ERR_INVALID_NAMESPACE}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_CLASS CIM_ERR_INVALID_CLASS}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 * @see #enumerateClassNames(CIMObjectPath, boolean)
	 */
	public synchronized Enumeration enumerateClassNames(CIMObjectPath pClassPath)
			throws CIMException {
		return enumerateClassNames(pClassPath, false);
	}

	/**
	 * Enumerates the CIM classes on the target CIM server that are subclasses
	 * of the specified class, and returns CIM class paths to these classes.
	 * 
	 * @param pClassPath
	 *            CIM class path to the CIM class whose subclasses will be
	 *            enumerated. Must be an intra-server or intra-namespace object
	 *            path within the target CIM server. In the latter case, the
	 *            default namespace name is added to this parameter before being
	 *            used. If the classname attribute is an empty string,
	 *            enumeration starts at the top of the class hierarchy, so that
	 *            the immediate subclasses are the top level classes of the
	 *            hierarchy. If the classname attribute is a valid class name,
	 *            the class must exist in the namespace and enumeration starts
	 *            at that class.
	 * @param pDeepInheritance
	 *            Controls the set of returned subclasses. If true, all
	 *            subclasses of the specified class will be returned, down to
	 *            the leaves of the class hierarchy. If false, only immediate
	 *            subclasses of the specified class will be returned. Note that
	 *            this definition of <code>pDeepInheritance</code> applies
	 *            only to the methods for class enumeration and is different
	 *            from its usage in the methods for instance enumeration.
	 * @return A <code>CIMEnumeration</code> object containing zero or more
	 *         <code>CIMObjectPath</code> objects referencing the enumerated
	 *         classes using intra-namespace CIM class paths (class names).
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_SUPPORTED CIM_ERR_NOT_SUPPORTED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_NAMESPACE CIM_ERR_INVALID_NAMESPACE}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_CLASS CIM_ERR_INVALID_CLASS}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 */
	public synchronized Enumeration enumerateClassNames(CIMObjectPath pClassPath,
			boolean pDeepInheritance) throws CIMException {
		Benchmark.startTimer();
		preCheck();
		if (pClassPath == null) pClassPath = new CIMObjectPath();
		Enumeration enumeration = iHandle.enumerateClassNames(pClassPath, pDeepInheritance);
		Benchmark.stopTimer();
		return enumeration;
	}

	/**
	 * Enumerates the CIM namespaces on the target CIM server, and returns CIM
	 * instance paths to the CIM_Namespace instances representing them.
	 * 
	 * TODO Proposal: Deprecate this method and define a better one, since it is
	 * ill defined: (1) caller has to know the Interop namespace name (2) only
	 * instance paths are returned (3) the "deep" argument is not used
	 * 
	 * @param pNamespacePath
	 *            CIM namespace path of the namespace used to enumerate the
	 *            CIM_NameSpace instances. The Interop namespace of a CIM server
	 *            is supposed to support this. The name of the Interop namespace
	 *            can be obtained by SLP discovery. Must be an intra-server or
	 *            intra-namespace object path within the target CIM server. In
	 *            the latter case, the default namespace name is added to this
	 *            parameter before being used.
	 * @param pDeep
	 *            Not used.
	 * @return A <code>CIMEnumeration</code> object containing zero or more
	 *         <code>CIMInstancePath</code> objects referencing the enumerated
	 *         CIM_NameSpace instances using intra-namespace CIM instance paths
	 *         (model paths).
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_SUPPORTED CIM_ERR_NOT_SUPPORTED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_NAMESPACE CIM_ERR_INVALID_NAMESPACE}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_CLASS CIM_ERR_INVALID_CLASS}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 */
	public synchronized Enumeration enumNameSpace(CIMObjectPath pNamespacePath, boolean pDeep)
			throws CIMException {
		preCheck();
		Enumeration enumeration = iHandle.enumNameSpace(pNamespacePath, pDeep);
		return enumeration;
	}

	/**
	 * Executes a query on the target CIM server.
	 * 
	 * @param pNamespacePath
	 *            CIM namespace path of the namespace used to run the query
	 *            against. Must be an intra-server or intra-namespace object
	 *            path within the target CIM server. In the latter case, the
	 *            default namespace name is added to this parameter before being
	 *            used.
	 * @param pQuery
	 *            Query in the specified query language.
	 * @param pQueryLanguage
	 *            Identifier for the query language. CIM clients can interrogate
	 *            which query languages a CIM server supports, as described in
	 *            the CIM Operations over HTTP specification [DSP0200] (4.5.2
	 *            "Determining CIM Server Capabilities through the HTTP
	 *            Options"). Typical identifiers for query languages are:
	 *            <ul>
	 *            <li>{@link CIMClient#CQL CQL} - CIM Query Language</li>
	 *            <li>{@link CIMClient#WQL WQL} - WBEM Query Language</li>
	 *            </ul>
	 * @return An enumeration containing the query result
	 * @throws CIMException
	 */
	public synchronized Enumeration execQuery(CIMObjectPath pNamespacePath, String pQuery,
			String pQueryLanguage) throws CIMException {
		Benchmark.startTimer();
		preCheck();
		Enumeration enumeration = iHandle.execQuery(pNamespacePath, pQuery, pQueryLanguage);
		Benchmark.stopTimer();
		return enumeration;
	}

	/**
	 * Gets a copy of a CIM qualifier declaration from the target CIM server.
	 * 
	 * <p>
	 * The CIM qualifier declaration must exist.
	 * </p>
	 * 
	 * @param pQualifierPath
	 *            CIM qualifier path (a class path used to specify the qualifier
	 *            name in the class attribute). Must be an intra-server or
	 *            intra-namespace object path within the target CIM server. In
	 *            the latter case, the default namespace name is added to this
	 *            parameter before being used. Must not be null.
	 * @return A <code>CIMQualifierType</code> object.
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_SUPPORTED CIM_ERR_NOT_SUPPORTED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_NAMESPACE CIM_ERR_INVALID_NAMESPACE}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_FOUND CIM_ERR_NOT_FOUND}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 */
	public synchronized CIMQualifierType getQualifierType(CIMObjectPath pQualifierPath)
			throws CIMException {
		Benchmark.startTimer();
		preCheck();
		CIMQualifierType qualifierType = iHandle.getQualifierType(pQualifierPath);
		Benchmark.stopTimer();
		return qualifierType;
	}

	/**
	 * Performs the operations specified in the BatchHandle object.
	 * 
	 * <p>
	 * This is a multi-operation as specified in the CIM Operations over HTTP
	 * specification [DSP0200]. The operations are executed by the server one by
	 * one in the specified order. The next operation is started only once the
	 * previous operation has completed. It is not defined whether the CIM
	 * server stops processing the sequence upon any error encountered, or
	 * continues with the next operation in a "best effort" way of processing.
	 * </p>
	 * 
	 * <p>
	 * Multi-operations do not guarantee any transactional capabilities. For
	 * example, there is no guarantee that the single operations either all fail
	 * or all succeed.
	 * </p>
	 * 
	 * <p>
	 * Not all CIM servers support multi-operations. CIM client applications can
	 * figure out support for multi-operations as described in CIM Operations
	 * over HTTP specification [DSP0200] (4.5.2 "Determining CIM Server
	 * Capabilities through the HTTP Options").
	 * 
	 * @param pBatchHandle
	 *            Batch handle with the set of operations to be performed.
	 * @return A <code>BatchResult</code> object with the results of these
	 *         operations.
	 * @throws CIMException
	 *             with any one of the possible CIM status codes.
	 */
	public synchronized BatchResult performBatchOperations(BatchHandle pBatchHandle)
			throws CIMException {
		Benchmark.startTimer();
		preCheck();
		BatchResult res = iHandle.performBatchOperations(pBatchHandle);
		Benchmark.stopTimer();
		return res;
	}

	/**
	 * Enumerates the CIM asociation objects on the target CIM server that
	 * reference a particular CIM object, and returns CIM object paths to these
	 * objects.
	 * 
	 * <p>
	 * This method produces the same results as
	 * <code>referenceNames(pObjectPath, null, null)</code>
	 * </p>
	 * 
	 * @param pObjectPath
	 *            CIM object path to the CIM object (instance or class)
	 *            referenced by the association objects to be returned. Must be
	 *            an intra-server or intra-namespace object path within the
	 *            target CIM server. In the latter case, the default namespace
	 *            name is added to this parameter before being used. The
	 *            referenced CIM object must exist.
	 * @return A <code>CIMEnumeration</code> object containing zero or more
	 *         <code>CIMObjectPath</code> objects referencing the enumerated
	 *         association classes or instances using global CIM class or
	 *         instance paths.
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_SUPPORTED CIM_ERR_NOT_SUPPORTED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_NAMESPACE CIM_ERR_INVALID_NAMESPACE}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 * @see #referenceNames(CIMObjectPath, String, String)
	 */
	public synchronized Enumeration referenceNames(CIMObjectPath pObjectPath) throws CIMException {
		return referenceNames(pObjectPath, null, null);
	}

	/**
	 * Enumerates the CIM asociation objects on the target CIM server that
	 * reference a particular CIM object, and returns CIM object paths to these
	 * objects.
	 * 
	 * <p>
	 * If the referenced CIM object is an instance, then the association
	 * instances are enumerated. If the referenced CIM object is a class, then
	 * the association classes are enumerated.
	 * </p>
	 * 
	 * @param pObjectPath
	 *            CIM object path to the CIM object (instance or class)
	 *            referenced by the association objects to be returned. Must be
	 *            an intra-server or intra-namespace object path within the
	 *            target CIM server. In the latter case, the default namespace
	 *            name is added to this parameter before being used. The
	 *            referenced CIM object must exist.
	 * @param pResultClass
	 *            Allows to restrict the set of returned objects. If null, the
	 *            set of objects will not be restricted. If not null, must be a
	 *            valid CIM association class name, and the set of objects will
	 *            be restricted by removing any objects that do not satisfy the
	 *            following conditions: (1) If the referenced CIM object is an
	 *            instance, the enumerated objects are CIM instances of the
	 *            specified class or one of its subclasses. (2) If the
	 *            referenced CIM object is a class, the enumerated objects are
	 *            the specified class or one of its subclasses. When combined
	 *            with other object restriction filters, all restrictions are
	 *            accumulated.
	 * @param pRole
	 *            Allows to restrict the set of returned objects. If null, the
	 *            set of objects will not be restricted. If not null, must be a
	 *            valid CIM property name, and the set of objects will be
	 *            restricted by removing any objects that are not associations
	 *            in which the referenced object plays the specified pRole (i.e.
	 *            the name of the reference in the association class that refers
	 *            to the referenced object must match the value of this
	 *            parameter). When combined with other object restriction
	 *            filters, all restrictions are accumulated.
	 * @return A <code>CIMEnumeration</code> object containing zero or more
	 *         <code>CIMObjectPath</code> objects referencing the enumerated
	 *         association classes or instances using global CIM class or
	 *         instance paths.
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_SUPPORTED CIM_ERR_NOT_SUPPORTED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_NAMESPACE CIM_ERR_INVALID_NAMESPACE}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 */
	public synchronized Enumeration referenceNames(CIMObjectPath pObjectPath, String pResultClass,
			String pRole) throws CIMException {
		Benchmark.startTimer();
		preCheck();
		Enumeration enumeration = iHandle.referenceNames(pObjectPath, pResultClass, pRole);
		Benchmark.stopTimer();
		return enumeration;
	}

	/**
	 * Enumerates the CIM asociation objects on the target CIM server that
	 * reference a particular CIM object, and returns copies of these objects.
	 * 
	 * <p>
	 * This method produces the same results as
	 * <code>references(pObjectPath, null, null, true, true, null)</code>
	 * </p>
	 * 
	 * @param pObjectPath
	 *            CIM object path to the CIM object (instance or class)
	 *            referenced by the association objects to be returned. Must be
	 *            an intra-server or intra-namespace object path within the
	 *            target CIM server. In the latter case, the default namespace
	 *            name is added to this parameter before being used. The
	 *            referenced CIM object must exist.
	 * @return A <code>CIMEnumeration</code> object containing zero or more
	 *         <code>CIMInstance</code> or <code>CIMClass</code> objects.
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_SUPPORTED CIM_ERR_NOT_SUPPORTED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_NAMESPACE CIM_ERR_INVALID_NAMESPACE}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 * @see #references(CIMObjectPath, String, String, boolean, boolean,
	 *      String[])
	 */
	public synchronized Enumeration references(CIMObjectPath pObjectPath) throws CIMException {
		return references(pObjectPath, null, null, true, true, null);
	}

	/**
	 * Enumerates the CIM asociation objects on the target CIM server that
	 * reference a particular CIM object, and returns copies of these objects.
	 * 
	 * <p>
	 * If the referenced CIM object is an instance, then the association
	 * instances are enumerated. If the referenced CIM object is a class, then
	 * the association classes are enumerated.
	 * </p>
	 * 
	 * @param pObjectPath
	 *            CIM object path to the CIM object (instance or class)
	 *            referenced by the association objects to be returned. Must be
	 *            an intra-server or intra-namespace object path within the
	 *            target CIM server. In the latter case, the default namespace
	 *            name is added to this parameter before being used. The
	 *            referenced CIM object must exist.
	 * @param pResultClass
	 *            Allows to restrict the set of returned objects. If null, the
	 *            set of objects will not be restricted. If not null, must be a
	 *            valid CIM association class name, and the set of objects will
	 *            be restricted by removing any objects that do not satisfy the
	 *            following conditions: (1) If the referenced CIM object is an
	 *            instance, the enumerated objects are CIM instances of the
	 *            specified class or one of its subclasses. (2) If the
	 *            referenced CIM object is a class, the enumerated objects are
	 *            the specified class or one of its subclasses. When combined
	 *            with other object restriction filters, all restrictions are
	 *            accumulated.
	 * @param pRole
	 *            Allows to restrict the set of returned objects. If null, the
	 *            set of objects will not be restricted. If not null, must be a
	 *            valid CIM property name, and the set of objects will be
	 *            restricted by removing any objects that are not associations
	 *            in which the referenced object plays the specified pRole (i.e.
	 *            the name of the reference in the association class that refers
	 *            to the referenced object must match the value of this
	 *            parameter). When combined with other object restriction
	 *            filters, all restrictions are accumulated.
	 * @param pIncludeQualifiers
	 *            If false, no qualifiers will be included. If true, all
	 *            qualifiers for each object will be included in the objects
	 *            returned. <br>
	 *            <span style="color: rgb(255,0,0);">Deprecation Note: The CIM
	 *            Operations over HTTP specification [DSP0200] V1.2 has
	 *            deprecated this parameter. The preferred way to retrieve
	 *            qualifier information is to use the class operations (for
	 *            example, <code>getClass</code>) instead, and to set this
	 *            parameter to <code>false</code>.</span>
	 * @param pIncludeClassOrigin
	 *            If true, class origin information will be present in the
	 *            returned objects. Class origin information indicates the class
	 *            in which a CIM property, method, or reference was first
	 *            defined in the class hierarchy. It can be retrieved from the
	 *            Java objects representing these CIM elements by means of the
	 *            <code>getOriginClass()</code> method. If false, class origin
	 *            information will not be present in the returned objects.
	 * @param pPropertyList
	 *            An array of CIM property names that allows to restrict the set
	 *            of properties in the returned objects (classes or instances).
	 *            If null, the set of properties will not be restricted by this
	 *            parameter. If not null, the set of properties will be
	 *            restricted such that any properties missing from this list
	 *            will not be present in the returned objects. In this case, the
	 *            members of the array must define zero or more property names
	 *            of the specified class. This may include inherited property
	 *            names or property names explicitly defined in the specified
	 *            class, but must not include property names defined in
	 *            subclasses of the specified class. Duplicate property names
	 *            are tolerated and the duplicates are ignored.
	 * @return A <code>CIMEnumeration</code> object containing zero or more
	 *         <code>CIMInstance</code> or <code>CIMClass</code> objects.
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_SUPPORTED CIM_ERR_NOT_SUPPORTED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_NAMESPACE CIM_ERR_INVALID_NAMESPACE}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 * @see org.sblim.wbem.cim.CIMProperty#getOriginClass()
	 * @see org.sblim.wbem.cim.CIMMethod#getOriginClass()
	 * @see org.sblim.wbem.cim.CIMInstance
	 * @see org.sblim.wbem.cim.CIMClass
	 */
	public synchronized Enumeration references(CIMObjectPath pObjectPath, String pResultClass,
			String pRole, boolean pIncludeQualifiers, boolean pIncludeClassOrigin,
			String[] pPropertyList) throws CIMException {
		Benchmark.startTimer();
		preCheck();
		Enumeration enumeration = iHandle.references(pObjectPath, pResultClass, pRole,
				pIncludeQualifiers, pIncludeClassOrigin, pPropertyList);
		Benchmark.stopTimer();
		return enumeration;
	}

	/**
	 * Modifies a property value of a CIM instance on the target CIM server.
	 * 
	 * <p>
	 * This method produces the same results as
	 * <code>setProperty(pInstancePath, propertyName, null)</code>
	 * </p>
	 * 
	 * @param pInstancePath
	 *            CIM instance path to the CIM instance containing the property.
	 *            Must be an intra-server or intra-namespace object path within
	 *            the target CIM server. In the latter case, the default
	 *            namespace name is added to this parameter before being used.
	 *            Must not be null. The CIM instance must exist.
	 * @param pPropertyName
	 *            Name of the CIM property to be modified. The property must be
	 *            defined on the class of the CIM instance. Must not be null.
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_SUPPORTED CIM_ERR_NOT_SUPPORTED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_NAMESPACE CIM_ERR_INVALID_NAMESPACE}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_CLASS CIM_ERR_INVALID_CLASS}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_FOUND CIM_ERR_NOT_FOUND}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NO_SUCH_PROPERTY CIM_ERR_NO_SUCH_PROPERTY}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_TYPE_MISMATCH CIM_ERR_TYPE_MISMATCH}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 * @see #setProperty(CIMObjectPath, String, CIMValue)
	 */
	public synchronized void setProperty(CIMObjectPath pInstancePath, String pPropertyName)
			throws CIMException {
		setProperty(pInstancePath, pPropertyName, null);
	}

	/**
	 * Modifies a property value of a CIM instance on the target CIM server.
	 * 
	 * <p>
	 * This operation is supported from a client perspective even if CIM
	 * providers do not implement it, since the CIM server will use the
	 * ModifyInstance provider operation in that case.
	 * </p>
	 * 
	 * <p>
	 * However, the setProperty client operation should be expected to go away
	 * in the future. It is therefore recommended already now to use
	 * {@link #setInstance(CIMObjectPath, CIMInstance, boolean, String[])}
	 * instead.
	 * </p>
	 * 
	 * @param pInstancePath
	 *            CIM instance path to the CIM instance containing the property.
	 *            Must be an intra-server or intra-namespace object path within
	 *            the target CIM server. In the latter case, the default
	 *            namespace name is added to this parameter before being used.
	 *            Must not be null. The CIM instance must exist.
	 * @param pPropertyName
	 *            Name of the CIM property to be modified. The property must be
	 *            defined on the class of the CIM instance. Must not be null.
	 * @param pNewValue
	 *            New value for the property. If null, sets the property value
	 *            to null.
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_SUPPORTED CIM_ERR_NOT_SUPPORTED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_NAMESPACE CIM_ERR_INVALID_NAMESPACE}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_CLASS CIM_ERR_INVALID_CLASS}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_FOUND CIM_ERR_NOT_FOUND}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NO_SUCH_PROPERTY CIM_ERR_NO_SUCH_PROPERTY}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_TYPE_MISMATCH CIM_ERR_TYPE_MISMATCH}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 */
	public synchronized void setProperty(CIMObjectPath pInstancePath, String pPropertyName,
			CIMValue pNewValue) throws CIMException {
		Benchmark.startTimer();
		preCheck();
		iHandle.setProperty(pInstancePath, pPropertyName, pNewValue);
		Benchmark.stopTimer();
	}

	/**
	 * Modifies a CIM class on the target CIM server.
	 * 
	 * @param pClassPath
	 *            CIM class path to the CIM class to be modified. Must be an
	 *            intra-server or intra-namespace object path within the target
	 *            CIM server. In the latter case, the default namespace name is
	 *            added to this parameter before being used. The CIM class must
	 *            exist. Must not be null.
	 * @param pModifiedClass
	 *            CIM class definition of the modified class. Must be a valid
	 *            class definition according to the CIM Infrastructure
	 *            Specification [DSP0004] and must not be null. The CIM server
	 *            will behave as if the previous class definition was completely
	 *            removed and re-created from the modified definition. If a
	 *            superclass was defined on the previous class, the modified
	 *            definition must observe the following restrictions: (1) The
	 *            modified definition must not define a different superclass,
	 *            and the superclass must exist. (2) Qualifiers with the
	 *            DisableOverride flavor must not be defined in the modified
	 *            definition. Any class origin and propagated information in
	 *            <code>modifiedClass</code> will be ignored (it is
	 *            reconstructed by the CIM server).
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_SUPPORTED CIM_ERR_NOT_SUPPORTED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_NAMESPACE CIM_ERR_INVALID_NAMESPACE}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_FOUND CIM_ERR_NOT_FOUND}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_SUPERCLASS CIM_ERR_INVALID_SUPERCLASS}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_CLASS_HAS_CHILDREN CIM_ERR_CLASS_HAS_CHILDREN}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_CLASS_HAS_INSTANCES CIM_ERR_CLASS_HAS_INSTANCES}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 */
	public synchronized void setClass(CIMObjectPath pClassPath, CIMClass pModifiedClass)
			throws CIMException {
		Benchmark.startTimer();
		preCheck();
		iHandle.setClass(pClassPath, pModifiedClass);
		Benchmark.stopTimer();
	}

	/**
	 * Modifies a CIM instance on the target CIM server.
	 * 
	 * <p>
	 * This method produces the same results as
	 * <code>setInstance(pInstancePath, modifiedInstance, true, null)</code>
	 * </p>
	 * 
	 * @param pInstancePath
	 *            CIM instance path to the CIM instance to be modified. Must be
	 *            an intra-server or intra-namespace object path within the
	 *            target CIM server. In the latter case, the default namespace
	 *            name is added to this parameter before being used. The CIM
	 *            instance must exist. Must not be null.
	 * @param pModifiedInstance
	 *            CIM instance defining the new values for the properties. Must
	 *            be a valid instance definition for the underlying CIM class
	 *            according to the CIM Infrastructure Specification [DSP0004].
	 *            The instance may contain a subset of all properties. Any class
	 *            origin and propagated information in
	 *            <code>modifiedInstance</code> will be ignored (it is
	 *            reconstructed by the CIM server). Any qualifiers included in
	 *            <code>modifiedInstance</code> may be ignored (it is
	 *            recommended that CIM servers ignore them). <br>
	 *            <span style="color: rgb(255,0,0);">Deprecation Note: The CIM
	 *            Infrastructure Specification [DSP0004] V2.3 has deprecated
	 *            instance level qualifiers. The preferred way to modify
	 *            qualifier information is to use the class operations (for
	 *            example, <code>setClass</code>) instead.</span> Must not
	 *            be null.
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_SUPPORTED CIM_ERR_NOT_SUPPORTED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_NAMESPACE CIM_ERR_INVALID_NAMESPACE}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_CLASS CIM_ERR_INVALID_CLASS}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_FOUND CIM_ERR_NOT_FOUND}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 * @see #setInstance(CIMObjectPath, CIMInstance, boolean, String[])
	 */
	public synchronized void setInstance(CIMObjectPath pInstancePath, CIMInstance pModifiedInstance)
			throws CIMException {
		setInstance(pInstancePath, pModifiedInstance, true, null);
	}

	/**
	 * Modifies a CIM instance on the target CIM server.
	 * 
	 * <p>
	 * The set of property values to be modified is determined by
	 * <code>pPropertyList</code>. Out of that set, the properties of the
	 * instance are attempted to be modified as follows:
	 * <ul>
	 * <li>For any properties contained in <code>modifiedInstance</code>,
	 * the value specified there will be used as the new value (this is true
	 * also for the null value).</li>
	 * <li>For any properties not specified in <code>modifiedInstance</code>,
	 * the class defined default value will be used as the new value. If there
	 * is no class defined default value, null is used as the new value.</li>
	 * </ul>
	 * Whether or not a modification attempt actually results in a changed
	 * property value in the instance on the CIM server depends on a number of
	 * factors:
	 * <ul>
	 * <li>If the property has its WRITE qualifier effectively being false, the
	 * modification request will be silently ignored. This is always the case
	 * for any key properties.</li>
	 * <li>If the property value is dynamically created by a CIM provider (for
	 * instance, for volatile properties), the modification request may or may
	 * not be honored.</li>
	 * </ul>
	 * </p>
	 * 
	 * @param pInstancePath
	 *            CIM instance path to the CIM instance to be modified. Must be
	 *            an intra-server or intra-namespace object path within the
	 *            target CIM server. In the latter case, the default namespace
	 *            name is added to this parameter before being used. The CIM
	 *            instance must exist. Must not be null.
	 * @param pModifiedInstance
	 *            CIM instance defining the new values for the properties. Must
	 *            be a valid instance definition for the underlying CIM class
	 *            according to the CIM Infrastructure Specification [DSP0004].
	 *            The instance may contain a subset of all properties. Any class
	 *            origin and propagated information in
	 *            <code>modifiedInstance</code> will be ignored (it is
	 *            reconstructed by the CIM server). Any qualifiers included in
	 *            <code>modifiedInstance</code> may be ignored (it is
	 *            recommended that CIM servers ignore them). <br>
	 *            <span style="color: rgb(255,0,0);">Deprecation Note: The CIM
	 *            Infrastructure Specification [DSP0004] V2.3 has deprecated
	 *            instance level qualifiers. The preferred way to modify
	 *            qualifier information is to use the class operations (for
	 *            example, <code>setClass</code>) instead.</span> Must not
	 *            be null.
	 * @param pIncludeQualifiers
	 *            The behavior of this parameter is not specified. A CIM client
	 *            application can not rely on it to have any impact on the
	 *            operation. It is recommended that the CIM server ignore any
	 *            qualifiers included in the modified instance. <br>
	 *            <span style="color: rgb(255,0,0);">Deprecation Note: The CIM
	 *            Operations over HTTP specification [DSP0200] V1.2 has
	 *            deprecated this parameter. The preferred way to modify
	 *            qualifier information is to use the class operations (for
	 *            example, <code>setClass</code>) instead, and to set this
	 *            parameter to <code>false</code>.</span>
	 * @param pPropertyList
	 *            An array of CIM property names that allows to restrict the set
	 *            of properties being modified. If null, the set of properties
	 *            will not be restricted by this parameter. If not null, the set
	 *            of properties will be restricted such that any properties
	 *            missing from this list will not be modified. Duplicate
	 *            property names are tolerated and the duplicates are ignored.
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_SUPPORTED CIM_ERR_NOT_SUPPORTED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_NAMESPACE CIM_ERR_INVALID_NAMESPACE}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_CLASS CIM_ERR_INVALID_CLASS}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_FOUND CIM_ERR_NOT_FOUND}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 */
	public synchronized void setInstance(CIMObjectPath pInstancePath,
			CIMInstance pModifiedInstance, boolean pIncludeQualifiers, String[] pPropertyList)
			throws CIMException {
		Benchmark.startTimer();
		preCheck();
		iHandle.setInstance(pInstancePath, pModifiedInstance, pIncludeQualifiers, pPropertyList);
		Benchmark.stopTimer();
	}

	/**
	 * Creates or replaces a CIM qualifier declaration on the target CIM server.
	 * 
	 * <p>
	 * If the qualifier declaration does not exist, it will be created. If it
	 * exists, it will be replaced.
	 * </p>
	 * 
	 * <p>
	 * The CIM namespace must exist.
	 * </p>
	 * 
	 * @param pNamespacePath
	 *            CIM namespace path. Must be an intra-server or intra-namespace
	 *            object path within the target CIM server. In the latter case,
	 *            the default namespace name is added to this parameter before
	 *            being used. Must not be null. Also, the class attribute must
	 *            not be null. TODO Proposal: It turns out the code ignores the
	 *            class attribute, but requires it to be non-null. Remove this
	 *            check.
	 * @param pQualifierType
	 *            CIM qualifier declaration. Must be a valid qualifier
	 *            declaration according to the CIM Infrastructure Specification
	 *            [DSP0004].
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_ACCESS_DENIED CIM_ERR_ACCESS_DENIED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_NOT_SUPPORTED CIM_ERR_NOT_SUPPORTED}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_NAMESPACE CIM_ERR_INVALID_NAMESPACE}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             <dt>{@link CIMException#CIM_ERR_FAILED CIM_ERR_FAILED}</dt>
	 *             </dl>
	 */
	public synchronized void setQualifierType(CIMObjectPath pNamespacePath,
			CIMQualifierType pQualifierType) throws CIMException {
		Benchmark.startTimer();
		preCheck();
		iHandle.setQualifierType(pNamespacePath, pQualifierType);
		Benchmark.stopTimer();
	}

	/**
	 * Control the use of HTTP MPost request.
	 * 
	 * @param pValue
	 *            If true, use the HTTP MPost request. Else, use the HTTP Post
	 *            request.
	 */
	public synchronized void useMPost(boolean pValue) {
		preCheck();
		iHandle.useMPost(pValue);
	}

	/**
	 * Control the use of HTTP 1.1.
	 * 
	 * @param pValue
	 *            If true, use HTTP 1.1. Else, use HTTP 1.0.
	 */
	public synchronized void useHttp11(boolean pValue) {
		preCheck();
		iHandle.useHttp11(pValue);
	}

	/**
	 * Gets the default CIM namespace associated with this CIM client.
	 * 
	 * @return A <code>CIMNameSpace</code> object containing the default
	 *         namespace in its namespace attribute.
	 */
	public synchronized CIMNameSpace getNameSpace() {
		preCheck();
		return iHandle.getNameSpace();
	}

	/**
	 * Modifies the locale information of the CIM client.
	 * 
	 * <p>
	 * The locale information of the CIM client is used to construct the
	 * Content-Language and Accept-Language HTTP headers. The CIM server uses
	 * these headers to translate any translatable property and qualifier
	 * values, and any localized messages.
	 * </p>
	 * 
	 * @param pLocale
	 *            The new locale information. Must not be null.
	 * @throws CIMException
	 *             with one of these CIM status codes:
	 *             <dl style="margin-left: 80px;">
	 *             <dt>{@link CIMException#CIM_ERR_INVALID_PARAMETER CIM_ERR_INVALID_PARAMETER}</dt>
	 *             </dl>
	 */
	public synchronized void setLocale(Locale pLocale) {
		preCheck();
		iHandle.setLocale(pLocale);
	}

	/**
	 * Gets the current locale information of the CIM client.
	 * 
	 * The initial locale information (that is, without any invocations of
	 * <code>setLocale</code>) is the value <code>Locale.getDefault()</code>.
	 * 
	 * @return Current locale information.
	 */
	public synchronized Locale getLocale() {
		preCheck();
		return iHandle.getLocale();
	}

	/**
	 * Gets the session properties of the CIM client. If this CIM client is
	 * associated with the global properties this method returns
	 * <code>null</code>
	 * 
	 * @return The session properties
	 */
	public synchronized SessionProperties getSessionProperties() {
		return iHandle.getSessionProperties();
	}

	/**
	 * Sets the session properties for the CIM client.
	 * 
	 * @param pProperties
	 *            The session properties
	 */
	public synchronized void setSessionProperties(SessionProperties pProperties) {
		iHandle.setSessionProperties(pProperties);
	}
}
