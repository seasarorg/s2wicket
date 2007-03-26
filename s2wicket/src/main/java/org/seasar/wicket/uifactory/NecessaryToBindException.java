package org.seasar.wicket.uifactory;

import java.lang.reflect.Field;

class NecessaryToBindException extends Exception {
	
	private Field parentField;

	NecessaryToBindException(Field parentField) {
		super();
		this.parentField = parentField;
	}
	
	Field getParentField() {
		return parentField;
	}

}
