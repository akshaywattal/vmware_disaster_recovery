/**
 * BatchResult.java
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
 * 1535756    2006-08-07  lupusalex    Make code warning free
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 *
 */

package org.sblim.wbem.client;

import org.sblim.wbem.cim.CIMException;
import org.sblim.wbem.client.operations.CIMOperation;

/**
 * BatchResult class encapsulates the results from a batch request, allowing to
 * manipulate results from individual operations one at the time. This class
 * provides mechanisms to check which operations failed, and which operations
 * where successfully completed.
 * 
 */
public class BatchResult {

	protected CIMOperation[] operations;

	protected int[] failureIds;

	protected int[] successIds;

	/**
	 * Constructs an object of a BatchResult.
	 * 
	 * @param results
	 *            An array of CIMOperation
	 */
	public BatchResult(Object[] results) {
		if (results instanceof CIMOperation[]) this.operations = (CIMOperation[]) results;
		else throw new IllegalArgumentException(
				"Results argument not an instance of CIMOperation[]");

		int[] failure = new int[operations.length];
		int[] success = new int[operations.length];
		int totalFailure = 0;
		int totalSuccess = 0;
		for (int i = 0; i < operations.length; i++) {
			if (!(operations[i].isException())) {
				success[totalSuccess++] = i;
			} else {
				failure[totalFailure++] = i;
			}
		}
		failureIds = new int[totalFailure];
		System.arraycopy(failure, 0, failureIds, 0, totalFailure);
		successIds = new int[totalSuccess];
		System.arraycopy(success, 0, successIds, 0, totalSuccess);
	}

	/**
	 * Returns an array which contains the indexes of the operations that
	 * failed. A zero length array, means that none of the operations fail.
	 * 
	 * @return The indexes of the operations that failed
	 */
	public int[] getFailureIds() {
		return failureIds;
	}

	/**
	 * Returns the object produced by the specified batch operation. The object
	 * may be an enumeration of entities, a single CIM Object (CIMClass,
	 * CIMInstance, CIMObject) or null
	 * 
	 * @param operationID
	 * @return The object
	 * @throws CIMException
	 *             a cim exception of the operation was not succesfully
	 *             completed.
	 * @throws IllegalArgumentException
	 *             is an invalid operationID is passed.
	 */
	public Object getResult(int operationID) throws CIMException {
		if (operationID < 0 || operationID >= operations.length) throw new IllegalArgumentException(
				"operation ID out of range");
		return operations[operationID].getResult();
	}

	/**
	 * Returns a list of the operation ID that were successfully completed.
	 * 
	 * @return The indexes of the operations that succeeded
	 */
	public int[] getSuccessIds() {
		return successIds;
	}
}
