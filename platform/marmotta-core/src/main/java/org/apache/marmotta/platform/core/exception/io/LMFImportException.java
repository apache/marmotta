/**
 * Copyright (C) 2013 Salzburg Research.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.marmotta.platform.core.exception.io;

/**
 * Exception that is thrown if KiWi recognizes problems with the import service.
 * 
 * @author Stephanie Stroka
 *			(stephanie.stroka@salzburgresearch.at)
 *
 */
public class LMFImportException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8157237047910758443L;

	public LMFImportException() {
		super();
	}

	public LMFImportException(String message, Throwable cause) {
		super(message, cause);
	}

	public LMFImportException(String message) {
		super(message);
	}

	public LMFImportException(Throwable cause) {
		super(cause);
	}

}
